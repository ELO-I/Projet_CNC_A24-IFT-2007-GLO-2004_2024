# CNC Designer (IFT-2007 / GLO-2004)

## Version Française

**CNC Designer** est une application de conception graphique assistée par ordinateur (CAO) dédiée aux machines à commande numérique (CNC). Développé dans le cadre des cours **IFT-2007** et **GLO-2004** à l'Université Laval, ce logiciel permet de planifier, configurer et valider des trajectoires de coupe sur des panneaux de divers matériaux.

### Fonctionnalités principales
*   **Interface Graphique Interactive** : Canevas complet permettant le zoom, le déplacement (panning) et la manipulation directe des coupes.
*   **Types de Coupes variés** : Support pour les coupes verticales, horizontales, en L, rectangulaires et les coupes de bordure.
*   **Zones Interdites** : Définition de zones de sécurité pour empêcher l'outil de passer dans des secteurs critiques.
*   **Validation Automatique** : Vérification en temps réel de la validité des coupes (collisions avec zones interdites ou sortie des limites du panneau).
*   **Gestion d'Outils** : Bibliothèque d'outils configurable (largeur de coupe, position dans le magasin).
*   **Export G-Code** : Génération automatique d'un fichier d'instructions G-Code pour une utilisation directe en atelier.
*   **Système Undo/Redo** : Historique complet des modifications basé sur le patron de conception *Memento*.
*   **Unités Flexibles** : Conversion instantanée entre le système métrique (mm) et impérial (pouces).

### Architecture Technique
L'application repose sur une architecture **MVC (Modèle-Vue-Contrôleur)** robuste utilisant des **DTO** (Data Transfer Objects) pour assurer un découpage propre entre la logique métier et l'interface Java Swing. Le projet utilise la bibliothèque **Gson** pour la sérialisation des projets au format JSON.

---

## English Version

**CNC Designer** is a specialized CAD (Computer-Aided Design) application for CNC machines. Developed as part of the **IFT-2007** and **GLO-2004** courses at Université Laval, this software allows users to plan, configure, and validate cutting paths on industrial panels.

### Key Features
*   **Interactive Graphic Interface**: Full canvas support including zooming, panning, and direct cut manipulation.
*   **Diverse Cut Types**: Support for Vertical, Horizontal, L-shaped, Rectangular, and Border cuts.
*   **Forbidden Zones**: Define safety zones to prevent the tool from entering critical areas.
*   **Automatic Validation**: Real-time checking for cut validity (collisions with forbidden zones or exceeding panel boundaries).
*   **Tool Management**: Configurable tool library (cut width, store position).
*   **G-Code Export**: Automatic generation of G-Code instruction files for workshop use.
*   **Undo/Redo System**: Full action history based on the *Memento* design pattern.
*   **Flexible Units**: Instant conversion between Metric (mm) and Imperial (in) systems.

### Technical Architecture
The application is built on a robust **MVC (Model-View-Controller)** architecture using **DTOs** (Data Transfer Objects) to ensure clean separation between business logic and the Java Swing UI. The project leverages the **Gson** library for JSON project serialization.
