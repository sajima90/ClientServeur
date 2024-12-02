import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;

public class GerantDeClient extends Observable implements Runnable {

    private static ArrayList<GerantDeClient> tabClient = new ArrayList<GerantDeClient>();
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String nom;
    private boolean premiereConnexion;
    // list de commande
    public static String[] listCommandes = {"help", "list", "msg", "quit"};

    public GerantDeClient(Socket socket) throws IOException {
        this.socket = socket;
        this.premiereConnexion = false;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        GerantDeClient.tabClient.add(this);
    }


    public String getNom() { return this.nom; }
    public Socket getSocket() { return this.socket; }

    @Override
    public void run() {
        String message = "";
        try {
            if (!premiereConnexion) {
                out.println("Tapez un pseudo : ");
                while (true) {
                    message = this.in.readLine();
                    if (message != null && !message.trim().isEmpty()) {
                        boolean pseudoPris = false;
                        for (GerantDeClient client : tabClient) {
                            if (!client.equals(this) && message.equalsIgnoreCase(client.nom)) {
                                pseudoPris = true;
                                break;
                            }
                        }

                        if (pseudoPris) {
                            this.out.println( Client.RED + "Le pseudo est déjà pris, merci de choisir un autre pseudo : " + Client.RESET);
                        } else {
                            this.nom = message;
                            this.out.println( Client.GREEN + Client.ITALIC + "Pseudo accepté : " + this.nom + Client.RESET);
                            this.premiereConnexion = true;

                            super.setChanged();
                            super.notifyObservers();

                            this.sendMessage(Client.GREEN + Client.ITALIC +this.nom + " vient de se connecter" + Client.RESET, "start");
                            break;
                        }
                    }
                }
            }

            while ((message = this.in.readLine()) != null) {
                if (message.equals("quit")) {
                    this.sendMessage(Client.GREY + Client.ITALIC + this.nom + " vient de se déconnecter" + Client.RESET, "quit");
                    break;
                } else if (message.equals("/list")) {
                    envoyerListeUtilisateurs();
                } else if (message.startsWith("/msg")){
                    traiterMessagePrive(message);
                }else if (message.startsWith("/help")){
                    infoCommandes();
                }else {
                    this.sendMessage(message);
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur de communication avec le client: " + e.getMessage());
        } finally {
            try {
                tabClient.remove(this);
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                    System.out.println("Client " + this.nom + " déconnecté");
                    for (GerantDeClient client : tabClient) {
                        if (!client.equals(this) && client.premiereConnexion)
                        {
                            client.out.println(Client.GREY + Client.ITALIC + "Utilisateur " + this.nom + " nous à quitté" + Client.RESET);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
            }
        }
    }

    private void envoyerListeUtilisateurs() {
        StringBuilder liste = new StringBuilder("Utilisateurs connectés :\n");
        for (GerantDeClient client : tabClient) {
            if (client.nom != null) {

                liste.append("- ").append(client.getNom() == this.nom ? "Vous" : client.getNom()).append("\n");
            }
        }
        this.out.println(liste.toString());
    }
    private void infoCommandes(){
        StringBuilder liste = new StringBuilder("Commandes disponibles :\n");
        for (String commande : listCommandes) {
                liste.append("- ").append(commande).append("\n");
            }
        this.out.println(liste.toString());
    }


    private void traiterMessagePrive(String message) {
        String[] parts = message.split(" ", 3);
        if (parts.length < 3) {
            this.out.println("Utilisation: /msg <nom_utilisateur> <message>");
            return;
        }

        String cibleNom = parts[1];
        String contenuMessage = parts[2];
        GerantDeClient cible = null;

        // Trouver l'utilisateur cible
        for (GerantDeClient client : tabClient) {
            if (client.getNom().equalsIgnoreCase(cibleNom)) {
                cible = client;
                break;
            }
        }

        if (cible != null) {
            cible.out.println("[Message privé de " + this.nom + "] : " + contenuMessage);
            this.out.println("[Message privé à "        + cibleNom + "] : " + contenuMessage);
        } else {
            this.out.println("Utilisateur " + cibleNom + " introuvable.");
        }
    }

    private void sendMessage(String message) {
        for (GerantDeClient client : tabClient) {
            if (!client.equals(this) && client.premiereConnexion) {
                client.out.println(Client.BOLD + "[ " +this.nom + " ]" + Client.RESET+ " : " + message);
            }
        }
    }

    private void sendMessage(String message, String type) {
        for (GerantDeClient client : tabClient) {
            if (!client.equals(this) && client.premiereConnexion) {
                client.out.println(message);
            }
        }
    }
}