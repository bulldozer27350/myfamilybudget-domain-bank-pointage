package com.moe.myfamilybudget.domain.bankpointage.core.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Lien de rapprochement (une ligne de budget associée à un ensemble de transactions), étape 3 de la
 * migration relationnelle (bank-reloc-core-9). Identifiant généré : nécessaire avant de pouvoir créer les
 * lignes {@link BankMatchingLinkTxEntity} correspondantes.
 */
@Entity
@Table(name = "bank_matching_link")
public class BankMatchingLinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "month")
    private String month;

    @Column(name = "budget_line_id")
    private String budgetLineId;

    public BankMatchingLinkEntity() {
    }

    public BankMatchingLinkEntity(String month, String budgetLineId) {
        this.month = month;
        this.budgetLineId = budgetLineId;
    }

    public Long getId() {
        return id;
    }

    public String getMonth() {
        return month;
    }

    public String getBudgetLineId() {
        return budgetLineId;
    }
}
