package com.moe.myfamilybudget.domain.bankpointage.core.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository de `BankTransactionSplitEntity`, etape 3 de la migration relationnelle de l'import bancaire. */
@Repository
public interface BankTransactionSplitRepository extends JpaRepository<BankTransactionSplitEntity, String> {
}
