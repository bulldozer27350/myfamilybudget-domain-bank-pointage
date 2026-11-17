package com.moe.myfamilybudget.domain.bankpointage.core.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.moe.myfamilybudget.domain.bankpointage.model.BankImportModel;

/**
 * Conversion entre le schéma relationnel (étape 1, {@code bank-reloc-core-*}) et {@link BankImportModel},
 * étape 3 de la migration (areas/bank-pointage-relational-migration.md, côté projet).
 *
 * <p>Convention reprise de {@link com.moe.myfamilybudget.server.internal.migration.BankImportRelationalMigration}
 * (autre dépôt, même logique) : le modèle Java représente "non catégorisé" par une chaîne vide, jamais
 * {@code null} (constructeurs compacts de {@link BankImportModel}) ; {@link #toModel} laisse faire cette
 * normalisation automatiquement (elle s'applique à {@code null} comme à une chaîne vide), {@link #blankToNull}
 * fait la conversion inverse à l'écriture.
 */
public final class BankRelationalMapper {

    private BankRelationalMapper() {
    }

    // ------------------------------------------------------------------------------------------------
    // Lecture : entités -> modèle
    // ------------------------------------------------------------------------------------------------

    public static BankImportModel toModel(
            BankColumnMappingEntity mapping,
            List<BankCategoryEntity> categories,
            List<BankRuleEntity> rules,
            List<BankTransactionEntity> transactions,
            List<BankTransactionSplitEntity> transactionSplits,
            List<BankPendingOperationEntity> pendingOperations,
            List<BankPendingOperationSplitEntity> pendingOperationSplits,
            List<BankMatchingEntity> matchings,
            List<BankMatchingLinkEntity> matchingLinks,
            List<BankMatchingLinkTxEntity> matchingLinkTxs) {

        Map<String, List<BankTransactionSplitEntity>> splitsByTransactionId = transactionSplits.stream()
                .collect(Collectors.groupingBy(BankTransactionSplitEntity::getTransactionId));
        Map<String, List<BankPendingOperationSplitEntity>> splitsByPendingOperationId = pendingOperationSplits
                .stream().collect(Collectors.groupingBy(BankPendingOperationSplitEntity::getPendingOperationId));
        Map<Long, List<BankMatchingLinkTxEntity>> txsByLinkId = matchingLinkTxs.stream()
                .collect(Collectors.groupingBy(BankMatchingLinkTxEntity::getMatchingLinkId));
        Map<String, List<BankMatchingLinkEntity>> linksByMonth = matchingLinks.stream()
                .collect(Collectors.groupingBy(BankMatchingLinkEntity::getMonth));

        return new BankImportModel(
                toColumnMappingModel(mapping),
                categories.stream().map(BankRelationalMapper::toCategoryModel).toList(),
                rules.stream().map(BankRelationalMapper::toRuleModel).toList(),
                transactions.stream()
                        .map(tx -> toTransactionModel(tx, splitsByTransactionId.getOrDefault(tx.getId(), List.of())))
                        .toList(),
                pendingOperations.stream()
                        .map(op -> toPendingOperationModel(op,
                                splitsByPendingOperationId.getOrDefault(op.getId(), List.of())))
                        .toList(),
                matchings.stream()
                        .map(matching -> toMatchingModel(matching,
                                linksByMonth.getOrDefault(matching.getMonth(), List.of()), txsByLinkId))
                        .toList());
    }

    private static BankImportModel.BankColumnMappingModel toColumnMappingModel(BankColumnMappingEntity mapping) {
        if (mapping == null) {
            return new BankImportModel.BankColumnMappingModel(null, null, null, null, null, null, null,
                    List.of(), List.of(), List.of());
        }
        return new BankImportModel.BankColumnMappingModel(
                mapping.getDelimiter(), mapping.getDateFormat(), mapping.getHasHeader(), mapping.getDateCol(),
                mapping.getLabelCol(), mapping.getTypeCol(), mapping.getAmountCol(),
                parseCsvInts(mapping.getExtraLabelColsCsv()), parseCsvInts(mapping.getCreditColsCsv()),
                parseCsvInts(mapping.getDebitColsCsv()));
    }

    private static BankImportModel.CategoryModel toCategoryModel(BankCategoryEntity entity) {
        return new BankImportModel.CategoryModel(entity.getId(), entity.getLabel(), entity.getKind(),
                entity.getCompressible());
    }

    private static BankImportModel.BankImportRuleModel toRuleModel(BankRuleEntity entity) {
        return new BankImportModel.BankImportRuleModel(entity.getId(), entity.getMatchText(),
                entity.getCategoryId());
    }

    /** Public (plutôt que privé) : réutilisée par {@code JpaBankStore.findTransactions} (étape 4). */
    public static BankImportModel.BankTransactionModel toTransactionModel(BankTransactionEntity entity,
            List<BankTransactionSplitEntity> splits) {
        return new BankImportModel.BankTransactionModel(entity.getId(), entity.getTransactionDate(),
                entity.getLabel(), entity.getType(), entity.getAmount(), entity.getCategoryId(),
                splits.stream().map(BankRelationalMapper::toTransactionSplitModel).toList());
    }

    private static BankImportModel.BankTransactionSplitModel toTransactionSplitModel(
            BankTransactionSplitEntity entity) {
        return new BankImportModel.BankTransactionSplitModel(entity.getId(), entity.getCategoryId(),
                entity.getAmount(), entity.getLabel());
    }

    private static BankImportModel.PendingOperationModel toPendingOperationModel(BankPendingOperationEntity entity,
            List<BankPendingOperationSplitEntity> splits) {
        return new BankImportModel.PendingOperationModel(entity.getId(), entity.getOperationDate(),
                entity.getExpectedDate(), entity.getType(), entity.getRefNumber(), entity.getLabel(),
                entity.getAmount(), entity.getCategoryId(), entity.getStatus(), entity.getLinkedTxId(),
                entity.getClearedDate(), entity.getNotes(),
                // Memes champs (id/categoryId/amount/label) que pour une repartition de transaction :
                // meme modele BankTransactionSplitModel, reutilise ici pour les operations en attente.
                splits.stream()
                        .map(split -> new BankImportModel.BankTransactionSplitModel(split.getId(),
                                split.getCategoryId(), split.getAmount(), split.getLabel()))
                        .toList(),
                entity.getBudgetLineId());
    }

    private static BankImportModel.MatchingModel toMatchingModel(BankMatchingEntity matching,
            List<BankMatchingLinkEntity> links, Map<Long, List<BankMatchingLinkTxEntity>> txsByLinkId) {
        return new BankImportModel.MatchingModel(matching.getMonth(),
                links.stream()
                        .map(link -> new BankImportModel.MatchingLinkModel(link.getBudgetLineId(),
                                txsByLinkId.getOrDefault(link.getId(), List.of()).stream()
                                        .map(BankMatchingLinkTxEntity::getTxId)
                                        .toList()))
                        .toList());
    }

    // ------------------------------------------------------------------------------------------------
    // Écriture : modèle -> entités
    // ------------------------------------------------------------------------------------------------

    public static BankColumnMappingEntity toColumnMappingEntity(BankImportModel.BankColumnMappingModel mapping) {
        return new BankColumnMappingEntity(mapping.delimiter(), mapping.dateFormat(), mapping.hasHeader(),
                mapping.dateCol(), mapping.labelCol(), mapping.typeCol(), mapping.amountCol(),
                csvOfInts(mapping.extraLabelCols()), csvOfInts(mapping.creditCols()), csvOfInts(mapping.debitCols()));
    }

    public static List<BankCategoryEntity> toCategoryEntities(List<BankImportModel.CategoryModel> categories) {
        return categories.stream()
                .map(c -> new BankCategoryEntity(c.id(), c.label(), c.kind(), c.compressible()))
                .toList();
    }

    public static List<BankRuleEntity> toRuleEntities(List<BankImportModel.BankImportRuleModel> rules) {
        return rules.stream()
                .map(r -> new BankRuleEntity(r.id(), r.matchText(), blankToNull(r.categoryId())))
                .toList();
    }

    public static List<BankTransactionEntity> toTransactionEntities(
            List<BankImportModel.BankTransactionModel> transactions) {
        return transactions.stream()
                .map(tx -> new BankTransactionEntity(tx.id(), tx.date(), tx.label(), tx.type(), tx.amount(),
                        blankToNull(tx.categoryId())))
                .toList();
    }

    public static List<BankTransactionSplitEntity> toTransactionSplitEntities(
            List<BankImportModel.BankTransactionModel> transactions) {
        List<BankTransactionSplitEntity> result = new ArrayList<>();
        for (BankImportModel.BankTransactionModel tx : transactions) {
            for (BankImportModel.BankTransactionSplitModel split : tx.splits()) {
                result.add(new BankTransactionSplitEntity(split.id(), tx.id(), blankToNull(split.categoryId()),
                        split.amount(), split.label()));
            }
        }
        return result;
    }

    public static List<BankPendingOperationEntity> toPendingOperationEntities(
            List<BankImportModel.PendingOperationModel> pendingOperations) {
        return pendingOperations.stream()
                .map(op -> new BankPendingOperationEntity(op.id(), op.date(), op.expectedDate(), op.type(),
                        op.refNumber(), op.label(), op.amount(), blankToNull(op.categoryId()), op.status(),
                        op.linkedTxId(), op.clearedDate(), op.notes(), op.budgetLineId()))
                .toList();
    }

    public static List<BankPendingOperationSplitEntity> toPendingOperationSplitEntities(
            List<BankImportModel.PendingOperationModel> pendingOperations) {
        List<BankPendingOperationSplitEntity> result = new ArrayList<>();
        for (BankImportModel.PendingOperationModel op : pendingOperations) {
            for (BankImportModel.BankTransactionSplitModel split : op.splits()) {
                result.add(new BankPendingOperationSplitEntity(split.id(), op.id(), blankToNull(split.categoryId()),
                        split.amount(), split.label()));
            }
        }
        return result;
    }

    public static List<BankMatchingEntity> toMatchingEntities(List<BankImportModel.MatchingModel> matchings) {
        return matchings.stream().map(m -> new BankMatchingEntity(m.month())).toList();
    }

    // ------------------------------------------------------------------------------------------------
    // Utilitaires
    // ------------------------------------------------------------------------------------------------

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private static String csvOfInts(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private static List<Integer> parseCsvInts(String csv) {
        if (csv == null || csv.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                .map(Integer::parseInt).toList();
    }
}
