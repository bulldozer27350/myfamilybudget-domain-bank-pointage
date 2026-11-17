package com.moe.myfamilybudget.domain.bankpointage.core.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository de `BankTransactionEntity`, etape 3 de la migration relationnelle de l'import bancaire.
 *
 * <p>findByCategoryIdIsNull/NotNull (etape 4) : s'appuient sur l'index {@code bank_transaction_category_id_idx}
 * (changeSet {@code bank-reloc-core-4}) pour le filtrage par etat de categorisation de l'ecran de
 * categorisation, sans charger l'integralite de la table.
 */
@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransactionEntity, String> {

    Page<BankTransactionEntity> findByCategoryIdIsNull(Pageable pageable);

    Page<BankTransactionEntity> findByCategoryIdIsNotNull(Pageable pageable);
}
