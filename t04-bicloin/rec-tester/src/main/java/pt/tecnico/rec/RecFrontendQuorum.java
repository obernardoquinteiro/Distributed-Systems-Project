package pt.tecnico.rec;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.rec.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class RecFrontendQuorum implements AutoCloseable {

    public static CountDownLatch finishLatch;
    private ManagedChannel channel;
    private RecordServiceGrpc.RecordServiceStub stub;
    private final ZKNaming zkNaming;
    private final String path = "/grpc/bicloin/rec";
    private int quorum;
    private List<ZKRecord> records;
    private int cont = 0;


    /**
     * Creates a frontend that contacts the specified path.
     *
     * @param zooHost Names server host
     * @param zooPort Names server port
     * @param p Names server path
     */

    public RecFrontendQuorum(String zooHost, String zooPort, String p) throws ZKNamingException {

        this.zkNaming = new ZKNaming(zooHost,zooPort);

        ZKRecord record = this.zkNaming.lookup(p);
        String target = record.getURI();
        records = new ArrayList<>(this.zkNaming.listRecords(this.path));
        quorum = records.size()/2 + 1;

        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        this.stub = RecordServiceGrpc.newStub(channel);
    }


    /**
     * Creates a frontend that contacts any server in Zookeeper from a certain base path.
     *
     * @param zooHost Names server host
     * @param zooPort Names server port
     */

    public RecFrontendQuorum(String zooHost, String zooPort) throws ZKNamingException {
        this.zkNaming = new ZKNaming(zooHost, zooPort);

        records = new ArrayList<>(this.zkNaming.listRecords(this.path));
        quorum = records.size()/2 + 1;

        int rnd = new Random().nextInt(records.size());

        String target = records.get(rnd).getURI();
        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        this.stub = RecordServiceGrpc.newStub(channel);
    }


    // Frontend functions called by the hub.
    public PingResponse ping(PingRequest request) {
        if (!request.getUri().isBlank()) {
            this.channel = ManagedChannelBuilder.forTarget(request.getUri()).usePlaintext().build();
        }
        RecordServiceGrpc.RecordServiceBlockingStub stub1 = RecordServiceGrpc.newBlockingStub(channel);
        return stub1.ping(request);
    }


    /* Write method,
        - firstly invokes read method to get most updated registry;
        - then updates registries on all servers with the new value and seq.
     */

    public WriteResponse write(WriteRequest request) {
        Instant startW = Instant.now();
        ReadRequest readRequest = ReadRequest.newBuilder().setId(request.getId()).build();
        ReadResponse readResponse = read(readRequest);
        WriteRequest request1 = WriteRequest.newBuilder().setSeq(readResponse.getSeq()).setId(request.getId()).setValue(request.getValue()).setCid(request.getCid()).build();
        try {
            finishLatch = new CountDownLatch(records.size());
            for (ZKRecord record : records) {
                this.channel = ManagedChannelBuilder.forTarget(record.getURI()).usePlaintext().build();
                this.stub = RecordServiceGrpc.newStub(channel);
                System.out.println("WRITE: Contacting replica at " + record.getURI());
                stub.write(request1, new RecObserver<>(record.getURI(), null));
                close();
            }
            finishLatch.await();
        } catch (InterruptedException ze) {
            System.out.println("ERROR : No servers available!");
        }
        Instant endW = Instant.now();
        Duration timeElapsedW = Duration.between(startW, endW);
        System.out.println("WRITE TIME: " + timeElapsedW);
        return null;
    }

    // Read method sends requests to all rec servers and returns the most updated value to the client.

    public ReadResponse read(ReadRequest request) {
        Instant start = Instant.now();
        System.out.println("\nPEDIDO " +  cont + " ID: " + request.getId());
        cont++;
        RespCollector respColl = new RespCollector(quorum);
        try {
            finishLatch = new CountDownLatch(records.size());
            for (ZKRecord record : records) {
                this.channel = ManagedChannelBuilder.forTarget(record.getURI()).usePlaintext().build();
                this.stub = RecordServiceGrpc.newStub(channel);
                System.out.println("READ: Contacting replica at " + record.getURI());
                stub.read(request, new RecObserver<>(record.getURI(), respColl));
                close();
            }
            finishLatch.await();
        } catch (InterruptedException ze) {
            System.out.println("ERROR : No servers available!");
        }
        if (respColl.getQuorumList().isEmpty()){
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT);
        }
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        System.out.println("READ TIME: " + timeElapsed);
        return respColl.getQuorumList().stream().max(Comparator.comparing(ReadResponse::getSeq).thenComparing(ReadResponse::getCid)).get();
    }

    @Override
    public final void close() {
        channel.shutdown();
    }
}
