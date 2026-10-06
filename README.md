# SHAHIN — Plateforme e-commerce

Plateforme e-commerce complète développée avec **Spring Boot, Vue 3, TypeScript et PostgreSQL**, conçue pour gérer la vente en ligne de parfums, cosmétiques et produits lifestyle auprès d'une clientèle en France et dans l'Union européenne.

Le projet intègre un parcours e-commerce complet : **catalogue, authentification, panier, checkout, paiement Stripe, commandes, facturation, livraison, TVA, gestion des stocks, remboursements, comptes clients, administration et intégration de fournisseurs**.

> Projet personnel développé dans une démarche de conception d'une application e-commerce complète, avec une attention particulière portée à la sécurité, à la modularité du backend et aux contraintes réelles d'une boutique en ligne.

---

## Aperçu

SHAHIN repose sur une architecture séparant clairement :

* le **frontend client** ;
* le **back-office administrateur** ;
* le **backend REST** ;
* la **base de données PostgreSQL** ;
* les services de paiement, d'e-mail, de stockage et de fournisseurs.

Le backend a été conçu autour de modules métier indépendants afin de faciliter l'évolution de la plateforme.

### Architecture générale

```text
                         ┌──────────────────────┐
                         │      SHAHIN SHOP     │
                         │    Vue 3 / TypeScript│
                         └──────────┬───────────┘
                                    │
                                    │ REST / JSON
                                    ▼
┌─────────────────────────────────────────────────────────┐
│                    SPRING BOOT API                      │
│                                                         │
│  Identity     Catalog       Cart        Order           │
│  Payment      Shipping      Invoice     Storage         │
│  Supplier     Admin         Mail        Legal           │
└───────────────┬─────────────────────────┬───────────────┘
                │                         │
                ▼                         ▼
       ┌────────────────┐       ┌────────────────────┐
       │   PostgreSQL   │       │ Services externes  │
       │                │       │                    │
       │ Flyway         │       │ Stripe             │
       │ Transactions  │       │ Email              │
       │ Stock         │       │ Fournisseurs       │
       └────────────────┘       └────────────────────┘

                         ▲
                         │
                  REST / JSON
                         │
              ┌──────────┴──────────┐
              │   SHAHIN ADMIN     │
              │ Vue 3 / TypeScript │
              └────────────────────┘
```

---

## Fonctionnalités

### Authentification et comptes

* Inscription et connexion sécurisées
* Authentification par JWT
* Gestion du profil utilisateur
* Gestion des adresses
* Vérification de l'adresse e-mail
* Renvoi du lien de vérification
* Mot de passe oublié
* Réinitialisation sécurisée du mot de passe
* Invalidation des sessions après changement de mot de passe
* Gestion des rôles et accès administrateur

### Catalogue

* Gestion des produits
* Catégories
* Marques
* Images multiples
* Notes olfactives
* Disponibilité
* Prix
* Stock
* Références produits
* Variantes de produit
* Recherche serveur
* Pagination
* Filtres combinables
* Tri des résultats

Les variantes permettent notamment de gérer plusieurs contenances d'un même produit avec des prix, références et stocks indépendants.

Exemple :

```text
Yara
├── 30 ml
├── 50 ml
└── 100 ml
```

Chaque variante possède ses propres informations commerciales et logistiques.

---

## Panier

Le panier permet :

* l'ajout de produits ;
* la sélection d'une variante ;
* la modification des quantités ;
* la suppression d'articles ;
* le calcul du sous-total ;
* le calcul des frais de livraison ;
* le calcul de la TVA ;
* le calcul du montant total ;
* la vérification de la disponibilité du stock.

Les montants sensibles sont recalculés **côté serveur** afin d'éviter qu'un client puisse modifier le prix envoyé à l'API.

---

## Checkout et commandes

Le parcours de commande prend en charge :

* commande avec compte ;
* commande invité ;
* adresse de livraison obligatoire ;
* adresse de facturation ;
* validation serveur des adresses ;
* livraison dans l'Union européenne ;
* calcul serveur des frais de livraison ;
* calcul de la TVA ;
* prix TTC ;
* création d'une commande figée ;
* confirmation du paiement ;
* traitement manuel de la commande.

### Adresse figée

L'adresse utilisée lors de la commande est enregistrée directement dans la commande.

Ainsi :

```text
Profil utilisateur
       │
       ├── Adresse actuelle
       │
       └── Adresse historique
               │
               ▼
           Commande
```

Une modification ultérieure du profil ne modifie donc jamais une commande passée.

---

## Paiement Stripe

Le paiement est réalisé avec **Stripe Checkout**.

Le backend ne considère pas simplement la redirection du navigateur comme une preuve de paiement.

La confirmation définitive repose sur le **webhook Stripe**.

```text
Client
  │
  ▼
Checkout
  │
  ▼
Stripe
  │
  │ paiement confirmé
  ▼
Webhook
  │
  ▼
Spring Boot
  │
  ├── Confirmation commande
  ├── Mise à jour paiement
  ├── Traitement stock
  ├── Génération facture
  └── Notification client
```

Cette architecture évite de faire confiance uniquement au navigateur du client.

---

## Frais de livraison

Les frais de livraison sont calculés exclusivement par le backend.

Ils dépendent notamment :

* du pays ;
* de la zone de livraison ;
* du montant de la commande ;
* du poids lorsque celui-ci est disponible ;
* du seuil de gratuité configuré.

Les frais sont calculés **une seule fois par commande**, indépendamment du mode d'approvisionnement final des produits.

Le montant calculé est ensuite figé dans la commande.

---

## TVA

Les prix présentés au client sont exprimés **TTC**.

Le backend décompose ensuite le montant :

```text
Prix TTC
   │
   ├── Prix HT
   │
   └── TVA
```

Le taux de TVA applicable est enregistré avec la commande afin qu'une modification ultérieure de la configuration fiscale ne puisse pas modifier l'historique des commandes.

Le modèle permet également de gérer différents taux selon les pays européens.

---

## Facturation

Une facture est générée automatiquement pour les commandes payées.

Le système prend en charge :

* numérotation des factures ;
* génération PDF ;
* date de facture ;
* identité du vendeur ;
* identité du client ;
* adresse de facturation ;
* détail des produits ;
* quantités ;
* prix unitaires HT ;
* total HT ;
* TVA ;
* total TTC ;
* frais de livraison ;
* téléchargement de la facture ;
* accès administrateur aux factures.

Une facture émise est considérée comme un document historique et ne doit pas être modifiée comme une simple donnée commerciale.

---

## Remboursements

Le back-office permet d'effectuer :

* un remboursement total ;
* un remboursement partiel.

Le remboursement déclenche un appel réel à l'API Stripe.

Le système assure également :

* la restauration du stock propre ;
* l'enregistrement du montant remboursé ;
* la date ;
* le motif ;
* l'administrateur ayant effectué l'opération ;
* l'envoi d'un e-mail au client.

---

## Gestion des stocks

Le stock est géré côté serveur.

Fonctionnalités :

* stock disponible ;
* décrément lors du traitement de la commande ;
* restauration lors d'un remboursement ou retour ;
* blocage de la vente lorsque le stock propre est épuisé ;
* seuil d'alerte configurable ;
* historique des mouvements ;
* entrées ;
* sorties ;
* corrections ;
* retours.

---

## Architecture hybride d'approvisionnement

SHAHIN supporte deux modes de fulfillment :

```text
                    Commande
                       │
                       ▼
               Validation manuelle
                       │
             ┌─────────┴─────────┐
             │                   │
             ▼                   ▼
        OWN_STOCK             DROPSHIP
             │                   │
             ▼                   ▼
       Stock SHAHIN          Fournisseur
             │                   │
             ▼                   ▼
        Expédition            Expédition
```

### OWN_STOCK

Le produit est disponible dans le stock propre de SHAHIN.

```text
SHAHIN
  │
  ▼
Préparation
  │
  ▼
Transporteur
  │
  ▼
Client
```

### DROPSHIP

Le produit est transmis à un fournisseur compatible.

```text
SHAHIN
  │
  ▼
Fournisseur
  │
  ▼
Préparation
  │
  ▼
Client
```

La source d'approvisionnement est déterminée **manuellement après paiement**.

Le statut `PAID` signifie donc :

> commande payée et en attente de traitement manuel.

Le passage à `PREPARING` indique que la commande a été prise en charge.

---

## Architecture fournisseurs

Le backend utilise une architecture d'adaptateurs afin de pouvoir intégrer différents fournisseurs sans coupler le cœur métier à une API particulière.

```text
SupplierService
      │
      ▼
SupplierAdapter
      │
 ┌────┴──────────────┐
 │                   │
 ▼                   ▼
Supplier A       Supplier B
Adapter           Adapter
```

Les adaptateurs peuvent gérer notamment :

* récupération des produits ;
* import catalogue ;
* synchronisation des prix ;
* synchronisation des stocks ;
* création de commandes ;
* récupération des informations de suivi ;
* gestion des erreurs ;
* authentification fournisseur.

Cette abstraction permet d'ajouter un nouveau fournisseur sans réécrire le système de commandes.

---

## Favoris

Les utilisateurs connectés peuvent enregistrer des produits dans leurs favoris.

Caractéristiques :

* un produit ne peut être ajouté qu'une seule fois ;
* suppression possible ;
* conservation d'un produit indisponible ;
* indication de l'indisponibilité ;
* association directe à l'utilisateur.

---

## Emails transactionnels

Le backend gère plusieurs communications transactionnelles :

* vérification d'e-mail ;
* réinitialisation du mot de passe ;
* confirmation de commande ;
* informations de commande ;
* remboursement ;
* informations liées au traitement.

Les templates sont centralisés côté backend.

```text
backend/
└── src/main/resources/
    └── templates/
        ├── invoice/
        └── mail/
```

---

## RGPD et données personnelles

Le backend prévoit les mécanismes nécessaires à la gestion des données personnelles :

* consentement marketing ;
* gestion du compte ;
* export des données personnelles ;
* suppression du compte ;
* contrôle des accès ;
* limitation de conservation ;
* gestion des informations nécessaires à la commande.

Les données liées aux commandes et aux documents comptables sont conservées selon les obligations applicables, indépendamment de la suppression du compte client lorsque cela est nécessaire.

---

## Pages et contenus légaux

L'API permet de servir les contenus légaux de la plateforme :

* mentions légales ;
* CGV ;
* politique de confidentialité ;
* politique de cookies ;
* politique de retour ;
* droit de rétractation.

---

## Sécurité

Le backend intègre plusieurs mécanismes de sécurité :

* JWT ;
* contrôle des rôles ;
* validation côté serveur ;
* protection des endpoints administrateurs ;
* contrôle de propriété des ressources ;
* gestion centralisée des erreurs ;
* limitation des tentatives de connexion ;
* limitation des requêtes ;
* verrouillage temporaire après échecs répétés ;
* journalisation des opérations administratives sensibles ;
* secrets externalisés.

Les clés privées et informations sensibles ne sont pas destinées à être stockées dans le dépôt.

---

## Stack technique

### Backend

| Technologie     | Utilisation                    |
| --------------- | ------------------------------ |
| Java 21         | Langage                        |
| Spring Boot     | Framework backend              |
| Spring Security | Sécurité et authentification   |
| JWT             | Authentification stateless     |
| Spring Data     | Accès aux données              |
| PostgreSQL      | Base de données                |
| Flyway          | Migrations SQL                 |
| Maven           | Gestion du projet              |
| Stripe          | Paiement                       |
| Docker          | Environnement d'exécution      |
| REST API        | Communication frontend/backend |

### Frontend

| Technologie | Utilisation            |
| ----------- | ---------------------- |
| Vue 3       | Interface utilisateur  |
| TypeScript  | Typage                 |
| Pinia       | Gestion d'état         |
| Vite        | Build et développement |
| CSS         | Interface responsive   |

Deux applications frontend sont présentes :

```text
frontend/
├── shop/
└── admin/
```

### Infrastructure

* PostgreSQL
* Docker / Docker Compose
* Stripe Webhooks
* système de stockage des fichiers
* Git / GitHub

---

## Structure du projet

```text
plateforme-ecommerce/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/parfum/ecommerce/
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── db/migration/
│   │   │       ├── static/images/
│   │   │       └── templates/
│   │   │
│   │   └── test/
│   │
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
│
├── docs/
│
└── README.md
```

---

## Organisation du backend

Le backend est organisé par domaines métier :

```text
com.parfum.ecommerce
│
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

Cette organisation permet de séparer les responsabilités plutôt que de regrouper toute la logique dans une architecture technique unique.

---

## Base de données

PostgreSQL constitue le stockage principal.

Les évolutions du schéma sont gérées avec **Flyway**.

```text
Application
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

Les migrations permettent de versionner l'évolution de la structure de la base et de reproduire l'environnement de développement.

---

## API REST

L'application expose une API REST consommée par les deux interfaces frontend :

```text
                 ┌──────────────┐
                 │  Shop Vue 3  │
                 └──────┬───────┘
                        │
                        ▼
                 ┌──────────────┐
                 │              │
                 │ Spring Boot  │
                 │     API      │
                 │              │
                 └──────┬───────┘
                        ▲
                        │
                 ┌──────┴───────┐
                 │ Admin Vue 3   │
                 └───────────────┘
```

Les endpoints sont organisés autour des différents domaines métier : identité, catalogue, panier, commandes, paiement, livraison, administration, fournisseurs, etc.

---

## Tests

Les flux critiques sont couverts par des tests automatisés, notamment :

* inscription ;
* connexion ;
* authentification ;
* gestion du panier ;
* calcul des montants ;
* checkout ;
* confirmation du paiement ;
* webhook Stripe ;
* gestion du stock ;
* annulation ;
* remboursement.

L'objectif est de protéger les règles métier critiques contre les régressions.

---

## Démarrage du projet

### Prérequis

Installer :

* Java 21
* Maven
* Node.js
* npm
* Docker Desktop
* PostgreSQL via Docker
* un compte Stripe pour les paiements

### Backend

Depuis le dossier backend :

```bash
./mvnw spring-boot:run
```

Sous Windows :

```powershell
.\mvnw.cmd spring-boot:run
```

### Frontend Shop

```bash
cd frontend/shop
npm install
npm run dev
```

### Frontend Admin

```bash
cd frontend/admin
npm install
npm run dev
```

### Base de données

L'environnement PostgreSQL peut être démarré avec Docker Compose :

```bash
docker compose up -d
```

Vérification :

```bash
docker ps
```

---

## Configuration

Les informations sensibles doivent être fournies par variables d'environnement ou par une configuration locale non versionnée.

Exemples :

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

Aucune clé secrète réelle ne doit être commitée dans Git.

---

## Webhook Stripe en développement

Pour tester les paiements localement, Stripe CLI peut être utilisé pour rediriger les événements vers le backend :

```bash
stripe listen --forward-to localhost:8080/...
```

Le secret du webhook est ensuite fourni via la configuration locale.

---

## Gestion des erreurs

Le backend possède une gestion centralisée des erreurs afin de fournir des réponses API cohérentes.

Les erreurs métier, validation, authentification, autorisation et erreurs techniques sont distinguées afin d'éviter de retourner des informations internes inutiles au client.

---

## Observabilité et exploitation

Le backend prévoit également :

* endpoint de santé ;
* logs structurés ;
* surveillance des erreurs ;
* sauvegarde de la base de données ;
* documentation de l'API ;
* suivi des commandes fournisseur ;
* traçabilité des opérations administratives sensibles.

---

## Choix d'architecture

Quelques choix structurants du projet :

### Calculs côté serveur

Les prix, frais de livraison, taxes, stock et totaux de commande sont recalculés côté backend.

Le frontend ne constitue jamais une source de vérité pour les montants financiers.

### Paiement confirmé par webhook

Le paiement est confirmé par Stripe côté serveur plutôt que par une simple information provenant du navigateur.

### Commande immuable

Les informations importantes d'une commande sont figées au moment de sa création :

* adresse ;
* prix ;
* TVA ;
* frais de livraison ;
* lignes de commande ;
* informations de facturation.

### Adaptateurs fournisseurs

Les fournisseurs sont isolés derrière des adaptateurs afin de permettre leur remplacement ou leur multiplication.

### Séparation Shop / Admin

L'interface client et le back-office sont deux applications frontend distinctes consommant la même API.

---

## Fonctionnalités volontairement hors périmètre

Certaines fonctionnalités sont prévues pour une évolution ultérieure :

* avis clients avec achat vérifié et modération ;
* promotions et codes promo ;
* historique de prix nécessaire aux prix barrés ;
* automatisation complète du fulfillment fournisseur ;
* amélioration avancée du moteur de recherche ;
* optimisation logistique selon le volume.

Ces éléments ne sont pas nécessaires au fonctionnement du socle e-commerce actuel.

---

## Limites connues

Le dropshipping dépend de la disponibilité d'un véritable fournisseur logistique.

Une base de données de produits ou un catalogue ne constitue pas à lui seul un service de dropshipping.

L'intégration fournisseur est donc conçue pour pouvoir être branchée à un partenaire capable de :

1. recevoir une commande ;
2. préparer le produit ;
3. expédier directement au client ;
4. fournir un numéro de suivi.

---

## Objectifs techniques du projet

Ce projet a notamment permis de travailler sur :

* conception d'une API REST ;
* architecture Spring Boot ;
* conception d'une base relationnelle ;
* migrations de schéma ;
* authentification JWT ;
* gestion des rôles ;
* sécurité backend ;
* gestion transactionnelle ;
* intégration d'une API de paiement ;
* webhooks ;
* gestion des stocks ;
* génération de documents PDF ;
* envoi d'e-mails transactionnels ;
* architecture par domaines métier ;
* intégration de fournisseurs externes ;
* développement frontend Vue 3 ;
* TypeScript ;
* gestion d'état avec Pinia ;
* Docker ;
* Git ;
* tests automatisés.

---

## Auteur

**Youssouf Hassan**

Étudiant en L3 Informatique — Université de Bordeaux

Projet personnel orienté **développement logiciel full-stack et conception backend**.

### Technologies principales

```text
Java · Spring Boot · Spring Security · JWT
PostgreSQL · Flyway · Maven
Vue 3 · TypeScript · Pinia · Vite
Docker · Stripe · REST API
Git · GitHub
```

---

## Statut du projet

**Projet fonctionnel — développement continu**

Le backend constitue le socle métier de la plateforme SHAHIN et est conçu pour évoluer progressivement vers une exploitation réelle.

---

## Licence

Projet personnel — tous droits réservés.

Le code est publié à des fins de présentation et de portfolio. Toute réutilisation commerciale doit faire l'objet d'une autorisation préalable.
