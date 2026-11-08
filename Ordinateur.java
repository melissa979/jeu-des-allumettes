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

    // Niveau difficile : laisser un multiple de 4 allumettes au joueur
    public static int propositionOrdinateurDifficile(int restantes) {
        int i = restantes % 4;
        if (i == 0) {
            i = 1 + alea.nextInt(Math.min(3, restantes));
        }
        System.out.println("L'ordinateur a choisi le nombre " + i);
        return i;
    }
}
