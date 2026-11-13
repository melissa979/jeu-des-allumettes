import java.util.Scanner;

class Joueur {
    static final Scanner clavier = new Scanner(System.in);

    private String nom;

    // Constructeur sans argument : garde le comportement du sujet (new Joueur())
    public Joueur() {
        this("Joueur");
    }

    // Constructeur avec le nom du joueur (utilise par l'interface graphique)
    public Joueur(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }

    // Version du sujet : entier entre 1 et 3 (F2, F3)
    public int propositionJoueur() {
        return propositionJoueur(3);
    }

    // RG7 : le joueur ne peut pas retirer plus que ce qui reste
    public int propositionJoueur(int max) {
        int limite = Math.min(3, max);
        int nb = 0;
        boolean valide = false;
        while (!valide) {
            System.out.print("Combien d'allumettes retirez-vous (1 a " + limite + ") ? ");
            String saisie = lireLigne();
            try {
                nb = Integer.parseInt(saisie.trim());
                valide = nb >= 1 && nb <= limite;
            } catch (NumberFormatException e) {
                valide = false;
            }
            if (!valide) {
                System.out.println("Saisie invalide : entrez un nombre entre 1 et " + limite + ".");
            }
        }
        return nb;
    }

    static String lireLigne() {
        if (!clavier.hasNextLine()) {
            System.out.println();
            System.exit(0);
        }
        return clavier.nextLine();
    }
}