# Release Notes — Silo Banque/Pointage (domain-bank-pointage)

Toutes les versions publiées de ce dépôt respectent la spécification [SemVer 2.0.0](https://semver.org/lang/fr/).

---

## [0.0.2-SNAPSHOT] - En cours

### Type de release
**Développement en cours** (reprise après la version initiale `0.0.1`, aucune release stable
produite depuis)

### Résumé
Ajout de la documentation d'exigences du silo Pointage Banque, sans aucun changement de code ni de
comportement métier : `bank-pointage-api` et `bank-pointage-core` restent identiques à la version
`0.0.1`.

### Changements par composant
- **`doc/srs.adoc`** (nouveau) : Software Requirements Specification du silo — 46 exigences
  fonctionnelles (`MFB-POIN-RF-001` à `MFB-POIN-RF-046`) et 14 exigences non fonctionnelles
  (`MFB-POIN-NRF-001` à `MFB-POIN-NRF-014`), chacune avec identifiant, titre, texte, note d'usage
  le cas échéant, méthode de vérification (Inspection / Analyse / Démonstration / Test) et
  traçabilité amont. Rédigé comme un fragment AsciiDoc sans titre de document, destiné à être
  concaténé (`include::`) au SRS consolidé de l'application MyFamilyBudget.
- **`doc/irs.adoc`** (nouveau) : Interface Requirements Specification spécifiant l'interaction
  entre l'application MyFamilyBudget et ce silo — nature différenciée de l'interface selon les
  trois services exposés, persistance JPA et verrouillage propres au silo, port
  `BankImportModifier` implémenté par l'application (sens inversé), événement
  `BankImportMutatedEvent`, intégration externe Enable Banking hors du périmètre applicatif, et
  règles de compatibilité (27 exigences `MFB-POIN-INT-001` à `MFB-POIN-INT-027`, chacune avec sa
  méthode de vérification). Même format de fragment AsciiDoc que `srs.adoc`, destiné à la même
  concaténation applicative.
- **`README.md`** : ajout d'un pointeur vers `doc/srs.adoc` et `doc/irs.adoc`.
- **`pom.xml`, `bank-pointage-api/pom.xml`, `bank-pointage-core/pom.xml`** : reprise du
  développement en continu après la release `0.0.1`, version bumpée vers `0.0.2-SNAPSHOT`
  conformément à l'étape 4 du processus de release documenté dans `README.md`.

### Rétro-compatibilité & Compatibilité binaire
Aucun changement de code : compatibilité binaire et fonctionnelle strictement identique à `0.0.1`.
Ce patch est documentaire et de gestion de version uniquement.

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
