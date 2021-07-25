package pt.tecnico.rec;


import io.grpc.StatusRuntimeException;
import pt.tecnico.rec.grpc.PingRequest;
import pt.tecnico.rec.grpc.PingResponse;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;


public class RecordTester {

	public static void main(String[] args) throws ZKNamingException {
		System.out.println(RecordTester.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 3) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s path zooHost zooPort%n", RecordTester.class.getName());
			return;
		}

		final String path = args[0];
		final String zooHost = args[1];
		final String zooPort = args[2];

		RecFrontendQuorum frontend = new RecFrontendQuorum(zooHost, zooPort, path);

		try {
			PingRequest request = PingRequest.newBuilder().build();
			PingResponse response = frontend.ping(request);
			System.out.println(response.getOutput());
		} catch (StatusRuntimeException ze) {
			System.out.println("Servidor nao esta a responder.");
		}

		frontend.close();

	}

}
