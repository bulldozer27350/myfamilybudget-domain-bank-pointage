package com.moe.myfamilybudget.domain.bankpointage.core.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.data.domain.Persistable;

/**
 * Configuration de mapping des colonnes CSV, ligne unique (id fixe {@code 1}), etape 3 de la migration
 * relationnelle de l'import bancaire (areas/bank-pointage-relational-migration.md, cote projet).
 *
 * <p>Les listes de positions de colonnes (libelle/credit/debit complementaires) sont stockees en CSV
 * d'entiers (ex. {@code "2,4"}) plutot que dans des tables de jointure : config a une seule ligne, pas
 * besoin de requetage SQL dessus (voir commentaire du changeSet {@code bank-reloc-core-1}).
 *
 * <p>{@link Persistable} avec {@link #isNew()} toujours {@code true} : identifiant fixe (jamais {@code null}),
 * voir la note de {@link BankCategoryEntity}.
 */
@Entity
@Table(name = "bank_column_mapping")
public class BankColumnMappingEntity implements Persistable<Long> {

    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(name = "delimiter")
    private String delimiter;

    @Column(name = "date_format")
    private String dateFormat;

    @Column(name = "has_header")
    private Boolean hasHeader;

    @Column(name = "date_col")
    private Integer dateCol;

    @Column(name = "label_col")
    private Integer labelCol;

    @Column(name = "type_col")
    private Integer typeCol;

    @Column(name = "amount_col")
    private Integer amountCol;

    @Column(name = "extra_label_cols_csv")
    private String extraLabelColsCsv;

    @Column(name = "credit_cols_csv")
    private String creditColsCsv;

    @Column(name = "debit_cols_csv")
    private String debitColsCsv;

    public BankColumnMappingEntity() {
    }

    public BankColumnMappingEntity(String delimiter, String dateFormat, Boolean hasHeader, Integer dateCol,
            Integer labelCol, Integer typeCol, Integer amountCol, String extraLabelColsCsv,
            String creditColsCsv, String debitColsCsv) {
        this.id = SINGLETON_ID;
        this.delimiter = delimiter;
        this.dateFormat = dateFormat;
        this.hasHeader = hasHeader;
        this.dateCol = dateCol;
        this.labelCol = labelCol;
        this.typeCol = typeCol;
        this.amountCol = amountCol;
        this.extraLabelColsCsv = extraLabelColsCsv;
        this.creditColsCsv = creditColsCsv;
        this.debitColsCsv = debitColsCsv;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return true;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public Boolean getHasHeader() {
        return hasHeader;
    }

    public Integer getDateCol() {
        return dateCol;
    }

    public Integer getLabelCol() {
        return labelCol;
    }

    public Integer getTypeCol() {
        return typeCol;
    }

    public Integer getAmountCol() {
        return amountCol;
    }

    public String getExtraLabelColsCsv() {
        return extraLabelColsCsv;
    }

    public String getCreditColsCsv() {
        return creditColsCsv;
    }

    public String getDebitColsCsv() {
        return debitColsCsv;
    }
}
