package pt.tecnico.rec;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.rec.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class RecFrontend implements AutoCloseable {

    private final ManagedChannel channel;
    private final RecordServiceGrpc.RecordServiceBlockingStub stub;
    private final ZKNaming zkNaming;
    private final String path = "/grpc/bicloin/rec";


    /**
     * Creates a frontend that contacts the specified path.
     *
     * @param zooHost Names server host
     * @param zooPort Names server port
     * @param p Names server path
     */

    public RecFrontend(String zooHost, String zooPort, String p) throws ZKNamingException {

        this.zkNaming = new ZKNaming(zooHost,zooPort);

        ZKRecord record = this.zkNaming.lookup(p);
        String target = record.getURI();

        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        this.stub = RecordServiceGrpc.newBlockingStub(channel);
    }


    /**
     * Creates a frontend that contacts any server in Zookeeper from a certain base path.
     *
     * @param zooHost Names server host
     * @param zooPort Names server port
     */

    public RecFrontend(String zooHost, String zooPort) throws ZKNamingException {
        this.zkNaming = new ZKNaming(zooHost, zooPort);

        List<ZKRecord> records = new ArrayList<>(this.zkNaming.listRecords(this.path));

        int rnd = new Random().nextInt(records.size());

        String target = records.get(rnd).getURI();
        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        this.stub = RecordServiceGrpc.newBlockingStub(channel);
    }


    // Frontend functions called by the hub.
    public PingResponse ping(PingRequest request) {
        return stub.ping(request);
    }

    public WriteResponse write(WriteRequest request) { return stub.write(request); }

    public ReadResponse read(ReadRequest request) { return stub.read(request); }

    @Override
    public final void close() {
        channel.shutdown();
    }
}