package com.moe.myfamilybudget.domain.bankpointage.model;

import java.util.List;

/**
 * Page de transactions bancaires, étape 4/5 de la migration relationnelle de l'import bancaire
 * (areas/bank-pointage-relational-migration.md, côté projet) : permet à l'écran de catégorisation de ne
 * charger qu'une page de transactions (filtrée par état de catégorisation) plutôt que l'intégralité de
 * {@link BankImportModel#transactions()}.
 *
 * @param transactions   le contenu de la page, dans l'ordre (date décroissante)
 * @param page           numéro de page (0-based), tel que demandé
 * @param size           taille de page demandée (le dernier {@code transactions} peut être plus court)
 * @param totalElements  nombre total de transactions correspondant au filtre, toutes pages confondues
 * @param totalPages     nombre total de pages pour ce filtre et cette taille de page
 */
public record BankTransactionPage(
        List<BankImportModel.BankTransactionModel> transactions,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
