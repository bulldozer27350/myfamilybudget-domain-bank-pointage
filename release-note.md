# Release Notes — Silo Banque/Pointage (domain-bank-pointage)

Toutes les versions publiées de ce dépôt respectent la spécification [SemVer 2.0.0](https://semver.org/lang/fr/).

---

## [0.1.0] - 2026-11-22

### Type de release
**MINEURE** (nouvelles capacités rétro-compatibles : persistance relationnelle de l'import
bancaire, pagination serveur des transactions, import CSV multi-colonnes)

### Résumé
Documentation d'exigences (SRS/IRS) déjà publiée en `0.0.2-SNAPSHOT`, suivie de la bascule
complète du stockage de l'import bancaire d'un document JSON unique (`bank_import_document`)
vers un schéma relationnel (étapes 1 à 4 du plan de migration), de corrections de compatibilité
H2/PostgreSQL découvertes lors de cette bascule, et d'un assouplissement de l'import CSV
(libellé et montant répartis sur plusieurs colonnes). Aucun changement de contrat observable
pour les appelants existants (`BankReader`/`BankWriter`/`BankSnapshotWriter` inchangés) ; le
nouveau port `BankTransactionPageReader` est une addition pure.

### Changements par composant
- **`doc/srs.adoc`, `doc/irs.adoc`, `README.md`** : documentation d'exigences du silo (46 RF,
  14 NRF, 27 exigences d'interface), déjà livrée en `0.0.2-SNAPSHOT`, sans changement de code.
- **Import CSV (`bank-pointage-api`/`bank-pointage-core`)** : `BankImportModel.BankColumnMappingModel`
  gagne `extraLabelCols`, `creditCols`, `debitCols` (additif, constructeur de compatibilité
  conservé) ; `DefaultBankImportCalculationService.importTransactions()` concatène désormais
  toutes les colonnes de rôle `label` et agrège Montant et/ou Crédit/Débit, au lieu de ne
  retenir que la première colonne de chaque rôle. Validation assouplie : accepte Montant OU
  Crédit/Débit. Message d'erreur de validation inchangé (rétro-compatibilité du test existant).
- **Schéma relationnel de l'import bancaire (`bank-pointage-core`)** :
  - Étape 1 : nouvelles tables Liquibase vides (`bank_column_mapping`, `bank_category`,
    `bank_rule`, `bank_transaction`/`bank_transaction_split`, `bank_pending_operation`/`_split`,
    `bank_matching`/`_link`/`_link_tx`), sans impact sur le code applicatif.
  - Étape 2 : changeSet `bank-reloc-core-11` déclenchant la copie unique des données existantes
    de `bank_import_document` vers les nouvelles tables (implémentation `CustomTaskChange` côté
    dépôt `MyFamilyBudget`, seul module dépendant à la fois de `liquibase-core` et de ce silo).
  - Étape 3 : `JpaBankStore` lit et écrit désormais directement les tables relationnelles (10
    entités JPA + repositories, `BankRelationalMapper`) au lieu du document JSON unique, à
    contrat `BankReader`/`BankWriter`/`BankSnapshotWriter` strictement inchangé ;
    `bank_import_document` n'est plus lue ni écrite depuis ce silo (conservée en base comme
    copie de secours).
  - Étape 4 : nouveau port `BankTransactionPageReader` (addition) pour la lecture paginée et
    filtrée (catégorisé/non catégorisé) des transactions, sans charger la table entière.
  - Corrections découvertes en cours de bascule : nom de colonne SQL `month` renommé en
    `matching_month` (mot réservé provoquant une erreur de démarrage sous H2), ajout d'une clé
    primaire auto-incrémentée sur `bank_matching_link_tx` (oubli de l'étape 1), et flush/clear
    de l'`EntityManager` dans `JpaBankStore` entre la purge et la réinsertion des tables pour
    éviter des collisions de session Hibernate.
- **`pom.xml`, `bank-pointage-api/pom.xml`, `bank-pointage-core/pom.xml`** : version stable
  `0.1.0` (plus de suffixe `-SNAPSHOT`).

### Rétro-compatibilité & Compatibilité binaire
- Aucun changement de contrat observable : `BankReader`, `BankWriter`, `BankSnapshotWriter`
  inchangés ; `BankTransactionPageReader` est une addition pure.
- `bank_import_document` reste intacte en base (copie de secours) jusqu'à l'étape 5 (suppression),
  non incluse dans cette version.
- Rétro-compatibilité binaire garantie dans la plage `[0.1.0, 0.2.0)`.

---

## [0.0.1] - 2026-11-07

### Type de release
**Version initiale** (mise en place et validation de la structure autonome multi-dépôts)

### Résumé
Première version stable du silo Banque/Pointage (`domain-bank-pointage`), regroupant `bank-pointage-api` et `bank-pointage-core` avec import du BOM centralisé, cycle de build autonome et configuration de déploiement GitHub Packages.

### Changements par composant
- **`bank-pointage-api`** : contrats, ports, interfaces de service (import bancaire, pointage) (aucun changement métier).
- **`bank-pointage-core`** : moteurs de calcul purs, intégration Enable Banking sans Spring, et persistance JPA du document d'import bancaire.

### Rétro-compatibilité & Compatibilité binaire
Non applicable : version initiale, aucune version antérieure publiée.
