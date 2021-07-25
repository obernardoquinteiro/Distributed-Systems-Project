package pt.tecnico.rec;


import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import sun.misc.Signal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RecordMain {

	static Map<String, Integer[]> records = new HashMap<>();

	public static void main(String[] args) throws IOException, InterruptedException, ZKNamingException {
		System.out.println(RecordMain.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s zooHost zooPort path host port%n", RecordMain.class.getName());
			return;
		}

		final BindableService impl = new RecServiceImpl();

		final String zooHost = "localhost";
		final String zooPort = "2181";
		final String host = "localhost";
		final int port = Integer.parseInt(args[0]);
		final String path = "/grpc/bicloin/rec/" + args[1];

		ZKNaming zkNaming = null;

		try {
			zkNaming = new ZKNaming(zooHost, zooPort);
			// publish
			zkNaming.rebind(path, host, Integer.toString(port));

			// Create a new server to listen on port
			Server server = ServerBuilder.forPort(port).addService(impl).build();

			// Start the server
			server.start();

			// Server threads are running in the background.
			System.out.println("Server started");

			// Do not exit the main thread. Wait until server is terminated.
			Signal.handle(new Signal("INT"),  // SIGINT
					signal -> server.shutdown());
			server.awaitTermination();

		} finally {
			if (zkNaming != null) {
				// remove
				zkNaming.unbind(path,host, String.valueOf(port));
			}
		}
	}

}
