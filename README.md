# SIDRA — Plateforme e-commerce full-stack

**SIDRA** est une plateforme e-commerce développée avec Java, Spring Boot, Vue 3, TypeScript et PostgreSQL, destinée à la vente en ligne de parfums, de cosmétiques et de produits lifestyle en France et dans l'Union européenne.

Le projet couvre les principaux processus d'une boutique en ligne : catalogue, authentification, panier, commandes, paiement Stripe, facturation, livraison, TVA, gestion des stocks, remboursements, administration et intégration de fournisseurs.

Il s'agit d'un projet personnel orienté **développement logiciel full-stack, conception d'API REST et architecture backend**, conçu pour évoluer progressivement vers une exploitation commerciale réelle.

## Sommaire

* [Présentation](#présentation)
* [Architecture générale](#architecture-générale)
* [Fonctionnalités](#fonctionnalités)
* [Architecture technique](#architecture-technique)
* [Sécurité](#sécurité)
* [Tests](#tests)
* [Structure du projet](#structure-du-projet)
* [Installation et démarrage](#installation-et-démarrage)
* [Configuration](#configuration)
* [Choix d'architecture](#choix-darchitecture)
* [Limites et évolutions](#limites-et-évolutions)
* [Objectifs techniques](#objectifs-techniques)
* [Auteur](#auteur)

---

## Présentation

SIDRA repose sur une architecture séparant clairement les différentes responsabilités de l'application :

* **Boutique en ligne** : interface destinée aux clients.
* **Back-office administrateur** : interface de gestion de la boutique.
* **Backend REST** : API centralisant la logique métier.
* **Base de données PostgreSQL** : stockage des données persistantes.
* **Services externes** : paiement, e-mails, stockage de fichiers et fournisseurs.

Le backend est organisé par domaines métier afin de faciliter la maintenance, les évolutions et l'intégration de nouveaux services.

### Architecture générale

```text
                   ┌─────────────────────────┐
                   │       SIDRA SHOP        │
                   │    Vue 3 / TypeScript   │
                   └────────────┬────────────┘
                                │
                                │ REST / JSON
                                ▼
┌────────────────────────────────────────────────────────┐
│                    SIDRA BACKEND                       │
│                                                        │
│ Identity  Catalog  Cart  Order  Payment  Shipping      │
│ Invoice   Supplier  Admin  Storage  Mail  Legal        │
└─────────────────────────┬──────────────────────────────┘
                          │
                ┌─────────┴──────────┐
                ▼                    ▼
      ┌──────────────────┐  ┌──────────────────────┐
      │   PostgreSQL     │  │  Services externes   │
      │                  │  │                      │
      │ Données métier   │  │ Stripe               │
      │ Transactions     │  │ E-mails              │
      │ Migrations       │  │ Fournisseurs         │
      └──────────────────┘  │ Stockage de fichiers │
                            └──────────────────────┘

                   ┌─────────────────────────┐
                   │      SIDRA ADMIN        │
                   │    Vue 3 / TypeScript   │
                   └────────────┬────────────┘
                                │
                                └── REST / JSON
                                    vers le backend
```

Les deux applications frontend utilisent la même API. Les règles métier et les opérations sensibles sont centralisées côté serveur.

---

## Fonctionnalités

### 1. Authentification et comptes clients

Le module d'identité gère les comptes utilisateurs et les mécanismes d'authentification.

Fonctionnalités :

* Inscription et connexion.
* Authentification par JWT.
* Gestion du profil utilisateur.
* Gestion des adresses.
* Vérification de l'adresse e-mail.
* Renvoi du lien de vérification.
* Mot de passe oublié et réinitialisation.
* Invalidation des sessions après un changement de mot de passe.
* Gestion des rôles et contrôle des accès administrateur.

Les opérations liées aux comptes sont traitées par le backend, qui contrôle les autorisations avant d'accéder aux ressources protégées.

### 2. Catalogue produits

Le catalogue centralise les informations commerciales et les données nécessaires à la présentation des produits.

Il prend notamment en charge :

* Produits, catégories et marques.
* Images multiples.
* Notes olfactives.
* Prix et disponibilité.
* Références produits.
* Variantes de produits.
* Gestion du stock.
* Recherche côté serveur.
* Pagination.
* Filtres combinables.
* Tri des résultats.

Les variantes permettent de représenter plusieurs contenances d'un même parfum avec des références, des prix et des stocks indépendants.

Exemple :

```text
Parfum Yara
├── Variante 30 ml
├── Variante 50 ml
└── Variante 100 ml
```

Chaque variante peut être gérée indépendamment dans le catalogue et les règles de disponibilité.

### 3. Panier

Le panier permet au client de préparer sa commande avant de passer au paiement.

Fonctionnalités :

* Ajout d'articles.
* Sélection d'une variante.
* Modification des quantités.
* Suppression d'articles.
* Calcul du sous-total.
* Calcul des frais de livraison.
* Calcul de la TVA.
* Calcul du montant total.
* Vérification de la disponibilité des produits.

Les prix et les montants financiers sont recalculés côté serveur. Le backend ne fait pas confiance aux montants transmis par le frontend.

Cette approche limite les risques de manipulation des prix ou des totaux de commande.

### 4. Checkout et gestion des commandes

Le processus de commande prend en charge :

* Commandes avec compte client.
* Commandes invitées.
* Adresse de livraison.
* Adresse de facturation.
* Validation serveur des données de commande.
* Gestion des destinations de livraison configurées.
* Calcul serveur des frais de livraison.
* Calcul de la TVA.
* Gestion des prix TTC.
* Enregistrement des lignes de commande.
* Confirmation du paiement.
* Traitement des commandes par l'administration.

#### Conservation de l'historique des commandes

Les informations importantes utilisées lors de la commande sont enregistrées dans celle-ci afin de préserver l'historique commercial.

Par exemple, l'adresse enregistrée dans une commande ne doit pas être remplacée automatiquement lorsque le client modifie son adresse dans son profil.

Le même principe s'applique aux prix, aux frais de livraison et aux informations fiscales enregistrés au moment de l'achat.

### 5. Paiement Stripe

Le paiement en ligne est intégré à l'aide de **Stripe Checkout**.

Le backend distingue la redirection du client après paiement de la confirmation effective du règlement.

Le flux de traitement repose sur les événements Stripe reçus par webhook.

```text
Client
  │
  ▼
SIDRA Shop
  │
  ▼
Backend Spring Boot
  │
  ▼
Stripe Checkout
  │
  ▼
Paiement
  │
  ▼
Événement Stripe
  │
  ▼
Webhook SIDRA
  │
  ├── Vérification de l'événement
  ├── Mise à jour du paiement
  ├── Confirmation de la commande
  ├── Traitement du stock
  ├── Génération de la facture
  └── Notification du client
```

La confirmation du paiement est traitée côté serveur. Une simple redirection du navigateur vers une page de succès ne constitue pas une preuve de paiement.

Le traitement des événements doit également tenir compte des notifications répétées afin d'éviter les opérations en double.

### 6. Frais de livraison

Les frais de livraison sont calculés par le backend à partir des règles configurées.

Les critères peuvent notamment inclure :

* Pays de destination.
* Zone de livraison.
* Montant de la commande.
* Poids des produits, lorsqu'il est disponible.
* Seuil de gratuité configuré.

Le montant retenu est enregistré dans la commande afin de conserver l'historique du calcul.

Le calcul des frais de livraison est indépendant du mode d'approvisionnement choisi pour les produits.

### 7. TVA et montants commerciaux

Les prix affichés au client sont destinés à être exprimés TTC.

Le backend distingue les montants hors taxes, la TVA et les montants TTC.

```text
Montant de la commande
          │
          ├── Montant HT
          │
          ├── TVA applicable
          │
          └── Montant TTC
```

Les informations fiscales utilisées lors de la commande sont enregistrées avec celle-ci afin de préserver l'historique des transactions.

Le modèle est conçu pour prendre en charge des règles fiscales différentes selon les destinations, sous réserve de la configuration et de la validation des règles applicables.

### 8. Facturation

Le module de facturation permet de produire et de consulter les factures associées aux commandes payées.

Fonctionnalités prévues dans le module :

* Numérotation des factures.
* Date de facture.
* Identité du vendeur.
* Identité du client.
* Adresse de facturation.
* Détail des produits.
* Quantités et prix unitaires.
* Montants HT.
* Détail de la TVA.
* Montant TTC.
* Frais de livraison.
* Génération de PDF.
* Téléchargement par le client.
* Consultation depuis le back-office.

Une facture émise constitue un document historique. Les modifications ultérieures des données du catalogue ou du profil client ne doivent pas modifier rétroactivement son contenu.

### 9. Remboursements

Le back-office permet de gérer les remboursements totaux et partiels.

Le processus de remboursement comprend notamment :

* Appel à l'API Stripe.
* Enregistrement du montant remboursé.
* Date du remboursement.
* Motif de l'opération.
* Identification de l'administrateur à l'origine de l'action.
* Notification du client.
* Traitement associé du stock, selon les règles applicables.

Les opérations doivent conserver une trace permettant de rapprocher le remboursement de la commande et du paiement d'origine.

La restauration du stock doit être adaptée au type de remboursement et à la situation réelle du produit : un remboursement financier ne signifie pas systématiquement qu'un produit physique est retourné et revendable.

### 10. Gestion des stocks

Le stock est géré côté serveur et associé aux produits ou à leurs variantes.

Fonctionnalités :

* Consultation du stock disponible.
* Mise à jour du stock lors du traitement des commandes.
* Blocage des ventes lorsque le stock propre est insuffisant.
* Seuil d'alerte configurable.
* Historique des mouvements.
* Entrées et sorties de stock.
* Corrections manuelles.
* Gestion des retours.

Les mouvements de stock doivent être cohérents avec les opérations de commande et de remboursement afin de limiter les écarts entre le stock enregistré et le stock réel.

### 11. Approvisionnement hybride : stock propre et dropshipping

SIDRA prévoit deux modes d'approvisionnement : le stock propre et le dropshipping.

```text
                   Commande payée
                         │
                         ▼
                Traitement manuel
                         │
               ┌─────────┴─────────┐
               │                   │
               ▼                   ▼
           OWN_STOCK             DROPSHIP
               │                   │
               ▼                   ▼
       Stock de SIDRA       Fournisseur externe
               │                   │
               ▼                   ▼
        Préparation         Préparation fournisseur
               │                   │
               ▼                   ▼
        Expédition client   Expédition client
```

#### OWN_STOCK

Le produit est disponible dans le stock propre de SIDRA.

```text
SIDRA
  │
  ▼
Préparation de la commande
  │
  ▼
Expédition
  │
  ▼
Client
```

#### DROPSHIP

Le produit est approvisionné auprès d'un fournisseur qui prend en charge la préparation et l'expédition directe au client, lorsque ce service est effectivement proposé par le fournisseur.

```text
SIDRA
  │
  ▼
Transmission de la commande
  │
  ▼
Fournisseur
  │
  ▼
Préparation et expédition
  │
  ▼
Client
```

Le choix de la source d'approvisionnement est réalisé manuellement après paiement.

Le statut `PAID` correspond à une commande payée qui attend son traitement. Le passage à `PREPARING` indique que la commande a été prise en charge.

Le mode dropshipping ne garantit pas, à lui seul, qu'un fournisseur accepte une commande ou expédie directement au client : ces capacités dépendent du partenaire intégré.

### 12. Intégration des fournisseurs

Le backend utilise une architecture d'adaptateurs afin de séparer la logique métier de SIDRA des API propres à chaque fournisseur.

```text
               SupplierService
                      │
                      ▼
               SupplierAdapter
                      │
              ┌───────┴────────┐
              │                │
              ▼                ▼
       Supplier A        Supplier B
         Adapter            Adapter
```

Selon les capacités de chaque fournisseur, les adaptateurs peuvent prendre en charge :

* Authentification à l'API.
* Récupération des produits.
* Import du catalogue.
* Synchronisation des prix.
* Synchronisation des stocks.
* Transmission des commandes.
* Récupération des informations de suivi.
* Gestion des erreurs et des réponses fournisseur.

Cette abstraction facilite l'ajout de nouveaux partenaires sans coupler directement le cœur métier à une API spécifique.

Les fonctionnalités réellement disponibles dépendent des opérations prises en charge par chaque fournisseur et de la configuration de son intégration.

### 13. Favoris

Le module de favoris permet aux utilisateurs connectés d'enregistrer des produits pour les retrouver plus tard.

Fonctionnalités :

* Ajout aux favoris.
* Suppression des favoris.
* Prévention des doublons pour un même produit.
* Conservation des références de produits devenus indisponibles.
* Affichage de l'indisponibilité.
* Association des favoris au compte utilisateur.

### 14. E-mails transactionnels

Le backend centralise les e-mails liés aux principales opérations de la boutique.

Les notifications peuvent notamment concerner :

* Vérification de l'adresse e-mail.
* Réinitialisation du mot de passe.
* Confirmation de commande.
* Informations sur le traitement de la commande.
* Remboursement.
* Transmission de documents ou d'informations utiles au client.

Les modèles d'e-mails sont regroupés dans les ressources du backend.

```text
backend/
└── src/
    └── main/
        └── resources/
            └── templates/
                ├── invoice/
                └── mail/
```

L'envoi effectif des e-mails dépend de la configuration du service de messagerie.

### 15. Données personnelles et RGPD

La plateforme prévoit des mécanismes de gestion des données personnelles, notamment :

* Gestion du consentement marketing.
* Accès aux données du compte.
* Export des données personnelles.
* Suppression du compte.
* Contrôle des accès.
* Gestion de la conservation des données.
* Protection des informations liées aux commandes.

La suppression d'un compte ne signifie pas nécessairement la suppression immédiate de toutes les données associées. Certaines informations peuvent devoir être conservées pendant les durées légales applicables, notamment pour les obligations comptables.

La conformité effective dépend également des procédures opérationnelles, des durées de conservation configurées, des traitements des prestataires et des documents d'information destinés aux utilisateurs.

### 16. Pages et contenus légaux

L'API permet de servir les contenus légaux de la plateforme, notamment :

* Mentions légales.
* Conditions générales de vente.
* Politique de confidentialité.
* Politique de cookies.
* Politique de retour.
* Informations relatives au droit de rétractation.

Ces contenus doivent être maintenus à jour en fonction des activités réellement exercées et des obligations applicables à SIDRA.

---

## Architecture technique

### Backend

| Technologie     | Utilisation                          |
| --------------- | ------------------------------------ |
| Java 21         | Langage de programmation             |
| Spring Boot     | Développement de l'API backend       |
| Spring Security | Authentification et autorisations    |
| JWT             | Authentification par jeton           |
| Spring Data     | Accès aux données                    |
| PostgreSQL      | Base de données relationnelle        |
| Flyway          | Versionnement des migrations SQL     |
| Maven           | Gestion du build et des dépendances  |
| Stripe          | Paiement et remboursements           |
| Docker          | Environnement de développement       |
| REST / JSON     | Communication entre les applications |

### Frontend

| Technologie | Utilisation                       |
| ----------- | --------------------------------- |
| Vue 3       | Interfaces utilisateur            |
| TypeScript  | Typage statique                   |
| Pinia       | Gestion de l'état applicatif      |
| Vite        | Serveur de développement et build |
| CSS         | Présentation et responsive design |

Deux applications frontend distinctes sont développées :

* **Shop** : interface de la boutique destinée aux clients.
* **Admin** : interface de gestion destinée aux administrateurs.

### Infrastructure et services

* PostgreSQL.
* Docker et Docker Compose.
* Stripe Checkout et webhooks.
* Service de messagerie.
* Stockage des fichiers.
* API des fournisseurs.
* Git et GitHub pour le versionnement du code.

---

## Sécurité

La sécurité est principalement prise en charge par le backend.

Les mécanismes prévus comprennent :

* Authentification JWT.
* Contrôle des rôles et autorisations.
* Protection des endpoints administrateurs.
* Validation des données reçues.
* Vérification de la propriété des ressources.
* Gestion centralisée des erreurs.
* Limitation des tentatives de connexion.
* Limitation du nombre de requêtes.
* Verrouillage temporaire après plusieurs échecs d'authentification.
* Journalisation des opérations administratives sensibles.
* Externalisation des secrets et des paramètres sensibles.

Les données financières et les règles métier critiques ne doivent pas dépendre uniquement des contrôles réalisés dans le navigateur.

Les clés secrètes, mots de passe et jetons d'accès ne doivent pas être versionnés dans le dépôt Git.

---

## Organisation du backend

Le backend est organisé par domaines métier afin de séparer les responsabilités.

```text
com.parfum.ecommerce
├── admin
├── cart
├── catalog
├── common
├── home
├── identity
├── invoice
├── legal
├── mail
├── order
├── payment
├── shipping
├── storage
└── supplier
    ├── adapters
    └── dto
```

Cette organisation permet de regrouper les règles métier liées à un même domaine tout en limitant les dépendances entre les modules.

Le package Java conserve son nom technique `com.parfum.ecommerce` : il s'agit du nom de l'espace de noms du code, et non du nom commercial de la boutique.

---

## Base de données et migrations

PostgreSQL constitue le stockage relationnel principal de SIDRA.

Les évolutions du schéma sont versionnées avec Flyway.

```text
Application Spring Boot
          │
          ▼
      Spring Data
          │
          ▼
      PostgreSQL
          ▲
          │
        Flyway
```

Les migrations permettent de versionner la structure de la base de données et de reproduire les évolutions du schéma dans les environnements concernés.

Les données persistantes comprennent notamment les informations relatives aux utilisateurs, aux produits, aux commandes, aux paiements, aux stocks et aux factures.

---

## API REST

Le backend expose une API REST consommée par les deux applications frontend.

```text
┌─────────────────────┐
│     SIDRA SHOP      │
│  Vue 3 / TypeScript │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│                     │
│   SIDRA BACKEND     │
│   Spring Boot API   │
│                     │
└──────────▲──────────┘
           │
           ▲
┌──────────┴──────────┐
│     SIDRA ADMIN     │
│  Vue 3 / TypeScript │
└─────────────────────┘
```

Les endpoints sont organisés autour des domaines métier :

* Identité et comptes.
* Catalogue.
* Panier.
* Commandes.
* Paiement.
* Livraison.
* Facturation.
* Favoris.
* Administration.
* Fournisseurs.
* Contenus légaux.

Les autorisations et validations nécessaires sont réalisées côté serveur.

---

## Structure du projet

```text
plateforme-ecommerce/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/parfum/ecommerce/
│   │   │   └── resources/
│   │   │       ├── db/migration/
│   │   │       ├── static/images/
│   │   │       └── templates/
│   │   └── test/
│   └── pom.xml
│
├── frontend/
│   ├── shop/
│   │   └── src/
│   │       ├── app/
│   │       ├── components/
│   │       ├── composables/
│   │       ├── layouts/
│   │       ├── pages/
│   │       ├── services/
│   │       ├── stores/
│   │       ├── styles/
│   │       ├── types/
│   │       └── utils/
│   │
│   └── admin/
│       └── src/
│           ├── components/
│           ├── router/
│           ├── services/
│           ├── stores/
│           └── views/
│
├── infra/
├── docs/
└── README.md
```

Cette structure distingue le backend, les deux interfaces frontend, les ressources d'infrastructure et la documentation.

---

## Tests

Les tests automatisés permettent de vérifier les comportements importants et de limiter les régressions lors des évolutions du projet.

Les scénarios concernés comprennent notamment :

* Inscription et connexion.
* Authentification et autorisations.
* Gestion du panier.
* Calcul des montants.
* Checkout.
* Confirmation du paiement.
* Traitement des webhooks Stripe.
* Gestion du stock.
* Annulation de commande.
* Remboursements.

Les tests d'intégration avec des services externes peuvent nécessiter des environnements de test et des configurations spécifiques.

---

## Installation et démarrage

### Prérequis

Installer les outils suivants :

* Java 21.
* Maven, si le projet n'utilise pas le wrapper Maven.
* Node.js et npm.
* Docker Desktop ou Docker Engine.
* Git.
* Un compte Stripe pour tester les paiements.

### 1. Récupérer le projet

```bash
git clone <URL_DU_DEPOT>
cd plateforme-ecommerce
```

Remplacer `<URL_DU_DEPOT>` par l'adresse réelle du dépôt Git.

### 2. Configurer les variables d'environnement

Créer une configuration locale pour PostgreSQL, l'authentification, Stripe, la messagerie et les éventuels fournisseurs.

Ne jamais publier les valeurs secrètes dans le dépôt.

### 3. Démarrer PostgreSQL

Depuis le répertoire contenant le fichier Docker Compose :

```bash
docker compose up -d
```

Vérifier que le conteneur est démarré :

```bash
docker ps
```

La configuration Docker doit correspondre aux paramètres de connexion utilisés par le backend.

### 4. Démarrer le backend

Depuis le répertoire `backend/`, sous Windows :

```powershell
.\mvnw.cmd spring-boot:run
```

Sous Linux ou macOS :

```bash
./mvnw spring-boot:run
```

Si le wrapper Maven n'est pas présent, utiliser Maven installé localement :

```bash
mvn spring-boot:run
```

Au démarrage, vérifier les logs pour confirmer que l'application se connecte à PostgreSQL et que les migrations Flyway s'exécutent correctement.

### 5. Démarrer la boutique

Dans un terminal :

```bash
cd frontend/shop
npm install
npm run dev
```

### 6. Démarrer le back-office

Dans un autre terminal :

```bash
cd frontend/admin
npm install
npm run dev
```

Les adresses locales d'accès aux interfaces sont indiquées par Vite dans le terminal. Les ports dépendent de la configuration de chaque application.

---

## Configuration

Les paramètres sensibles doivent être fournis par des variables d'environnement ou par une configuration locale exclue du dépôt Git.

Exemples de paramètres :

```text
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD

JWT_SECRET

STRIPE_SECRET_KEY
STRIPE_WEBHOOK_SECRET

MAIL_HOST
MAIL_USERNAME
MAIL_PASSWORD

SUPPLIER_API_URL
SUPPLIER_API_KEY
```

Les noms exacts attendus dépendent de la configuration Spring Boot et des propriétés définies dans le projet.

Les clés de production ne doivent jamais être utilisées dans les environnements de test ni exposées au frontend.

---

## Tester les webhooks Stripe en local

Stripe CLI permet de transférer les événements Stripe vers le backend pendant le développement.

Exemple :

```bash
stripe listen --forward-to localhost:8080/<CHEMIN_WEBHOOK>
```

Remplacer `<CHEMIN_WEBHOOK>` par le chemin réel de l'endpoint webhook configuré dans le backend.

Stripe CLI fournit alors un secret de signature pour l'environnement local. Ce secret doit être utilisé dans la configuration locale de l'application.

Les tests doivent notamment vérifier la validation de la signature et le comportement du backend lors de la réception répétée d'un même événement.

---

## Gestion des erreurs

Le backend dispose d'une gestion centralisée des erreurs afin de fournir des réponses API cohérentes.

Les erreurs de validation, les erreurs métier, les problèmes d'authentification et d'autorisation ainsi que les erreurs techniques doivent être distingués.

Les réponses envoyées au client ne doivent pas exposer de secrets, de traces internes ou de détails sensibles de l'infrastructure.

---

## Observabilité et exploitation

Les besoins d'exploitation comprennent notamment :

* Vérification de l'état de santé du backend.
* Journalisation des erreurs.
* Traçabilité des opérations administratives sensibles.
* Sauvegarde et restauration de la base de données.
* Surveillance des paiements et des commandes.
* Suivi des opérations fournisseur.
* Documentation des interfaces API.
* Gestion des secrets et des configurations par environnement.

La mise en production nécessite également une configuration adaptée de l'hébergement, du HTTPS, des sauvegardes, de la surveillance et de la gestion des incidents.

---

## Choix d'architecture

### Calculs financiers côté serveur

Les prix, les frais de livraison, la TVA, le stock et les totaux de commande sont recalculés ou validés par le backend.

Le frontend ne constitue pas une source de vérité pour les montants financiers.

### Paiement confirmé par webhook

Le backend s'appuie sur les événements Stripe pour confirmer les paiements, plutôt que sur une simple redirection du navigateur.

### Conservation des données de commande

Les informations commerciales importantes sont enregistrées avec la commande :

* Adresse de livraison.
* Adresse de facturation.
* Prix des produits.
* Frais de livraison.
* Informations fiscales.
* Lignes de commande.

Cette approche permet de conserver un historique cohérent même si le catalogue ou le profil client évolue.

### Adaptateurs fournisseurs

Les fournisseurs sont isolés derrière des adaptateurs. Cette séparation facilite l'ajout ou le remplacement d'un partenaire sans réécrire les règles métier principales.

### Séparation Shop / Admin

La boutique et le back-office sont deux applications frontend distinctes qui consomment la même API REST.

Cette séparation facilite l'évolution indépendante des interfaces tout en centralisant les règles métier dans le backend.

### Migrations versionnées

Flyway permet de versionner les évolutions de la base de données et de rendre les changements de schéma reproductibles.

---

## Limites et évolutions

Certaines fonctionnalités ne font pas partie du périmètre actuel ou restent à approfondir.

### Évolutions envisagées

* Avis clients avec achat vérifié et modération.
* Promotions et codes de réduction.
* Historique des prix pour les règles relatives aux prix barrés.
* Automatisation avancée du traitement des commandes fournisseurs.
* Amélioration du moteur de recherche.
* Optimisation de la logistique selon le volume de commandes.

### Dépendances liées aux fournisseurs

Le dropshipping nécessite un fournisseur qui accepte effectivement de recevoir les commandes, de préparer les produits et de les expédier directement aux clients.

La présence d'un catalogue ou d'une API de produits ne suffit pas à garantir cette capacité.

Avant toute exploitation commerciale, chaque intégration doit être vérifiée concernant :

1. La réception et la confirmation des commandes.
2. La disponibilité réelle des produits.
3. La préparation et l'expédition directe.
4. Le suivi des colis.
5. La gestion des annulations et des retours.
6. Les délais, les frais et les conditions commerciales.

---

## Objectifs techniques

Le développement de SIDRA permet de mettre en pratique plusieurs domaines du développement logiciel.

### Backend et conception logicielle

* Développement Java avec Spring Boot.
* Conception d'API REST.
* Organisation par domaines métier.
* Gestion des dépendances.
* Validation des données.
* Gestion centralisée des erreurs.
* Transactions et règles métier.

### Bases de données

* Modélisation relationnelle.
* PostgreSQL.
* Accès aux données avec Spring Data.
* Migrations SQL avec Flyway.
* Conservation de l'historique des commandes.

### Sécurité

* Authentification JWT.
* Contrôle des rôles et des autorisations.
* Protection des endpoints.
* Validation côté serveur.
* Gestion des opérations sensibles.

### Intégrations et services externes

* Intégration de Stripe.
* Traitement des webhooks.
* Génération de factures PDF.
* Envoi d'e-mails transactionnels.
* Conception d'adaptateurs pour des fournisseurs externes.

### Frontend

* Développement avec Vue 3.
* TypeScript.
* Gestion d'état avec Pinia.
* Communication avec une API REST.
* Séparation entre interface client et administration.

### Outils et environnement

* Git et GitHub.
* Maven.
* npm et Vite.
* Docker et Docker Compose.
* Tests automatisés.
* Gestion de configuration par environnement.

---

## Auteur

**Youssouf Hassan**

Étudiant en L3 Informatique à l'Université de Bordeaux.

Projet personnel orienté **développement logiciel full-stack, conception backend et intégration de services externes**.

### Technologies principales

```text
Java · Spring Boot · Spring Security · JWT
PostgreSQL · Flyway · Maven
Vue 3 · TypeScript · Pinia · Vite
Docker · Stripe · API REST
Git · GitHub
```

---

## Statut du projet

**SIDRA — Projet personnel en développement continu.**

La plateforme repose sur un backend Spring Boot, une base PostgreSQL et deux applications frontend distinctes. Elle est conçue pour faire évoluer progressivement ses fonctionnalités métier et ses intégrations externes vers une exploitation réelle.

Le périmètre effectivement opérationnel dépend de l'état du code, des tests réalisés et de la configuration des services externes.
