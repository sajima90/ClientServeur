import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

public class Serveur implements Observer
{
    private static final int PORT = 3000;
    private ServerSocket serveurSocket;

    public Serveur()
    {
        try {
            serveurSocket = new ServerSocket(PORT);
            System.out.println("Serveur démarré sur le port " + PORT);
            demarrerServeur();
        } catch (IOException e) {
            System.err.println("Erreur lors du démarrage du serveur: " + e.getMessage());
        }
    }

    private void demarrerServeur()
    {
        try {
            System.out.println("En attente de connexions...");
            while (!serveurSocket.isClosed()) {
                Socket socketClient = serveurSocket.accept();

                GerantDeClient gestionnaire = new GerantDeClient(socketClient);
                gestionnaire.addObserver(this);
                new Thread(gestionnaire).start();
            }
        } catch (IOException e) {
            fermerServeur();
        }
    }

    public void update(Observable o, Object arg) {
        if (o instanceof GerantDeClient) {
            GerantDeClient client = (GerantDeClient) o;
            System.out.println("Client connecté " +
                    " | Pseudo: "    + client.getNom() +
                    " | Adresse: "   + client.getSocket().getInetAddress().getHostAddress() +
                    " | Port: "      + client.getSocket().getPort()
            );
        }
    }

    private void fermerServeur() {
        try {
            if (serveurSocket != null && !serveurSocket.isClosed()) {
                serveurSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la fermeture du serveur: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new Serveur();
    }
}
