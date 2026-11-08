class Jeu {
    int nbAllumettes = 17;
    int nbDepart = 17;   // a ajouter au diagramme de classes (menuJeu)
    int niveau = 1;      // 1 = facile (aleatoire), 2 = difficile (strategie)

    public void afficherAllumettes() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nbAllumettes; i++) {
            sb.append("| ");
        }
        System.out.println(sb.toString().trim() + "   (" + nbAllumettes + ")");
    }

    public void commencerJeu() {
        nbAllumettes = nbDepart;           // retour au nombre de depart a chaque partie
        Joueur joueur = new Joueur();
        boolean tourJoueur = true;         // RG6 : le joueur commence
        String vainqueur = "";
        while (nbAllumettes > 0) {
            afficherAllumettes();
            int nb;
            if (tourJoueur) {
                nb = joueur.propositionJoueur(nbAllumettes);
            } else if (niveau == 2) {
                nb = Ordinateur.propositionOrdinateurDifficile(nbAllumettes);
            } else {
                nb = Ordinateur.propositionOrdinateur(nbAllumettes);
            }
            nbAllumettes -= nb;
            if (nbAllumettes == 0) {
                vainqueur = tourJoueur ? "Vous avez gagne !" : "L'ordinateur a gagne !";
            }
            tourJoueur = !tourJoueur;
        }
        System.out.println(vainqueur);
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
                System.out.print("Nombre d'allumettes (4 a 50) : ");
                try {
                    int n = Integer.parseInt(Joueur.lireLigne().trim());
                    if (n >= 4 && n <= 50) {
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
