package pt.tecnico.bicloin.hub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.bicloin.hub.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class HubFrontend implements AutoCloseable{

    private ManagedChannel channel;
    private HubServiceGrpc.HubServiceBlockingStub stub;
    private final ZKNaming zkNaming;
    private final String path = "/grpc/bicloin/hub";
    private List<ZKRecord> records;
    private String target;
    private PingResponse response;


    /**
     * Creates a frontend that contacts the specified path.
     *
     * @param zooHost Names server host
     * @param zooPort Names server port
     * @param p Names server path
     */

    public HubFrontend(String zooHost, String zooPort, String p) throws ZKNamingException {
        // Channel is the abstraction to connect to a service endpoint.
        // Let us use plaintext communication because we do not have certificates.
        this.zkNaming = new ZKNaming(zooHost, zooPort);

        // lookup
        ZKRecord record = this.zkNaming.lookup(p);
        target = record.getURI();

        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        this.stub = HubServiceGrpc.newBlockingStub(channel);

    }


    /**
     * Creates a frontend that contacts any server in Zookeeper from a certain base path.
     *
     * @param zooHost Names server host
     * @param zooPort Names server port
     */

    public HubFrontend(String zooHost, String zooPort) throws ZKNamingException {
        this.zkNaming = new ZKNaming(zooHost, zooPort);
        records = new ArrayList<>(this.zkNaming.listRecords(this.path));
        ZKRecord record = this.zkNaming.lookup("/grpc/bicloin/hub/1");
        target = record.getURI();
        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        this.stub = HubServiceGrpc.newBlockingStub(channel);
    }


    /**
     * Connects to all the hub servers and rec to check if they are UP or DOWN.
     */

    public SysStatusResponse sysStatus(SysStatusRequest request) throws ZKNamingException{
        List<ZKRecord> records_rec = new ArrayList<>(this.zkNaming.listRecords("/grpc/bicloin/rec"));
        String status = "";
        List<String> servers = new ArrayList<>();

        try {
            response = ping(PingRequest.newBuilder().build());
            if (response.getOutput().equals("Pong.")) {
                status += "Hub Server: " + target + " OK.\n";
            }
        } catch (RuntimeException e) {
            status += "Hub Server: " + target + " DOWN.\n";
        }

        for (ZKRecord record : records_rec){
            servers.add(record.getURI());
        }

        SysStatusRequest request1 = SysStatusRequest.newBuilder().setStatus(status).addAllServers(servers).build();
        return stub.sysStatus(request1);
    }

    // Frontend functions called by the app.

    public PingResponse ping(PingRequest request) { return stub.ping(request); }

    public BalanceResponse getBalance(BalanceRequest request) { return stub.balance(request); }

    public TopUpResponse addSaldo(TopUpRequest request) { return stub.topUp(request); }

    public InfoStationResponse getStationInfo(InfoStationRequest request) { return  stub.infoStation(request); }

    public LocateStationResponse locateStation(LocateStationRequest request) { return stub.locateStation(request); }

    public BikeUpResponse bikeUp(BikeUpRequest request) { return stub.bikeUp(request); }

    public BikeDownResponse bikeDown(BikeDownRequest request) { return stub.bikeDown(request); }

    public GetDistanceResponse getDistance(GetDistanceRequest request) { return stub.getDistance(request); }

    @Override
    public final void close() {
        channel.shutdown();
    }
}
