import type { CategoryFilterDefinition, CategoryNode } from "@/types/catalog";

/**
 * Catégories utilisées pour configurer les filtres du front-end.
 *
 * Cette arborescence est une configuration d'affichage.
 * Elle ne remplace pas l'arborescence de catégories reçue de l'API.
 */
export const catalogCategories: CategoryNode[] = [
  {
    id: "mode",
    name: "Mode",
    slug: "mode",
    path: "mode",
    depth: 0,
    position: 1,
    active: true,
    productCount: 0,
    children: [
      {
        id: "vetements",
        name: "Vêtements",
        slug: "vetements",
        path: "mode/vetements",
        depth: 1,
        position: 1,
        active: true,
        productCount: 0,
        children: [],
      },
      {
        id: "chaussures",
        name: "Chaussures",
        slug: "chaussures",
        path: "mode/chaussures",
        depth: 1,
        position: 2,
        active: true,
        productCount: 0,
        children: [],
      },
      {
        id: "sacs",
        name: "Sacs et maroquinerie",
        slug: "sacs",
        path: "mode/sacs",
        depth: 1,
        position: 3,
        active: true,
        productCount: 0,
        children: [],
      },
      {
        id: "accessoires",
        name: "Accessoires",
        slug: "accessoires",
        path: "mode/accessoires",
        depth: 1,
        position: 4,
        active: true,
        productCount: 0,
        children: [],
      },
    ],
  },
  {
    id: "beaute",
    name: "Beauté",
    slug: "beaute",
    path: "beaute",
    depth: 0,
    position: 2,
    active: true,
    productCount: 0,
    children: [
      {
        id: "parfums",
        name: "Parfums",
        slug: "parfums",
        path: "beaute/parfums",
        depth: 1,
        position: 1,
        active: true,
        productCount: 0,
        children: [],
      },
      {
        id: "soins",
        name: "Soins",
        slug: "soins",
        path: "beaute/soins",
        depth: 1,
        position: 2,
        active: true,
        productCount: 0,
        children: [],
      },
      {
        id: "maquillage",
        name: "Maquillage",
        slug: "maquillage",
        path: "beaute/maquillage",
        depth: 1,
        position: 3,
        active: true,
        productCount: 0,
        children: [],
      },
    ],
  },
  {
    id: "bijoux-montres",
    name: "Montres et bijoux",
    slug: "bijoux-montres",
    path: "bijoux-montres",
    depth: 0,
    position: 3,
    active: true,
    productCount: 0,
    children: [
      {
        id: "montres",
        name: "Montres",
        slug: "montres",
        path: "bijoux-montres/montres",
        depth: 1,
        position: 1,
        active: true,
        productCount: 0,
        children: [],
      },
      {
        id: "bijoux",
        name: "Bijoux",
        slug: "bijoux",
        path: "bijoux-montres/bijoux",
        depth: 1,
        position: 2,
        active: true,
        productCount: 0,
        children: [],
      },
    ],
  },
  {
    id: "maison",
    name: "Maison et décoration",
    slug: "maison",
    path: "maison",
    depth: 0,
    position: 4,
    active: true,
    productCount: 0,
    children: [],
  },
  {
    id: "electronique",
    name: "Électronique",
    slug: "electronique",
    path: "electronique",
    depth: 0,
    position: 5,
    active: true,
    productCount: 0,
    children: [],
  },
];

/**
 * Crée un filtre de type liste déroulante.
 */
function select(
  key: string,
  label: string,
  values: string[],
): CategoryFilterDefinition {
  return {
    key,
    label,
    type: "select",
    options: values.map((value) => ({
      label: value,
      value: value.toLowerCase(),
    })),
  };
}

/**
 * Filtres spécifiques par slug de catégorie.
 *
 * Les filtres généraux (prix, disponibilité, maisons et tri)
 * sont gérés séparément dans CatalogGlobalFilters.vue.
 */
export const categoryFilters: Record<string, CategoryFilterDefinition[]> = {
  vetements: [
    select("size", "Taille", ["XXS", "XS", "S", "M", "L", "XL", "XXL", "3XL"]),
    select("gender", "Genre", ["Femme", "Homme", "Unisexe"]),
    select("color", "Couleur", [
      "Noir",
      "Blanc",
      "Beige",
      "Marron",
      "Bleu",
      "Rouge",
      "Vert",
      "Gris",
      "Rose",
    ]),
    select("material", "Matière", [
      "Coton",
      "Laine",
      "Lin",
      "Soie",
      "Polyester",
      "Cuir",
    ]),
    select("fit", "Coupe", ["Slim", "Regular", "Oversize", "Ample"]),
  ],

  chaussures: [
    select(
      "size",
      "Pointure",
      Array.from({ length: 17 }, (_, index) => String(index + 36)),
    ),
    select("gender", "Genre", ["Femme", "Homme", "Unisexe"]),
    select("color", "Couleur", [
      "Noir",
      "Blanc",
      "Beige",
      "Marron",
      "Bleu",
      "Gris",
    ]),
    select("material", "Matière", ["Cuir", "Daim", "Textile", "Synthétique"]),
    select("shoeType", "Type de chaussure", [
      "Baskets",
      "Bottes",
      "Mocassins",
      "Sandales",
      "Escarpins",
      "Chaussures habillées",
    ]),
  ],

  sacs: [
    select("bagType", "Type de sac", [
      "Sac à main",
      "Sac à dos",
      "Sac bandoulière",
      "Sac de voyage",
      "Pochette",
      "Portefeuille",
    ]),
    select("color", "Couleur", ["Noir", "Blanc", "Beige", "Marron", "Bleu"]),
    select("material", "Matière", ["Cuir", "Toile", "Synthétique"]),
  ],

  accessoires: [
    select("accessoryType", "Type d'accessoire", [
      "Ceinture",
      "Écharpe",
      "Lunettes",
      "Chapeau",
      "Portefeuille",
      "Autre",
    ]),
    select("color", "Couleur", ["Noir", "Blanc", "Beige", "Marron", "Bleu"]),
    select("material", "Matière", [
      "Cuir",
      "Coton",
      "Laine",
      "Métal",
      "Synthétique",
    ]),
  ],

  parfums: [
    select("volume", "Contenance", [
      "10 ml",
      "30 ml",
      "50 ml",
      "75 ml",
      "100 ml",
      "200 ml",
    ]),
    select("concentration", "Concentration", [
      "Eau de Cologne",
      "Eau de toilette",
      "Eau de parfum",
      "Extrait de parfum",
    ]),
    select("gender", "Genre", ["Femme", "Homme", "Mixte"]),
    select("scentFamily", "Famille olfactive", [
      "Boisée",
      "Florale",
      "Orientale",
      "Fraîche",
      "Gourmande",
      "Ambrée",
      "Musquée",
    ]),
  ],

  soins: [
    select("skinType", "Type de peau", [
      "Tous types",
      "Sèche",
      "Grasse",
      "Mixte",
      "Sensible",
    ]),
    select("productType", "Type de soin", [
      "Nettoyant",
      "Hydratant",
      "Sérum",
      "Protection solaire",
      "Masque",
    ]),
    select("concern", "Besoin", [
      "Hydratation",
      "Éclat",
      "Imperfections",
      "Anti-âge",
      "Protection",
    ]),
  ],

  maquillage: [
    select("productType", "Type de produit", [
      "Fond de teint",
      "Rouge à lèvres",
      "Mascara",
      "Blush",
      "Poudre",
      "Palette",
    ]),
    select("finish", "Fini", ["Mat", "Satiné", "Brillant", "Naturel"]),
    select("color", "Teinte", ["Clair", "Medium", "Foncé", "Transparent"]),
  ],

  montres: [
    select("watchType", "Type de montre", [
      "Classique",
      "Sport",
      "Connectée",
      "Automatique",
    ]),
    select("movement", "Mouvement", [
      "Quartz",
      "Automatique",
      "Mécanique",
      "Connecté",
    ]),
    select("strapMaterial", "Bracelet", [
      "Cuir",
      "Acier",
      "Silicone",
      "Textile",
    ]),
    select("color", "Couleur", ["Noir", "Argent", "Or", "Marron", "Bleu"]),
  ],

  bijoux: [
    select("jewelryType", "Type de bijou", [
      "Bague",
      "Collier",
      "Bracelet",
      "Boucles d'oreilles",
      "Pendentif",
    ]),
    select("material", "Matériau", [
      "Or",
      "Argent",
      "Acier inoxydable",
      "Plaqué or",
    ]),
    select("color", "Couleur", ["Or", "Argent", "Noir", "Rose"]),
  ],

  maison: [
    select("productType", "Type de produit", [
      "Décoration",
      "Luminaire",
      "Textile",
      "Rangement",
      "Art de la table",
    ]),
    select("room", "Pièce", ["Salon", "Chambre", "Cuisine", "Salle de bain"]),
    select("color", "Couleur", ["Blanc", "Noir", "Beige", "Marron", "Vert"]),
    select("material", "Matière", [
      "Bois",
      "Verre",
      "Métal",
      "Céramique",
      "Textile",
    ]),
  ],

  electronique: [
    select("productType", "Type d'appareil", [
      "Téléphone",
      "Ordinateur",
      "Audio",
      "Accessoire",
      "Objet connecté",
    ]),
    select("connectivity", "Connectivité", [
      "Bluetooth",
      "Wi-Fi",
      "USB-C",
      "Sans fil",
    ]),
    select("color", "Couleur", ["Noir", "Blanc", "Argent", "Gris"]),
  ],
};

export const globalFilterLabels = [
  "Prix",
  "Disponibilité",
  "Maisons",
  "Trier par",
] as const;
