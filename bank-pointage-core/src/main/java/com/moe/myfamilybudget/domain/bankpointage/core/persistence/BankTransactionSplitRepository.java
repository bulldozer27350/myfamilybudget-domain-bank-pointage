package com.moe.myfamilybudget.domain.bankpointage.core.persistence;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository de `BankTransactionSplitEntity`, etape 3 de la migration relationnelle de l'import bancaire.
 *
 * <p>findByTransactionIdIn (etape 4) : pour une page de transactions (quelques dizaines/centaines), evite
 * de charger l'integralite de la table des repartitions comme le fait {@link JpaBankStore#getBankImport()}.
 */
@Repository
public interface BankTransactionSplitRepository extends JpaRepository<BankTransactionSplitEntity, String> {

    List<BankTransactionSplitEntity> findByTransactionIdIn(Collection<String> transactionIds);
}
