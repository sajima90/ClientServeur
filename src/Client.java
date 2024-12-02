import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

	// Détection de l'OS
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    // Code couleurs pour les terminaux
    public static final String RESET = IS_WINDOWS ? "" : "\033[0m";  // Réinitialise la couleur
    public static final String BLACK = IS_WINDOWS ? "" : "\033[30m"; // Noir
    public static final String RED = IS_WINDOWS ? "" : "\033[31m";   // Rouge
    public static final String GREEN = IS_WINDOWS ? "" : "\033[32m"; // Vert
    public static final String YELLOW = IS_WINDOWS ? "" : "\033[33m"; // Jaune
    public static final String BLUE = IS_WINDOWS ? "" : "\033[34m";  // Bleu
    public static final String MAGENTA = IS_WINDOWS ? "" : "\033[35m"; // Magenta
    public static final String CYAN = IS_WINDOWS ? "" : "\033[36m";   // Cyan
    public static final String WHITE = IS_WINDOWS ? "" : "\033[37m";  // Blanc
    public static final String GREY = IS_WINDOWS ? "" : "\033[90m";   // Gris (clair)
    public static final String ITALIC = IS_WINDOWS ? "" : "\033[3m";  // Italique
    public static final String BOLD = IS_WINDOWS ? "" : "\033[1m";    // Gras

    private static final String SERVEUR = "localhost";
    private static final int PORT = 3000;

	public static void main(String[] args) {
		String server = SERVEUR;
		int port = PORT;

		try {
			if (args.length == 0) {
				System.out.println(RED + "---------------------------------------------------" + RESET);
				System.out.println(GREEN + "Astuce : Vous pouvez vous connecter à un autre serveur en passant ses arguments !" + RESET);
				System.out.println(GREEN + "Exemple (cette commande est valide) : "+ YELLOW +"java Client sajima.fr 6000" + RESET);
				System.out.println(CYAN + "Connexion par défaut au serveur : " + server + " sur le port : " + port + RESET);
			} else if (args.length == 2) {
				server = args[0];
				try {
					port = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					System.err.println("Erreur : Le port doit être un entier. Utilisation du port par défaut : " + PORT);
					port = PORT;
				}
			} else {
				System.err.println("Erreur : Nombre d'arguments invalide !");
				System.err.println("Utilisation : java Client <serveur> <port>");
				return;
			}

			Socket socket = new Socket(server, port);
			System.out.println("Connecté au serveur " + BLUE + server + RESET + " sur le port " + BLUE + port + RESET);

			new Thread(new RecevoirMessage(socket)).start();
			new Thread(new EnvoyerMessage(socket)).start();

		} catch (IOException e) {
			System.err.println("Erreur de connexion au serveur : " + e.getMessage());
		}
	}
}


class RecevoirMessage implements Runnable
{
	private BufferedReader in;
	private Socket socket;

	public RecevoirMessage(Socket socket) throws IOException
	{
		this.socket = socket;
		this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	public void run()
	{
		try
		{
			String message;
			while ((message = in.readLine()) != null)
			{
				System.out.println(message);
			}
		} catch (IOException e)
		{
			if (!socket.isClosed())
			{
				System.err.println("Erreur de réception: " + e.getMessage());
			}
		}
	}
}

class EnvoyerMessage implements Runnable {
	private PrintWriter out;
	private Socket socket;
	private BufferedReader consoleIn;

	public EnvoyerMessage(Socket socket) throws IOException {
		this.socket = socket;
		this.out = new PrintWriter(socket.getOutputStream(), true);
		this.consoleIn = new BufferedReader(new InputStreamReader(System.in));
	}

	public void run() {
		try {
			String message;
			while ((message = this.consoleIn.readLine()) != null) {
				if (message.trim().isEmpty()) {
					continue;
				}

				this.out.println(message);

				// Supprimer le message brut du terminal et afficher correctement
				System.out.print("\033[1A\033[2K"); // Efface la ligne au-dessus // Internet

				System.out.println( Client.GREY + "[ Vous ] : " + Client.RESET + message);

				if (message.equals("/quit")) {
					this.socket.close();
					break;
				}
			}
		} catch (IOException e) {
			if (!this.socket.isClosed()) {
				System.err.println("Erreur d'envoi: " + e.getMessage());
			}
		}
	}
}
