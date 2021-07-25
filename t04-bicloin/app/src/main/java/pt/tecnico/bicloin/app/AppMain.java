package pt.tecnico.bicloin.app;

import pt.tecnico.bicloin.hub.HubFrontend;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.util.*;

import static java.lang.Thread.sleep;

public class AppMain {
	
	public static void main(String[] args) throws ZKNamingException, InterruptedException {
		System.out.println(AppMain.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 6) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s path zooHost zooPort%n", AppMain.class.getName());
			return;
		}

		// Arguments received
		final String zooHost = args[0];
		final String zooPort = args[1];
		final String username = args[2];
		final String cellphone = args[3];
		String coordsLat = args[4];
		String coordsLong = args[5];

		// Initialization of the HubFrontend, App and Tag HashMap
		HubFrontend frontend = new HubFrontend(zooHost, zooPort);
		App app = new App(frontend);
		Map<String, List<String>> tags = new HashMap<>();

		// Initialization of the scanner to scan the input from the user/file
		Scanner scanner = new Scanner(System.in);
		String scanned;
		String[] tokens;

		boolean quit = false;


		// While loop that allows the user to input various commands in order to get responses from the server
		try {
			while (!quit) {

				System.out.print("> ");
				System.out.flush();
				scanned = scanner.nextLine();
				tokens = scanned.split(" ");
				switch (tokens[0]) {
					case "balance":
						if (tokens.length == 1) {
							app.balance(username);
						} else {
							System.out.println("ERRO input invalido.");
						}
						break;

					case "top-up":
						if (tokens.length == 2) {
							app.topUp(username, cellphone, Integer.parseInt(tokens[1]));
						} else {
							System.out.println("ERRO input invalido.");
						}
						break;

					case "info":
						if (tokens.length == 2) {
							app.infoStation(tokens[1]);
						} else {
							System.out.println("ERRO input invalido.");
						}
						break;

					case "scan":
						if (tokens.length == 2) {
							if (Integer.parseInt(tokens[1]) <= 0){
								System.out.println("ERRO valor de K invalido.");
							} else {
								app.locateStation(coordsLat, coordsLong, Integer.parseInt(tokens[1]));
							}
						} else {
							System.out.println("ERRO input invalido.");
						}
						break;

					case "bike-up":
						if (tokens.length == 2) {
							app.bikeUp(username, coordsLat, coordsLong, tokens[1]);
						} else {
							System.out.println("ERRO input invalido.");
						}
						break;

					case "bike-down":
						if (tokens.length == 2) {
							app.bikeDown(username, coordsLat, coordsLong, tokens[1]);
						} else {
							System.out.println("ERRO input invalido.");
						}
						break;

					case "at":
						if (tokens.length == 1) {
							app.at(username, coordsLat, coordsLong);
						} else {
							System.out.println("ERRO input invalido.");
						}
						break;

					case "tag":
						if (tokens.length == 4) {
							tags.put(tokens[3], new ArrayList<>(Arrays.asList(tokens[1], tokens[2])));
							System.out.println("OK");
						} else {
							System.out.println("ERRO input invalido.");
						}
						break;

					case "move":
						if (tokens.length == 2) {
							try {
								coordsLat = tags.get(tokens[1]).get(0);
								coordsLong = tags.get(tokens[1]).get(1);
								app.at(username, coordsLat, coordsLong);
							} catch (Exception e){
								System.out.println("ERRO tag nao encontrada");
							}
						} else if (tokens.length == 3) {
							coordsLat = tokens[1];
							coordsLong = tokens[2];
							app.at(username, coordsLat, coordsLong);
						} else {
							System.out.println("ERRO input invalido.");
						}
						break;

					case "zzz":
						if (tokens.length == 2) {
							sleep(Integer.parseInt(tokens[1]));
							System.out.println("Sleep " + tokens[1] + " milissegundos.");
						} else {
							System.out.println("ERRO input invalido.");
						}
						break;

					case "sys_status":
						if (tokens.length == 1){
							app.sysStatus();
						} else {
							System.out.println("ERRO input invalido.");
						}
						break;

					case "quit":
						quit = true;
						System.out.println("Exiting the app.");
						break;

					case "ping":
						if (tokens.length == 1){
							app.ping();
						} else {
							System.out.println("ERRO input invalido.");
						}
						break;

					case "help":
						System.out.println("Estes sao os comandos disponiveis: \n" +
								"- balance       (retorna o balance do utilizador) \n" +
								"- top-up X      (acrescenta X em BIC na conta do utilizador) \n" +
								"- info X        (mostra informacao sobre a estacao X) \n" +
								"- scan X        (mostra as X estacoes mais proximas do utilizador) \n" +
								"- bike-up X     (levanta uma bicicleta da estacao X) \n" +
								"- bike-down X   (devolve uma bicileta na estacao X) \n" +
								"- at            (apresenta um link do google maps com as coordenadas do utilizador) \n" +
								"- tag X Y Z     (cria uma tag nas coordenadas (X,Y) com o nome Z) \n" +
								"- move X        (move o utilizador para a tag com nome X) \n" +
								"- move X Y      (move o utilizador para as coordenadas (X,Y)) \n" +
								"- sys_status    (mostra os servidores que existem e se estao UP ou DOWN) \n" +
								"- ping          (retorna uma mensagem Pong. do servidor) \n" +
								"- quit          (fecha a aplicacao)");
						break;

					default:
						System.out.println("ERRO input invalido ou comentario.");
						break;
				}
			}
		} catch (Exception e){
			System.out.println("Fim do ficheiro.");
		}
	}
}
