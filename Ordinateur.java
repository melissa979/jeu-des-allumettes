import java.util.Random;

class Ordinateur {
    private static final Random alea = new Random();

    // Version du sujet : choix aleatoire entre 1 et 3 (F1)
    public static int propositionOrdinateur() {
        return propositionOrdinateur(3);
    }

    // RG7 : le choix ne depasse pas le nombre d'allumettes restantes
    public static int propositionOrdinateur(int max) {
        int limite = Math.min(3, max);
        int i = 1 + alea.nextInt(limite);
        System.out.println("L'ordinateur a choisi le nombre " + i);
        return i;
    }

    // Niveau difficile (partie normale) : laisser un multiple de 4 allumettes au joueur
    public static int propositionOrdinateurDifficile(int restantes) {
        return propositionOrdinateurDifficile(restantes, false);
    }

    // Niveau difficile, avec ou sans mode misere
    public static int propositionOrdinateurDifficile(int restantes, boolean misere) {
        int i;
        if (estPositionPerdante(restantes, misere)) {
            // Position perdue : aucun bon coup, on joue au hasard
            i = 1 + alea.nextInt(Math.min(3, restantes));
        } else {
            i = meilleurCoup(restantes, misere);
        }
        System.out.println("L'ordinateur a choisi le nombre " + i);
        return i;
    }

    // La position est perdue pour celui qui doit jouer (si l'adversaire joue bien)
    public static boolean estPositionPerdante(int restantes, boolean misere) {
        if (misere) {
            return (restantes - 1) % 4 == 0;   // 1, 5, 9, 13...
        }
        return restantes % 4 == 0;             // 4, 8, 12, 16...
    }

    // Meilleur coup possible. N'affiche rien : sert aussi pour le bouton Indice.
    public static int meilleurCoup(int restantes, boolean misere) {
        if (!misere) {
            if (restantes <= 3) {
                return restantes;              // on prend tout : c'est gagnant
            }
            int reste = restantes % 4;
            return reste == 0 ? 3 : reste;     // 0 : position perdue, on joue 3
        }
        if (restantes == 1) {
            return 1;                          // obligatoire (position perdue)
        }
        int r = (restantes - 1) % 4;
        return r == 0 ? 1 : r;                 // 0 : position perdue, on joue 1
    }
}