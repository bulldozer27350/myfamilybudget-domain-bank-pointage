# Release Notes — Silo Banque/Pointage (domain-bank-pointage)

Toutes les versions publiées de ce dépôt respectent la spécification [SemVer 2.0.0](https://semver.org/lang/fr/).

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
