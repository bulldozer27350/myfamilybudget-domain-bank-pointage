package com.moe.myfamilybudget.domain.bankpointage.core.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.moe.myfamilybudget.domain.bankpointage.model.BankImportModel;
import com.moe.myfamilybudget.domain.bankpointage.model.BankImportMutatedEvent;
import com.moe.myfamilybudget.domain.bankpointage.model.BankTransactionPage;
import com.moe.myfamilybudget.domain.bankpointage.port.BankReader;
import com.moe.myfamilybudget.domain.bankpointage.port.BankSnapshotWriter;
import com.moe.myfamilybudget.domain.bankpointage.port.BankTransactionPageReader;
import com.moe.myfamilybudget.domain.bankpointage.port.BankWriter;

/**
 * Adaptateur JPA du silo Banque/Pointage : implemente {@link BankReader}, {@link BankWriter} et
 * {@link BankSnapshotWriter} directement sur le schema relationnel introduit par l'etape 1/5 de la
 * migration (areas/bank-pointage-relational-migration.md, cote projet : {@code bank-reloc-core-1} a
 * {@code -10}), sans passer par le cache global ni par le {@code PersistenceManager}.
 *
 * <p><strong>Etape 3/5</strong> : avant cette classe, l'import bancaire etait stocke comme un document JSON
 * unique ({@code bank_import_document}, voir {@link BankImportDocumentEntity}). Cette table n'est plus lue
 * ni ecrite a partir d'ici (elle reste en base intacte, comme copie de secours, jusqu'a l'etape 5) ; seule
 * {@code com.moe.myfamilybudget.server.internal.migration.BankImportRelationalMigration} (depot
 * MyFamilyBudget, etape 2/5) la lit encore, une seule fois, pour peupler le schema relationnel au moment de
 * la mise a jour.
 *
 * <p>Les ecritures ne s'appellent que dans une transaction deja ouverte ({@code TransactionRunner}) et apres
 * la prise du verrou du silo ({@code MutationSilo.BANK_POINTAGE}) par l'appelant : l'adaptateur ne porte ni
 * {@code @Transactional} ni verrou. Chaque ecriture remplace l'integralite des tables par l'import resultant
 * (suppression puis insertion, dans un ordre qui respecte les contraintes de cle etrangere entre tables :
 * enfants avant parents a la suppression, parents avant enfants a l'insertion). Pas de relation JPA
 * {@code @OneToMany}/{@code @ManyToOne} entre entites : la lecture charge systematiquement l'integralite de
 * chaque table et recompose les listes en memoire ({@link BankRelationalMapper}), ce qui evite tout risque
 * de requetes N+1 — le cas d'usage n'est jamais une navigation entite par entite, mais toujours un
 * chargement ou un remplacement complet.
 *
 * <p>Lecture : sans ligne en base, un import vide est restitue (aucun consommateur ne recoit {@code null}).
 *
 * <p>Apres chaque ecriture, un {@link BankImportMutatedEvent} est publie : l'ecoute apres commit declenche le
 * controle automatique des notifications, comme avant l'etape 3.
 *
 * <p><strong>Etape 4/5</strong> : {@link BankTransactionPageReader#findTransactions} complete {@link BankReader}
 * (qui reste un chargement complet) par une lecture paginee et filtree par etat de categorisation, pour
 * l'ecran de categorisation : seule la page demandee est chargee en memoire (serveur comme navigateur),
 * via l'index {@code bank_transaction_category_id_idx} (changeSet {@code bank-reloc-core-4}).
 */
@Component
public class JpaBankStore implements BankReader, BankWriter, BankSnapshotWriter, BankTransactionPageReader {

    private final BankColumnMappingRepository columnMappingRepository;
    private final BankCategoryRepository categoryRepository;
    private final BankRuleRepository ruleRepository;
    private final BankTransactionRepository transactionRepository;
    private final BankTransactionSplitRepository transactionSplitRepository;
    private final BankPendingOperationRepository pendingOperationRepository;
    private final BankPendingOperationSplitRepository pendingOperationSplitRepository;
    private final BankMatchingRepository matchingRepository;
    private final BankMatchingLinkRepository matchingLinkRepository;
    private final BankMatchingLinkTxRepository matchingLinkTxRepository;
    private final ApplicationEventPublisher eventPublisher;

    @PersistenceContext
    private EntityManager entityManager;

    public JpaBankStore(
            BankColumnMappingRepository columnMappingRepository,
            BankCategoryRepository categoryRepository,
            BankRuleRepository ruleRepository,
            BankTransactionRepository transactionRepository,
            BankTransactionSplitRepository transactionSplitRepository,
            BankPendingOperationRepository pendingOperationRepository,
            BankPendingOperationSplitRepository pendingOperationSplitRepository,
            BankMatchingRepository matchingRepository,
            BankMatchingLinkRepository matchingLinkRepository,
            BankMatchingLinkTxRepository matchingLinkTxRepository,
            ApplicationEventPublisher eventPublisher) {
        this.columnMappingRepository = columnMappingRepository;
        this.categoryRepository = categoryRepository;
        this.ruleRepository = ruleRepository;
        this.transactionRepository = transactionRepository;
        this.transactionSplitRepository = transactionSplitRepository;
        this.pendingOperationRepository = pendingOperationRepository;
        this.pendingOperationSplitRepository = pendingOperationSplitRepository;
        this.matchingRepository = matchingRepository;
        this.matchingLinkRepository = matchingLinkRepository;
        this.matchingLinkTxRepository = matchingLinkTxRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public BankImportModel getBankImport() {
        BankColumnMappingEntity mapping = columnMappingRepository.findById(BankColumnMappingEntity.SINGLETON_ID)
                .orElse(null);
        return BankRelationalMapper.toModel(
                mapping,
                categoryRepository.findAll(),
                ruleRepository.findAll(),
                transactionRepository.findAll(),
                transactionSplitRepository.findAll(),
                pendingOperationRepository.findAll(),
                pendingOperationSplitRepository.findAll(),
                matchingRepository.findAll(),
                matchingLinkRepository.findAll(),
                matchingLinkTxRepository.findAll());
    }

    /**
     * Etape 4/5 : lecture paginee et filtree par etat de categorisation, pour l'ecran de categorisation.
     * Charge uniquement les repartitions ({@code bank_transaction_split}) des transactions de la page
     * (pas l'integralite de la table), via {@link BankTransactionSplitRepository#findByTransactionIdIn}.
     */
    @Override
    public BankTransactionPage findTransactions(Boolean categorized, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));
        Page<BankTransactionEntity> entityPage;
        if (categorized == null) {
            entityPage = transactionRepository.findAll(pageable);
        } else if (categorized) {
            entityPage = transactionRepository.findByCategoryIdIsNotNull(pageable);
        } else {
            entityPage = transactionRepository.findByCategoryIdIsNull(pageable);
        }

        List<String> transactionIds = entityPage.getContent().stream().map(BankTransactionEntity::getId).toList();
        Map<String, List<BankTransactionSplitEntity>> splitsByTransactionId = transactionSplitRepository
                .findByTransactionIdIn(transactionIds).stream()
                .collect(Collectors.groupingBy(BankTransactionSplitEntity::getTransactionId));

        List<BankImportModel.BankTransactionModel> transactions = entityPage.getContent().stream()
                .map(tx -> BankRelationalMapper.toTransactionModel(tx,
                        splitsByTransactionId.getOrDefault(tx.getId(), List.of())))
                .toList();

        return new BankTransactionPage(transactions, entityPage.getNumber(), entityPage.getSize(),
                entityPage.getTotalElements(), entityPage.getTotalPages());
    }

    /** Un import {@code null} est ignore (aucune ecriture, aucun evenement), comme avant l'etape 3. */
    @Override
    public void updateBankImport(BankImportModel bankImport) {
        if (bankImport == null) {
            return;
        }
        rewrite(bankImport);
        publishMutated("updateBankImport");
    }

    /** Import de l'import bancaire ({@code null} accepte : tables vides). */
    @Override
    public void replace(BankImportModel bankImport) {
        rewrite(bankImport);
        publishMutated("replace");
    }

    /** Remise a vide de l'import bancaire (tables videes, rien n'est reinsere). */
    @Override
    public void reset() {
        rewrite(emptyImport());
        publishMutated("reset");
    }

    /** Remplace le contenu des tables par {@code bankImport} ({@code null} : tables vides). */
    private void rewrite(BankImportModel bankImport) {
        deleteAllRows();
        if (entityManager != null && TransactionSynchronizationManager.isActualTransactionActive()) {
            entityManager.flush();
            entityManager.clear();
        }
        if (bankImport == null) {
            return;
        }
        insertAllRows(bankImport);
        // Par prudence (comme l'ancien code, qui devait forcer l'ordre suppression/insertion) : garantit
        // que les insertions encore en attente (entites a identifiant assigne, non genere, que Hibernate
        // peut differer jusqu'au flush) sont bien ecrites avant de rendre la main a l'appelant.
        if (entityManager != null && TransactionSynchronizationManager.isActualTransactionActive()) {
            entityManager.flush();
        } else {
            columnMappingRepository.flush();
        }
    }

    /** Ordre obligatoire : les tables enfants (contrainte de cle etrangere) avant leurs parents. */
    private void deleteAllRows() {
        matchingLinkTxRepository.deleteAllInBatch();
        matchingLinkRepository.deleteAllInBatch();
        matchingRepository.deleteAllInBatch();
        pendingOperationSplitRepository.deleteAllInBatch();
        pendingOperationRepository.deleteAllInBatch();
        transactionSplitRepository.deleteAllInBatch();
        transactionRepository.deleteAllInBatch();
        ruleRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        columnMappingRepository.deleteAllInBatch();
    }

    /** Ordre obligatoire : les tables parents avant leurs enfants (contrainte de cle etrangere). */
    private void insertAllRows(BankImportModel bankImport) {
        columnMappingRepository.save(BankRelationalMapper.toColumnMappingEntity(bankImport.columnMapping()));
        categoryRepository.saveAll(BankRelationalMapper.toCategoryEntities(bankImport.categories()));
        ruleRepository.saveAll(BankRelationalMapper.toRuleEntities(bankImport.rules()));
        transactionRepository.saveAll(BankRelationalMapper.toTransactionEntities(bankImport.transactions()));
        transactionSplitRepository.saveAll(
                BankRelationalMapper.toTransactionSplitEntities(bankImport.transactions()));
        pendingOperationRepository.saveAll(
                BankRelationalMapper.toPendingOperationEntities(bankImport.pendingOperations()));
        pendingOperationSplitRepository.saveAll(
                BankRelationalMapper.toPendingOperationSplitEntities(bankImport.pendingOperations()));
        matchingRepository.saveAll(BankRelationalMapper.toMatchingEntities(bankImport.matchings()));
        insertMatchingLinksAndTransactions(bankImport);
    }

    /**
     * Les liens de rapprochement ont un identifiant genere (necessaire avant de pouvoir creer les lignes
     * {@code bank_matching_link_tx} correspondantes) : chaque lien est sauvegarde individuellement pour
     * recuperer son identifiant, puis ses transactions associees sont accumulees et sauvegardees en bloc a
     * la fin (volume attendu faible : quelques dizaines de mois de rapprochement, pas de souci de
     * performance a iterer ainsi).
     */
    private void insertMatchingLinksAndTransactions(BankImportModel bankImport) {
        List<BankMatchingLinkTxEntity> allLinkTxRows = new ArrayList<>();
        for (BankImportModel.MatchingModel matching : bankImport.matchings()) {
            for (BankImportModel.MatchingLinkModel link : matching.links()) {
                BankMatchingLinkEntity savedLink = matchingLinkRepository.save(
                        new BankMatchingLinkEntity(matching.month(), link.budgetLineId()));
                for (String txId : link.txIds()) {
                    allLinkTxRows.add(new BankMatchingLinkTxEntity(savedLink.getId(), txId));
                }
            }
        }
        matchingLinkTxRepository.saveAll(allLinkTxRows);
    }

    private static BankImportModel emptyImport() {
        return new BankImportModel(List.of(), List.of(), List.of());
    }

    private void publishMutated(String mutationKind) {
        eventPublisher.publishEvent(new BankImportMutatedEvent(mutationKind));
    }
}
