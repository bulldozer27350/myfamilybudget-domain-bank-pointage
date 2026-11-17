package com.moe.myfamilybudget.domain.bankpointage.core.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.data.domain.Persistable;

/** Mois de rapprochement bancaire ({@code AAAA-MM}), étape 3 de la migration relationnelle (bank-reloc-core-8). */
@Entity
@Table(name = "bank_matching")
public class BankMatchingEntity implements Persistable<String> {

    @Id
    @Column(name = "matching_month")
    private String month;

    public BankMatchingEntity() {
    }

    public BankMatchingEntity(String month) {
        this.month = month;
    }

    public String getMonth() {
        return month;
    }

    /** Identifiant {@link Persistable} : {@code month} est la clé primaire de cette entité. */
    @Override
    public String getId() {
        return month;
    }

    /** Toujours {@code true} : voir la note de {@link BankCategoryEntity} sur {@link Persistable}. */
    @Override
    public boolean isNew() {
        return true;
    }
}
