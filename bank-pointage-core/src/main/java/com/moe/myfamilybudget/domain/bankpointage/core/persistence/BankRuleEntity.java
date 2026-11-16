package com.moe.myfamilybudget.domain.bankpointage.core.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.data.domain.Persistable;

/**
 * Règle de catégorisation automatique, étape 3 de la migration relationnelle (bank-reloc-core-3).
 *
 * <p>{@code categoryId} est {@code NULL} en base pour une règle sans catégorie assignée (le modèle Java
 * représente ce cas par une chaîne vide, jamais {@code null} : conversion faite par
 * {@link BankRelationalMapper}).
 */
@Entity
@Table(name = "bank_rule")
public class BankRuleEntity implements Persistable<String> {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "match_text")
    private String matchText;

    @Column(name = "category_id")
    private String categoryId;

    public BankRuleEntity() {
    }

    public BankRuleEntity(String id, String matchText, String categoryId) {
        this.id = id;
        this.matchText = matchText;
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

    public String getMatchText() {
        return matchText;
    }

    public String getCategoryId() {
        return categoryId;
    }
}
