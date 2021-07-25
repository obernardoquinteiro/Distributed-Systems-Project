package pt.tecnico.bicloin.hub;


import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.bicloin.hub.domain.Station;
import pt.tecnico.bicloin.hub.domain.User;
import pt.tecnico.rec.RecFrontend;
import pt.tecnico.rec.RecFrontendQuorum;
import pt.tecnico.rec.grpc.WriteRequest;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class HubMain {

	static List<User> userList = new ArrayList<User>();
	static List<Station> stationList = new ArrayList<Station>();
	static RecFrontendQuorum record;
	
	public static void main(String[] args) throws ZKNamingException, IOException, InterruptedException, CsvException {
		System.out.println(HubMain.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 4) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s zooHost zooPort path host port%n", HubMain.class.getName());
			return;
		}

		final BindableService impl = new HubServiceImpl();

		final String zooHost = "localhost";
		final String zooPort = "2181";
		final String host = "localhost";
		final int port = Integer.parseInt(args[0]);
		final String path = "/grpc/bicloin/hub/" + args[1];
		final String userscsv = args[2];
		final String stationscsv = args[3];
		boolean initRec = false;

		// check if initRec is an argument
		if (args.length == 5){
			initRec = true;
		}

		record = new RecFrontendQuorum(zooHost, zooPort);
		Instant startR = Instant.now();

		// Read from User csv file and populate Rec in case of initRec as argument
		try (CSVReader reader = new CSVReader(new FileReader("../demo/" + userscsv))) {
			List<String[]> r = reader.readAll();
			for (String[] string : r){
				userList.add(new User(string[0], string[1], string[2]));
				if (initRec){
					record.write(WriteRequest.newBuilder().setId("u/" + string[0] + "/balance").setValue(0).setCid(1).build());
					record.write(WriteRequest.newBuilder().setId("u/" + string[0] + "/hasBike").setValue(0).setCid(1).build());
				}
			}
		}


		// Read from Station csv file and populate Rec in case of initRec as argument
		try (CSVReader reader = new CSVReader(new FileReader("../demo/" + stationscsv))) {
			List<String[]> r = reader.readAll();
			for (String[] string : r){
				stationList.add(new Station(string[0], string[1], string[2], string[3], Integer.parseInt(string[4]), Integer.parseInt(string[6])));
				if (initRec){
					record.write(WriteRequest.newBuilder().setId("s/" + string[1] + "/bikes").setValue(Integer.parseInt(string[5])).setCid(1).build());
					record.write(WriteRequest.newBuilder().setId("s/" + string[1] + "/levantamentos").setValue(0).setCid(1).build());
					record.write(WriteRequest.newBuilder().setId("s/" + string[1] + "/devolucoes").setValue(0).setCid(1).build());
				}
			}
		}

		Instant endR = Instant.now();
		Duration timeElapsedR = Duration.between(startR, endR);
		System.out.println("INITREC TIME: " + timeElapsedR);

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
			server.awaitTermination();

		} finally {
			if (zkNaming != null) {
				// remove
				zkNaming.unbind(path,host, String.valueOf(port));
			}
		}
	}
}
