# Jeu des allumettes

Projet BTS SIO SLAM : jeu des allumettes en Java (programmation orientee objet)
avec une interface graphique HTML / CSS / JavaScript.

Les regles du jeu sont entierement gerees par les classes Java.
Le JavaScript s'occupe seulement de l'affichage, des animations et des clics.

## Lancer le jeu

Prerequis : un JDK installe (Java 11 ou plus).

Dans le dossier du projet :

    javac Jeu.java Joueur.java Ordinateur.java TestJeu.java ServeurJeu.java
    java ServeurJeu

Puis ouvrir http://127.0.0.1:8080 dans le navigateur.
Ne pas ouvrir `index.html` par double-clic : la page doit etre servie par ServeurJeu.

## Version console (sujet du TP)

    java TestJeu

## Les classes

| Classe | Role |
|---|---|
| `TestJeu` | Point d'entree de la version console. Cree un `Jeu` et lance `menuJeu()`. |
| `Jeu` | Gere la partie : allumettes, tour courant, historique, vainqueur, mode deux joueurs, misere. Methode cle : `jouerCoup(nb)` (un seul coup). |
| `Joueur` | Represente un joueur (nom) et gere la saisie au clavier en mode console. |
| `Ordinateur` | Choix du coup de l'ordinateur : hasard (facile) ou strategie gagnante (difficile), avec ou sans misere. |
| `ServeurJeu` | Pont entre le navigateur et Java (serveur HTTP du JDK, sans bibliotheque externe). Ne contient aucune regle du jeu. |

Relations :
- `TestJeu` cree un `Jeu` (dependance).
- `Jeu` cree et possede ses deux `Joueur` (composition) et utilise `Ordinateur` (methodes statiques).
- `ServeurJeu` utilise un `Jeu` (dependance).

## Communication entre l'interface et Java

Le navigateur ne peut pas appeler Java directement : JavaScript envoie des requetes
HTTP a `ServeurJeu`, qui appelle les methodes de `Jeu` et renvoie l'etat de la partie en JSON.

| Adresse | Methode de `Jeu` appelee |
|---|---|
| `POST /api/nouvelle` | `configurer(...)` |
| `POST /api/jouer` | `jouerCoup(nb)` |
| `POST /api/ordinateur` | `jouerOrdinateur()` |
| `POST /api/rejouer` | `demarrerPartie()` |
| `GET /api/indice` | `conseillerCoup()` et `estPositionPerdante()` |
| `GET /api/etat` | les getters |

## Modes de jeu

- Deux joueurs
- Contre la machine (choix au hasard)
- Contre l'IA (strategie gagnante)
- Option misere : celui qui prend la derniere allumette perd