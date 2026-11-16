package com.moe.myfamilybudget.domain.bankpointage.core.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Transaction associée à un {@link BankMatchingLinkEntity}, étape 3 de la migration relationnelle
 * (bank-reloc-core-10, colonne {@code id} ajoutée par le correctif bank-reloc-core-12 : table de jointure
 * à l'origine sans clé primaire, nécessaire pour le mapping JPA).
 */
@Entity
@Table(name = "bank_matching_link_tx")
public class BankMatchingLinkTxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "matching_link_id")
    private Long matchingLinkId;

    @Column(name = "tx_id")
    private String txId;

    public BankMatchingLinkTxEntity() {
    }

    public BankMatchingLinkTxEntity(Long matchingLinkId, String txId) {
        this.matchingLinkId = matchingLinkId;
        this.txId = txId;
    }

    public Long getId() {
        return id;
    }

    public Long getMatchingLinkId() {
        return matchingLinkId;
    }

    public String getTxId() {
        return txId;
    }
}
