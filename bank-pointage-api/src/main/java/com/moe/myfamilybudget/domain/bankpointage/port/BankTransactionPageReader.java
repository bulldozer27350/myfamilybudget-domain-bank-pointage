package com.moe.myfamilybudget.domain.bankpointage.port;

import com.moe.myfamilybudget.domain.bankpointage.model.BankTransactionPage;

/**
 * Port de lecture paginée/filtrée des transactions bancaires, étape 4/5 de la migration relationnelle
 * (areas/bank-pointage-relational-migration.md, côté projet). Distinct de {@link BankReader#getBankImport()}
 * (qui reste un chargement complet, nécessaire pour l'import CSV, le calcul des règles, le rapprochement) :
 * ce port sert spécifiquement l'écran de catégorisation, qui n'a besoin de voir qu'une page de transactions
 * à la fois.
 */
public interface BankTransactionPageReader {

    /**
     * @param categorized {@code true} : transactions déjà catégorisées uniquement ; {@code false} : non
     *                    catégorisées uniquement ; {@code null} : toutes, sans filtre.
     * @param page        numéro de page, 0-based.
     * @param size        taille de page (nombre de transactions par page).
     */
    BankTransactionPage findTransactions(Boolean categorized, int page, int size);
}
