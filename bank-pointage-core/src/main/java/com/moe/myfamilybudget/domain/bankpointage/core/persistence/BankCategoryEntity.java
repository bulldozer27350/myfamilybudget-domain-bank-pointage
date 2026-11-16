package com.moe.myfamilybudget.domain.bankpointage.core.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.data.domain.Persistable;

/**
 * Catégorie de l'import bancaire, étape 3 de la migration relationnelle (bank-reloc-core-2).
 *
 * <p>{@link Persistable} avec {@link #isNew()} toujours {@code true} : identifiant assigné (pas généré par
 * la base), et {@link JpaBankStore} ne fait jamais de mise à jour en place (toujours un {@code deleteAllInBatch}
 * puis une réinsertion complète) — sans cela, {@code JpaRepository.save}/{@code saveAll} ferait un
 * {@code SELECT} avant chaque {@code INSERT} pour verifier l'existence de la ligne (comportement par défaut
 * de Spring Data JPA pour un identifiant assigné), inutile ici et couteux sur un volume de transactions
 * important — exactement le type de cout qu'optimise cette migration.
 */
@Entity
@Table(name = "bank_category")
public class BankCategoryEntity implements Persistable<String> {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "label")
    private String label;

    @Column(name = "kind")
    private String kind;

    @Column(name = "compressible")
    private String compressible;

    public BankCategoryEntity() {
    }

    public BankCategoryEntity(String id, String label, String kind, String compressible) {
        this.id = id;
        this.label = label;
        this.kind = kind;
        this.compressible = compressible;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return true;
    }

    public String getLabel() {
        return label;
    }

    public String getKind() {
        return kind;
    }

    public String getCompressible() {
        return compressible;
    }
}
