package com.moe.myfamilybudget.domain.bankpointage.core.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.data.domain.Persistable;

/** Opération en attente (chèque émis, CB différée...), étape 3 de la migration relationnelle (bank-reloc-core-6). */
@Entity
@Table(name = "bank_pending_operation")
public class BankPendingOperationEntity implements Persistable<String> {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "operation_date")
    private String operationDate;

    @Column(name = "expected_date")
    private String expectedDate;

    @Column(name = "type")
    private String type;

    @Column(name = "ref_number")
    private String refNumber;

    @Column(name = "label")
    private String label;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "category_id")
    private String categoryId;

    @Column(name = "status")
    private String status;

    @Column(name = "linked_tx_id")
    private String linkedTxId;

    @Column(name = "cleared_date")
    private String clearedDate;

    @Column(name = "notes")
    private String notes;

    @Column(name = "budget_line_id")
    private String budgetLineId;

    public BankPendingOperationEntity() {
    }

    public BankPendingOperationEntity(String id, String operationDate, String expectedDate, String type,
            String refNumber, String label, BigDecimal amount, String categoryId, String status,
            String linkedTxId, String clearedDate, String notes, String budgetLineId) {
        this.id = id;
        this.operationDate = operationDate;
        this.expectedDate = expectedDate;
        this.type = type;
        this.refNumber = refNumber;
        this.label = label;
        this.amount = amount;
        this.categoryId = categoryId;
        this.status = status;
        this.linkedTxId = linkedTxId;
        this.clearedDate = clearedDate;
        this.notes = notes;
        this.budgetLineId = budgetLineId;
    }

    public String getId() {
        return id;
    }

    /** Toujours {@code true} : voir la note de {@link BankCategoryEntity} sur {@link Persistable}. */
    @Override
    public boolean isNew() {
        return true;
    }

    public String getOperationDate() {
        return operationDate;
    }

    public String getExpectedDate() {
        return expectedDate;
    }

    public String getType() {
        return type;
    }

    public String getRefNumber() {
        return refNumber;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getStatus() {
        return status;
    }

    public String getLinkedTxId() {
        return linkedTxId;
    }

    public String getClearedDate() {
        return clearedDate;
    }

    public String getNotes() {
        return notes;
    }

    public String getBudgetLineId() {
        return budgetLineId;
    }
}
