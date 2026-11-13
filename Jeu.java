import java.util.ArrayList;
import java.util.List;

class Jeu {
    // Valeurs possibles pour le nombre d'allumettes de depart
    static final int NB_MIN = 1;
    static final int NB_MAX = 100;

    // Valeur de "vainqueur" tant que la partie n'est pas terminee
    static final int AUCUN = -1;

    private int nbAllumettes = 17;
    private int nbDepart = 17;   // a ajouter au diagramme de classes (menuJeu)
    private int niveau = 1;      // 1 = facile (aleatoire), 2 = difficile (strategie)

    // --- Ajouts pour l'interface graphique ---
    private int nbJoueurs = 1;       // 1 = joueur contre ordinateur, 2 = joueur contre joueur
    private boolean misere = false;  // true : celui qui prend la derniere allumette perd
    private Joueur joueur1 = new Joueur("Vous");              // composition : Jeu cree ses joueurs
    private Joueur joueur2 = new Joueur("L'ordinateur");      // adversaire (humain ou ordinateur)
    private int joueurCourant = 0;   // 0 = joueur1, 1 = joueur2
    private int vainqueur = AUCUN;   // 0 ou 1 quand la partie est finie
    private List<int[]> historique = new ArrayList<>();  // un coup = {joueur, nb retire, nb restant}

    public void afficherAllumettes() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nbAllumettes; i++) {
            sb.append("| ");
        }
        System.out.println(sb.toString().trim() + "   (" + nbAllumettes + ")");
    }

    public void commencerJeu() {
        demarrerPartie();                  // retour au nombre de depart a chaque partie
        while (nbAllumettes > 0) {
            afficherAllumettes();
            if (estTourOrdinateur()) {
                jouerOrdinateur();
            } else {
                Joueur joueur = (joueurCourant == 0) ? joueur1 : joueur2;
                jouerCoup(joueur.propositionJoueur(nbAllumettes));
            }
        }
        annoncerVainqueur();
    }

    // ------------------------------------------------------------------
    // Methodes utilisees par l'interface graphique (un seul coup a la fois)
    // ------------------------------------------------------------------

    // Regle la partie puis la demarre. Retourne false si un parametre est invalide.
    public boolean configurer(int nbDepart, int nbJoueurs, int niveau, boolean misere,
                              String nom1, String nom2) {
        if (nbDepart < NB_MIN || nbDepart > NB_MAX) return false;
        if (nbJoueurs < 1 || nbJoueurs > 2) return false;
        if (niveau < 1 || niveau > 2) return false;
        this.nbDepart = nbDepart;
        this.nbJoueurs = nbJoueurs;
        this.niveau = niveau;
        this.misere = misere;
        this.joueur1 = new Joueur(nomValide(nom1, "Joueur 1"));
        this.joueur2 = new Joueur(nomValide(nom2, "Joueur 2"));
        demarrerPartie();
        return true;
    }

    private String nomValide(String nom, String parDefaut) {
        if (nom == null || nom.trim().isEmpty()) return parDefaut;
        return nom.trim();
    }

    // Remet la partie a zero (meme reglage). Le joueur 1 commence (RG6).
    public void demarrerPartie() {
        nbAllumettes = nbDepart;
        joueurCourant = 0;
        vainqueur = AUCUN;
        historique.clear();
    }

    // Joue UN coup pour le joueur courant. Retourne false si le coup est refuse (RG3, RG7).
    public boolean jouerCoup(int nb) {
        if (vainqueur != AUCUN) return false;                    // partie deja finie
        if (nb < 1 || nb > Math.min(3, nbAllumettes)) return false;
        nbAllumettes -= nb;
        historique.add(new int[] { joueurCourant, nb, nbAllumettes });
        if (nbAllumettes == 0) {
            // RG5 : celui qui prend la derniere gagne. En misere, il perd.
            vainqueur = misere ? 1 - joueurCourant : joueurCourant;
        } else {
            joueurCourant = 1 - joueurCourant;                   // RG2 : chacun son tour
        }
        return true;
    }

    // C'est a l'ordinateur de jouer (un seul joueur humain, tour du joueur 2)
    public boolean estTourOrdinateur() {
        return nbJoueurs == 1 && joueurCourant == 1 && vainqueur == AUCUN;
    }

    // L'ordinateur choisit son coup selon le niveau, puis on le joue. Retourne le nombre retire.
    public int jouerOrdinateur() {
        int nb;
        if (niveau == 2) {
            nb = Ordinateur.propositionOrdinateurDifficile(nbAllumettes, misere);
        } else {
            nb = Ordinateur.propositionOrdinateur(nbAllumettes);
        }
        jouerCoup(nb);
        return nb;
    }

    // Aide pour le joueur (bouton Indice) : meilleur coup, sans rien afficher
    public int conseillerCoup() {
        return Ordinateur.meilleurCoup(nbAllumettes, misere);
    }

    public boolean estPositionPerdante() {
        return Ordinateur.estPositionPerdante(nbAllumettes, misere);
    }

    // Lecture de l'etat de la partie
    public int getNbAllumettes() { return nbAllumettes; }
    public int getNbDepart() { return nbDepart; }
    public int getNiveau() { return niveau; }
    public int getNbJoueurs() { return nbJoueurs; }
    public boolean isMisere() { return misere; }
    public Joueur getJoueur(int index) { return index == 0 ? joueur1 : joueur2; }
    public int getJoueurCourant() { return joueurCourant; }
    public int getVainqueur() { return vainqueur; }
    public List<int[]> getHistorique() { return historique; }

    private void annoncerVainqueur() {
        if (nbJoueurs == 1) {
            System.out.println(vainqueur == 0 ? "Vous avez gagne !" : "L'ordinateur a gagne !");
        } else {
            System.out.println(getJoueur(vainqueur).getNom() + " a gagne !");
        }
    }

    public void menuJeu() {
        boolean quitter = false;
        while (!quitter) {
            System.out.println();
            System.out.println("=== JEU DES ALLUMETTES ===");
            System.out.println("1. Jouer une partie (" + nbDepart + " allumettes, niveau "
                    + (niveau == 1 ? "facile" : "difficile") + ")");
            System.out.println("2. Changer le nombre d'allumettes");
            System.out.println("3. Changer le niveau");
            System.out.println("4. Quitter");
            System.out.print("Votre choix : ");
            String choix = Joueur.lireLigne().trim();
            if (choix.equals("1")) {
                commencerJeu();
            } else if (choix.equals("2")) {
                System.out.print("Nombre d'allumettes (" + NB_MIN + " a " + NB_MAX + ") : ");
                try {
                    int n = Integer.parseInt(Joueur.lireLigne().trim());
                    if (n >= NB_MIN && n <= NB_MAX) {
                        nbDepart = n;
                    } else {
                        System.out.println("Valeur hors limites, inchangee.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Saisie invalide, valeur inchangee.");
                }
            } else if (choix.equals("3")) {
                niveau = (niveau == 1) ? 2 : 1;
                System.out.println("Niveau : " + (niveau == 1 ? "facile" : "difficile"));
            } else if (choix.equals("4")) {
                quitter = true;
            } else {
                System.out.println("Choix invalide.");
            }
        }
        System.out.println("Au revoir !");
    }
}