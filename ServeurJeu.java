import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Pont entre l'interface HTML et les classes Java.
// Le navigateur ne peut pas appeler Java directement : il envoie des requetes HTTP
// a ce petit serveur, qui les transmet a l'objet Jeu et renvoie l'etat de la partie.
// Aucune regle du jeu n'est ecrite ici : tout est dans Jeu, Joueur et Ordinateur.
public class ServeurJeu {
    private static final int PORT = 8080;

    private final Jeu jeu = new Jeu();   // l'unique partie en cours

    public static void main(String[] args) throws IOException {
        if (!Files.exists(Path.of("index.html"))) {
            System.out.println("index.html introuvable : lancez le serveur depuis le dossier du projet.");
            return;
        }
        ServeurJeu serveur = new ServeurJeu();
        try {
            // Ecoute uniquement sur cet ordinateur (adresse 127.0.0.1)
            HttpServer http = HttpServer.create(
                    new InetSocketAddress(InetAddress.getLoopbackAddress(), PORT), 0);
            http.createContext("/", serveur::traiter);
            http.start();
            System.out.println("Jeu des allumettes : ouvrez http://127.0.0.1:" + PORT);
            System.out.println("(Ctrl+C pour arreter le serveur)");
        } catch (BindException e) {
            System.out.println("Le port " + PORT + " est deja utilise. Fermez l'autre serveur et recommencez.");
        }
    }

    // ------------------------------------------------------------------
    // Aiguillage : chaque adresse correspond a une action sur l'objet Jeu
    // ------------------------------------------------------------------
    private synchronized void traiter(HttpExchange echange) throws IOException {
        try {
            String chemin = echange.getRequestURI().getPath();
            String methode = echange.getRequestMethod();

            if (chemin.equals("/") || chemin.equals("/index.html")) {
                envoyerPage(echange);
            } else if (chemin.equals("/favicon.ico")) {
                echange.sendResponseHeaders(204, -1);
            } else if (chemin.equals("/api/etat") && methode.equals("GET")) {
                envoyer(echange, 200, etatJson());
            } else if (chemin.equals("/api/indice") && methode.equals("GET")) {
                indice(echange);
            } else if (chemin.equals("/api/nouvelle") && methode.equals("POST")) {
                nouvelle(echange);
            } else if (chemin.equals("/api/jouer") && methode.equals("POST")) {
                jouer(echange);
            } else if (chemin.equals("/api/ordinateur") && methode.equals("POST")) {
                ordinateur(echange);
            } else if (chemin.equals("/api/rejouer") && methode.equals("POST")) {
                jeu.demarrerPartie();
                envoyer(echange, 200, etatJson());
            } else if (chemin.startsWith("/api/")) {
                erreur(echange, 405, "Action inconnue ou mauvaise methode HTTP.");
            } else {
                erreur(echange, 404, "Page introuvable.");
            }
        } catch (RuntimeException e) {
            erreur(echange, 500, "Erreur interne du serveur.");
        } finally {
            echange.close();
        }
    }

    // POST /api/nouvelle : total, mode (deux | machine | ia), misere, nom1, nom2
    private void nouvelle(HttpExchange echange) throws IOException {
        Map<String, String> p = lireFormulaire(echange);
        String mode = p.getOrDefault("mode", "");
        int nbJoueurs = mode.equals("deux") ? 2 : 1;
        int niveau = mode.equals("ia") ? 2 : 1;
        boolean modeValide = mode.equals("deux") || mode.equals("machine") || mode.equals("ia");

        boolean ok = modeValide && jeu.configurer(
                entier(p, "total"), nbJoueurs, niveau,
                p.getOrDefault("misere", "false").equals("true"),
                p.get("nom1"), p.get("nom2"));
        if (ok) {
            envoyer(echange, 200, etatJson());
        } else {
            erreur(echange, 400, "Parametres de partie invalides.");
        }
    }

    // POST /api/jouer : nb (le coup d'un joueur humain)
    private void jouer(HttpExchange echange) throws IOException {
        Map<String, String> p = lireFormulaire(echange);
        if (jeu.estTourOrdinateur()) {
            erreur(echange, 400, "C'est le tour de l'ordinateur.");
        } else if (jeu.jouerCoup(entier(p, "nb"))) {
            envoyer(echange, 200, etatJson());
        } else {
            erreur(echange, 400, "Coup invalide.");
        }
    }

    // POST /api/ordinateur : l'ordinateur joue son coup
    private void ordinateur(HttpExchange echange) throws IOException {
        if (jeu.estTourOrdinateur()) {
            jeu.jouerOrdinateur();
            envoyer(echange, 200, etatJson());
        } else {
            erreur(echange, 400, "Ce n'est pas le tour de l'ordinateur.");
        }
    }

    // GET /api/indice : meilleur coup pour le joueur courant
    private void indice(HttpExchange echange) throws IOException {
        if (jeu.getVainqueur() != Jeu.AUCUN || jeu.estTourOrdinateur()) {
            erreur(echange, 400, "Pas d'indice disponible maintenant.");
            return;
        }
        String json = "{\"coup\":" + jeu.conseillerCoup()
                + ",\"perdante\":" + jeu.estPositionPerdante() + "}";
        envoyer(echange, 200, json);
    }

    // ------------------------------------------------------------------
    // Etat de la partie, ecrit en JSON (sans bibliotheque)
    // ------------------------------------------------------------------
    private String etatJson() {
        String mode = jeu.getNbJoueurs() == 2 ? "deux" : (jeu.getNiveau() == 2 ? "ia" : "machine");
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"restantes\":").append(jeu.getNbAllumettes());
        sb.append(",\"total\":").append(jeu.getNbDepart());
        sb.append(",\"mode\":\"").append(mode).append("\"");
        sb.append(",\"misere\":").append(jeu.isMisere());
        sb.append(",\"noms\":[\"").append(echapper(jeu.getJoueur(0).getNom()))
          .append("\",\"").append(echapper(jeu.getJoueur(1).getNom())).append("\"]");
        sb.append(",\"courant\":").append(jeu.getJoueurCourant());
        sb.append(",\"vainqueur\":").append(jeu.getVainqueur());
        sb.append(",\"tourOrdinateur\":").append(jeu.estTourOrdinateur());
        sb.append(",\"historique\":[");
        List<int[]> coups = jeu.getHistorique();
        for (int i = 0; i < coups.size(); i++) {
            int[] c = coups.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"joueur\":").append(c[0]).append(",\"nb\":").append(c[1])
              .append(",\"reste\":").append(c[2]).append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    // Les noms saisis peuvent contenir des guillemets : on les protege pour garder un JSON valide
    private String echapper(String texte) {
        StringBuilder sb = new StringBuilder();
        for (char c : texte.toCharArray()) {
            if (c == '"') sb.append("\\\"");
            else if (c == '\\') sb.append("\\\\");
            else if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
            else sb.append(c);
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Outils HTTP
    // ------------------------------------------------------------------
    private Map<String, String> lireFormulaire(HttpExchange echange) throws IOException {
        String corps = new String(echange.getRequestBody().readNBytes(10_000), StandardCharsets.UTF_8);
        Map<String, String> resultat = new HashMap<>();
        for (String paire : corps.split("&")) {
            int egal = paire.indexOf('=');
            if (egal > 0) {
                resultat.put(URLDecoder.decode(paire.substring(0, egal), StandardCharsets.UTF_8),
                             URLDecoder.decode(paire.substring(egal + 1), StandardCharsets.UTF_8));
            }
        }
        return resultat;
    }

    // Lit un entier ; retourne -1 s'il est absent ou invalide (Jeu le refusera)
    private int entier(Map<String, String> p, String cle) {
        try {
            return Integer.parseInt(p.getOrDefault(cle, "").trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void envoyerPage(HttpExchange echange) throws IOException {
        byte[] page = Files.readAllBytes(Path.of("index.html"));
        echange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        echange.getResponseHeaders().set("Cache-Control", "no-store");
        echange.sendResponseHeaders(200, page.length);
        echange.getResponseBody().write(page);
    }

    private void envoyer(HttpExchange echange, int code, String json) throws IOException {
        byte[] octets = json.getBytes(StandardCharsets.UTF_8);
        echange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        echange.getResponseHeaders().set("Cache-Control", "no-store");
        echange.sendResponseHeaders(code, octets.length);
        echange.getResponseBody().write(octets);
    }

    private void erreur(HttpExchange echange, int code, String message) throws IOException {
        envoyer(echange, code, "{\"erreur\":\"" + echapper(message) + "\"}");
    }
}