# SHAHIN — Référence backend pour le développement du frontend boutique

Ce document décrit **l'état réel** du backend. Il est destiné à un agent de développement frontend.

**Règle absolue : ne jamais inventer un endpoint, un champ ou une donnée qui ne figure pas ici.** Si une fonctionnalité nécessite quelque chose d'absent, le signaler au lieu de le simuler.

---

## 1. Contexte

- Boutique **SHAHIN** : parfums et produits venus d'ailleurs, clientèle jeune et internationale, livraison dans l'Union européenne.
- Backend : Spring Boot 4.1, Java 21, PostgreSQL, Stripe Checkout.
- Frontend boutique : Vue 3 + TypeScript + Vite + Pinia + Vue Router, dans `frontend/shop` (port 5173).
- URL de l'API : variable `VITE_API_URL`, en développement `http://localhost:8080/api`.
- Origine autorisée par CORS en développement : `http://localhost:5173`.
- Tous les prix sont en euros et **TTC**.

---

## 2. Authentification

Jeton **JWT** renvoyé à la connexion, à envoyer dans chaque requête authentifiée :
```
Authorization: Bearer <token>
```

- Validité : 24 heures.
- Un jeton devient **invalide** après un changement ou une réinitialisation de mot de passe, et après suppression du compte. Le frontend doit alors déconnecter l'utilisateur.
- Réponse `401` ou `403` sur une route protégée → effacer le jeton et proposer la reconnexion.

Trois types d'utilisateurs :
- **Client** : compte avec mot de passe.
- **Invité** (`guest: true`) : compte créé automatiquement par code email, sans mot de passe. Il peut commander et suivre ses commandes, et devenir client en choisissant un mot de passe.
- **Administrateur** : back-office, hors périmètre de ce document.

---

## 3. Format des erreurs

```json
{
  "timestamp": "2026-09-23T10:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Il ne reste que 3 exemplaire(s) disponible(s)"
}
```

| Code | Signification | Action frontend |
|---|---|---|
| 400 | Donnée invalide ou ressource introuvable | Afficher `message` près du champ ou en alerte |
| 401 / 403 | Non authentifié ou droits insuffisants | Déconnecter, rediriger vers la connexion |
| 404 | Route inexistante | Page d'erreur |
| 409 | Action refusée selon l'état (panier vide, stock insuffisant, lien déjà utilisé) | Afficher `message` |
| 429 | Trop de tentatives ou compte temporairement verrouillé | Afficher `message`, respecter l'en-tête `Retry-After` (secondes), désactiver le bouton pendant ce délai |
| 500 | Erreur inattendue | Message générique, proposer de réessayer |
| 502 / 503 | Service tiers indisponible | Message générique |

Toujours afficher `error.response.data.message`.

**Limites anti-abus** : 10 tentatives par minute et par adresse IP sur les endpoints d'authentification ; 300 requêtes par minute sur l'API ; 5 mots de passe faux verrouillent un compte 15 minutes. Empêcher les doubles clics sur les boutons de connexion, d'inscription et d'envoi de code.

---

## 4. Endpoints publics (sans authentification)

### Page d'accueil

**GET /api/products/home?limit=8** — tout le contenu de la page d'accueil en un appel :
```json
{
  "featured": [ { ...produit... } ],
  "newest": [ { ...produit... } ],
  "bestSellers": [ { ...produit... } ],
  "categories": [ { "id": "uuid", "name": "Parfums" } ]
}
```
- `featured` : produits choisis par l'administrateur ; reprend les nouveautés si aucune sélection.
- `bestSellers` : ventes des 90 derniers jours. **Peut être vide** au lancement — masquer la section dans ce cas.
- `limit` : 8 par défaut, 12 maximum.

**GET /api/products/featured?limit=8** et **GET /api/products/best-sellers?limit=8** — les mêmes listes séparément.

### Catalogue

**GET /api/products/search** — catalogue paginé et filtré. **À utiliser pour la page catalogue.**

| Paramètre | Description |
|---|---|
| `q` | Recherche texte (nom, marque, description) |
| `category` | Nom exact de catégorie |
| `brand` | Nom exact de marque |
| `minPrice`, `maxPrice` | Fourchette de prix TTC |
| `availableOnly` | `true` pour masquer les produits indisponibles |
| `sort` | `relevance` (défaut), `price_asc`, `price_desc`, `newest`, `name` |
| `page` | Numéro de page, commence à 0 |
| `size` | 20 par défaut, 50 maximum |

```json
{
  "content": [ { ...produit... } ],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3,
  "hasNext": true
}
```

**GET /api/products/filters** — valeurs pour construire les filtres :
```json
{
  "categories": ["Beauté", "Parfums"],
  "brands": ["Lattafa", "Rasasi"],
  "minPrice": 12.99,
  "maxPrice": 259.99,
  "sorts": ["relevance", "price_asc", "price_desc", "newest", "name"]
}
```

**GET /api/products/{id}** — fiche d'un produit actif. `400` si introuvable ou désactivé.

**GET /api/products/{id}/similar?limit=4** — suggestions pour la fiche produit (4 par défaut, 12 maximum). Priorité : même marque, puis même catégorie à prix proche. Le produit courant est exclu, seuls des produits disponibles sont proposés. **Peut être vide** : masquer la section. Appelé une seule fois au chargement.

**GET /api/products** — ancien endpoint, renvoie tout le catalogue sans pagination. À ne plus utiliser.

**GET /api/categories** — `[{ "id": "uuid", "name": "Parfums" }]`

**Format d'un produit :**
```json
{
  "id": "uuid",
  "name": "Asad",
  "description": "Parfum oriental...",
  "brand": "Lattafa",
  "price": 29.99,
  "available": true,
  "imageUrl": "/images/asad/Asad-01.webp",
  "imageUrls": ["/images/asad/Asad-01.webp", "/images/asad/asad-2.webp"],
  "categoryName": "Parfums",
  "variants": [
    { "id": "uuid", "label": "Standard", "price": 29.99, "available": true },
    { "id": "uuid", "label": "100 ml", "price": 44.99, "available": false }
  ]
}
```

Règles d'affichage :
- `price` est le prix **le plus bas** des variantes : afficher « à partir de » si plusieurs prix différents.
- `available` indique qu'au moins une variante est achetable. Le stock exact n'est **jamais** exposé : ne pas afficher de quantité restante.
- **Variantes** : une seule variante → pas de sélecteur, et ne pas afficher le libellé « Standard ». Plusieurs → sélecteur, le prix suit la variante choisie, les variantes `available: false` sont grisées.
- **Images** : chemin relatif (`/images/...`) ou URL absolue (produits importés). Préfixer les chemins relatifs par l'origine du backend. `imageUrls` peut être vide : utiliser `imageUrl`. Prévoir une image de repli.
- `brand` peut être `null`.

### Livraison

**GET /api/shipping/countries** — codes pays ISO desservis : `["AT", "BE", ..., "FR", ...]`
Afficher le nom en français à partir du code (ex. `Intl.DisplayNames`).

### Pages légales

**GET /api/legal** — `[{ "slug", "title", "version", "updatedAt" }]`

**GET /api/legal/{slug}** — `{ "slug", "title", "content", "version", "updatedAt" }`

Slugs : `mentions-legales`, `cgv`, `confidentialite`, `cookies`, `retours`. Contenu en texte brut. Liens obligatoires dans le pied de page.

### Inscription et connexion

**POST /api/auth/register** — `{ "email", "password" }` (8 caractères minimum) → `{ "token" }`
Le compte est créé **non vérifié** et un email de vérification est envoyé. L'utilisateur est connecté mais **ne peut pas commander** tant que son email n'est pas vérifié.

**POST /api/auth/login** — `{ "email", "password" }` → `{ "token" }`
`400` identifiants incorrects, `429` trop de tentatives ou compte verrouillé.

**POST /api/auth/verify-email** — `{ "token" }` → `{ "message" }`
Appelé par la page `/verifier-email?token=...`. `400` lien invalide, `409` déjà utilisé ou expiré.

**POST /api/auth/forgot-password** — `{ "email" }` → `{ "message" }`
Réponse **toujours identique**, que le compte existe ou non.

**POST /api/auth/reset-password** — `{ "token", "newPassword" }` → `{ "message" }`
Appelé par `/reinitialiser-mot-de-passe?token=...`. Rediriger ensuite vers la connexion.

### Commande invité

**POST /api/auth/guest/request-code** — `{ "email" }` → `{ "message" }`
Code à 6 chiffres valable 10 minutes. Réponse toujours identique. Nouveau code possible après 60 secondes : afficher un compte à rebours.

**POST /api/auth/guest/verify-code** — `{ "email", "code" }` → `{ "token" }`
Crée un compte invité si l'email est inconnu, ou connecte au compte existant. 5 essais maximum.

---

## 5. Endpoints authentifiés (client ou invité)

### Profil

**GET /api/me**
```json
{
  "email": "...",
  "firstName": null,
  "lastName": null,
  "phone": null,
  "emailVerified": true,
  "guest": false
}
```
- `emailVerified: false` → bandeau « Vérifiez votre adresse email » avec bouton de renvoi.
- `guest: true` → proposer « Créez votre compte en choisissant un mot de passe ».

**PUT /api/me** — `{ "firstName", "lastName", "phone" }`, mise à jour partielle. L'email n'est pas modifiable.

**POST /api/auth/resend-verification** → `204`. `409` si déjà vérifié ou demande trop rapprochée.

**PUT /api/auth/change-password** — `{ "currentPassword", "newPassword" }` → `204`.
**Le jeton devient invalide** : déconnecter et inviter à se reconnecter.

**POST /api/auth/guest/set-password** — `{ "password" }` → `{ "token" }`. Réservé aux invités. Remplacer le jeton stocké.

### Données personnelles (RGPD)

**GET /api/me/export** → JSON complet (profil, adresses, commandes), à proposer en téléchargement.

**PUT /api/me/consents** — `{ "marketing": true }` → `204`. Case décochée par défaut.

**DELETE /api/me** — `{ "password" }` → `204`. Mot de passe exigé sauf pour un invité. Demander confirmation explicite, puis déconnecter.

### Adresses

**GET /api/addresses**

**POST /api/addresses**
```json
{
  "firstName": "Youssouf",
  "lastName": "Hassan",
  "street": "12 rue de la Paix",
  "complement": "Bât. B",
  "city": "Bordeaux",
  "postalCode": "33000",
  "countryCode": "FR",
  "phone": "0612345678"
}
```
Obligatoires : `firstName`, `lastName`, `street`, `city`, `postalCode`, `countryCode` (2 lettres, parmi `/api/shipping/countries`). Facultatifs : `complement`, `phone`.
Il n'existe **pas** de modification ni de suppression d'adresse.

### Panier

Stocké côté serveur, authentification requise (client ou invité).

**GET /api/cart**
```json
{
  "items": [
    {
      "itemId": "uuid",
      "productId": "uuid",
      "variantId": "uuid",
      "productName": "Asad",
      "variantLabel": "100 ml",
      "unitPrice": 44.99,
      "quantity": 2,
      "available": true,
      "maxQuantity": 5
    }
  ],
  "subtotal": 89.98,
  "shipping": 0.00,
  "vat": 15.00,
  "vatRate": 20.00,
  "total": 89.98,
  "freeShippingThreshold": 40.00,
  "amountUntilFreeShipping": 0.00,
  "checkoutBlocked": false,
  "warnings": []
}
```

Règles d'affichage :
- Les frais de livraison sont une **estimation pour la France** ; le montant définitif dépend du pays de l'adresse choisie au checkout.
- `vat` est la TVA **incluse** dans le total : afficher « dont TVA », ne jamais l'ajouter.
- `amountUntilFreeShipping > 0` → « Plus que X € pour la livraison offerte ».
- `warnings` : messages à afficher en haut du panier (article indisponible, stock réduit, prix modifié depuis l'ajout).
- `checkoutBlocked: true` → **désactiver le bouton de commande** et inviter à corriger le panier.
- `maxQuantity` : plafond du sélecteur de quantité. `null` signifie pas de limite de stock (limite générale de 20 par article).
- Un article `available: false` est exclu du total : le signaler visuellement et proposer de le retirer.
- Masquer `variantLabel` s'il vaut « Standard ».

**POST /api/cart/items** — `{ "productId", "variantId", "quantity" }` → panier mis à jour.
`variantId` **obligatoire** si le produit a plusieurs variantes actives. Ajouter une variante déjà présente augmente sa quantité. Le stock est vérifié dès l'ajout : `409` avec le stock restant si la quantité est trop élevée.

**PUT /api/cart/items/{itemId}** — `{ "quantity" }` (1 minimum) → panier mis à jour.

**DELETE /api/cart/items/{itemId}** → panier mis à jour.

**DELETE /api/cart** → vide entièrement le panier.

**Visiteur non connecté** : pas de panier serveur. Garder le panier dans le navigateur (localStorage : `productId`, `variantId`, `quantity`), puis l'envoyer article par article via `POST /api/cart/items` dès l'obtention d'un jeton.

### Commandes

**POST /api/orders/checkout** — `{ "addressId": "uuid", "acceptTerms": true }` → commande.

Conditions vérifiées par le serveur :
- email vérifié (`409` sinon) ;
- adresse fournie et appartenant à l'utilisateur ;
- `acceptTerms: true` : case « J'accepte les conditions générales de vente » avec lien vers `/api/legal/cgv`, non cochée par défaut ;
- panier non vide, variantes actives, stock suffisant.

Le panier est vidé. La commande est créée au statut `PENDING` et **expire au bout de 30 minutes** si elle n'est pas payée.

```json
{
  "id": "uuid",
  "status": "PENDING",
  "subtotalAmount": 89.98,
  "shippingAmount": 0.00,
  "vatAmount": 15.00,
  "vatRate": 20.00,
  "totalAmount": 89.98,
  "createdAt": "2026-09-23T10:00:00",
  "customerFirstName": "Youssouf",
  "customerLastName": "Hassan",
  "customerEmail": "...",
  "items": [
    { "productName": "Asad", "variantLabel": "100 ml", "quantity": 2, "unitPrice": 44.99 }
  ]
}
```

**POST /api/orders/{orderId}/pay** → `{ "sessionId", "checkoutUrl" }`
Rediriger le navigateur vers `checkoutUrl` (page Stripe).
- Après paiement : retour sur `/commande/succes?order={orderId}`.
- En cas d'abandon : retour sur `/cart`.

**Confirmation asynchrone** : au retour sur la page de succès, la commande peut encore être `PENDING` quelques secondes. Interroger `GET /api/orders` toutes les 2 secondes (30 secondes maximum) jusqu'à voir `PAID`, en affichant « Confirmation du paiement en cours ». Ne jamais afficher « payée » sans ce statut.

**GET /api/orders** — commandes de l'utilisateur, les plus récentes d'abord.

| Statut | Libellé |
|---|---|
| `PENDING` | En attente de paiement |
| `PAID` | Payée |
| `PREPARING` | En préparation |
| `SHIPPED` | Expédiée |
| `DELIVERED` | Livrée |
| `CANCELLED` | Annulée |

Une commande `PENDING` non expirée peut être payée à nouveau via `/pay` (« Finaliser le paiement »).

**GET /api/orders/{orderId}/invoice** → PDF. Uniquement pour les commandes payées (`409` sinon). Télécharger via une requête authentifiée (`fetch` avec en-tête, puis créer un lien de téléchargement depuis le fichier reçu — pas un simple `<a href>`).

**GET /api/orders/{orderId}/shipment** → `{ "carrier", "trackingNumber", "shippedAt" }`. `400` si non expédiée.

---

## 6. Parcours à implémenter

### Page d'accueil
Un seul appel à `/api/products/home` : bandeau, produits mis en avant, nouveautés, meilleures ventes (masquée si vide), catégories.

### Page catalogue
`/api/products/search` avec filtres et pagination, `/api/products/filters` pour alimenter les filtres. Conserver les filtres dans l'URL pour que la page soit partageable.

### Page produit
`/api/products/{id}` puis `/api/products/{id}/similar`. Galerie d'images, sélecteur de variante, ajout au panier.

### Tunnel de commande
1. **Panier** : articles, quantités (bornées par `maxQuantity`), `warnings`, sous-total, livraison estimée, « dont TVA », total, incitation à la livraison offerte. Bouton désactivé si `checkoutBlocked`.
2. **Identification** : se connecter, créer un compte, ou continuer en invité (email → code). Envoyer ensuite le panier local au serveur.
3. **Vérification d'email** : si `emailVerified: false`, bloquer avec un bouton de renvoi.
4. **Adresse** : choisir une adresse existante ou en saisir une nouvelle.
5. **Récapitulatif** : articles, livraison, total, case CGV obligatoire.
6. **Paiement** : checkout puis redirection vers Stripe.
7. **Confirmation** : attente du statut `PAID`, liens vers la commande et la facture.

### Pages appelées depuis les emails
- `/verifier-email?token=...` → `POST /api/auth/verify-email`
- `/reinitialiser-mot-de-passe?token=...` → formulaire → `POST /api/auth/reset-password`
- `/commande/succes?order=...` → confirmation de paiement

En développement, les emails ne partent pas : leur contenu et leurs liens s'affichent dans la console du backend.

### Espace client
Onglets : commandes (frise de statut, facture, suivi de colis), informations personnelles, adresses (liste et ajout), sécurité (changement de mot de passe), données personnelles (export, consentement marketing, suppression du compte).

---

## 7. Emails envoyés automatiquement

Le client reçoit, sans action du frontend : vérification d'adresse, réinitialisation de mot de passe, code de connexion invité, commande reçue, commande en préparation, commande expédiée (avec suivi), commande livrée, commande annulée.

Le frontend n'a donc pas à afficher de message « un email vous a été envoyé », sauf pour la vérification d'adresse, le mot de passe oublié et le code invité, où l'action suivante dépend de l'email.

---

## 8. Fonctionnalités qui n'existent PAS

Ne pas les implémenter, ne pas les simuler :
- favoris / liste d'envies ;
- avis clients et notes ;
- promotions, prix barrés, codes promo ;
- remboursement ou retour en ligne (traité manuellement) ;
- notes olfactives ;
- modification ou suppression d'une adresse ;
- modification de l'email ;
- choix du transporteur ou point relais ;
- paiement autre que Stripe Checkout ;
- plusieurs devises ou langues.

Ne jamais afficher : stock exact, coût d'achat, marge, fournisseur, type d'approvisionnement, indicateur « mis en avant ». Pour le client, tous les produits sont des produits SHAHIN.

Ne jamais inventer : avis, statistiques, promotions, délais de livraison chiffrés.

---

## 9. Changements par rapport au frontend actuel

Le frontend existant a été construit sur une version antérieure de l'API :
- `stockQuantity` n'existe plus côté boutique : utiliser `available` (produit et variantes).
- Catalogue : passer à `/api/products/search` avec pagination et filtres serveur.
- Page produit : utiliser `/api/products/{id}` et gérer les variantes ; ajouter les suggestions.
- Page d'accueil : un seul appel à `/api/products/home`.
- Panier : envoyer `variantId`, afficher `variantLabel`, gérer `warnings`, `checkoutBlocked` et `maxQuantity`, utiliser les nouveaux montants.
- Checkout : adresse et `acceptTerms` obligatoires, email vérifié exigé.
- Paiement : rediriger vers `checkoutUrl` ; créer la page de succès avec attente du statut.
- Adresses : nouveaux champs (`firstName`, `lastName`, `complement`, `countryCode`, `phone`).
- Ajouter : vérification d'email, mot de passe oublié, commande invité, pages légales, RGPD.
