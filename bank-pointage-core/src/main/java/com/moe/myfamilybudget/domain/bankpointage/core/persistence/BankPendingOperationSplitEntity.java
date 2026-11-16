package com.moe.myfamilybudget.domain.bankpointage.core.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.data.domain.Persistable;

/** Répartition d'une opération en attente sur plusieurs catégories, étape 3 (bank-reloc-core-7). */
@Entity
@Table(name = "bank_pending_operation_split")
public class BankPendingOperationSplitEntity implements Persistable<String> {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "pending_operation_id")
    private String pendingOperationId;

    @Column(name = "category_id")
    private String categoryId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "label")
    private String label;

    public BankPendingOperationSplitEntity() {
    }

    public BankPendingOperationSplitEntity(String id, String pendingOperationId, String categoryId,
            BigDecimal amount, String label) {
        this.id = id;
        this.pendingOperationId = pendingOperationId;
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

    public String getPendingOperationId() {
        return pendingOperationId;
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
