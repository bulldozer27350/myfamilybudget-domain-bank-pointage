package com.moe.myfamilybudget.domain.bankpointage.core.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.data.domain.Persistable;

/**
 * Transaction bancaire, étape 3 de la migration relationnelle (bank-reloc-core-4). Index sur
 * {@code category_id} et {@code transaction_date} (étape 4 : lecture paginée/filtrée de l'écran de
 * catégorisation).
 *
 * <p>Pas de relation JPA {@code @OneToMany} vers {@link BankTransactionSplitEntity} : la lecture
 * ({@link JpaBankStore#getBankImport()}) charge systématiquement l'intégralité des deux tables et les
 * recompose en mémoire ({@link BankRelationalMapper}), ce qui évite tout risque de requêtes N+1.
 */
@Entity
@Table(name = "bank_transaction")
public class BankTransactionEntity implements Persistable<String> {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "transaction_date")
    private String transactionDate;

    @Column(name = "label")
    private String label;

    @Column(name = "type")
    private String type;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "category_id")
    private String categoryId;

    public BankTransactionEntity() {
    }

    public BankTransactionEntity(String id, String transactionDate, String label, String type,
            BigDecimal amount, String categoryId) {
        this.id = id;
        this.transactionDate = transactionDate;
        this.label = label;
        this.type = type;
        this.amount = amount;
        this.categoryId = categoryId;
    }

    public String getId() {
        return id;
    }

    /** Toujours {@code true} : voir la note de {@link BankCategoryEntity} sur {@link Persistable}. */
    @Override
    public boolean isNew() {
        return true;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public String getLabel() {
        return label;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCategoryId() {
        return categoryId;
    }
}
