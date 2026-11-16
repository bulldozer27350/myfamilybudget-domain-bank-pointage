package com.moe.myfamilybudget.domain.bankpointage.core.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.data.domain.Persistable;

/** Répartition d'une transaction bancaire sur plusieurs catégories, étape 3 (bank-reloc-core-5). */
@Entity
@Table(name = "bank_transaction_split")
public class BankTransactionSplitEntity implements Persistable<String> {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "category_id")
    private String categoryId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "label")
    private String label;

    public BankTransactionSplitEntity() {
    }

    public BankTransactionSplitEntity(String id, String transactionId, String categoryId, BigDecimal amount,
            String label) {
        this.id = id;
        this.transactionId = transactionId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    /** Toujours {@code true} : voir la note de {@link BankCategoryEntity} sur {@link Persistable}. */
    @Override
    public boolean isNew() {
        return true;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getLabel() {
        return label;
    }
}
