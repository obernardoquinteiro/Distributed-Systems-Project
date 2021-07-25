package pt.tecnico.rec;

import io.grpc.stub.StreamObserver;
import pt.tecnico.rec.grpc.*;

import static io.grpc.Status.INVALID_ARGUMENT;
import static pt.tecnico.rec.RecordMain.records;

public class RecServiceImpl extends RecordServiceGrpc.RecordServiceImplBase {

    /**
     * Receives a PingRequest and returns the server response.
     *
     * @param request PingRequest
     */

    public synchronized void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        try {
            PingResponse response = PingResponse.newBuilder().setOutput("Pong.").build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch(Exception e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Servidor nao esta a responder").asRuntimeException());
        }
    }


    /**
     * Receives a ReadRequest and returns the server response with the value found on the map "records" on certain ID.
     *
     * @param request ReadRequest (contains id)
     */

    public synchronized void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
        String id = request.getId();
        if (id.isBlank()) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("ID not found.").asRuntimeException());
        }
        int value, seq, cid;
        if (records.containsKey(id)){
            value = records.get(id)[0];
            seq = records.get(id)[1];
            cid = records.get(id)[2];
        }
        else {
            records.put(id, new Integer[]{-1, -1, -1});
            value = -1;
            seq = -1;
            cid = 1;
        }
        responseObserver.onNext(ReadResponse.newBuilder().setValue(value).setSeq(seq).setCid(cid).build());
        responseObserver.onCompleted();
    }


    /**
     * Receives a WriteRequest and writes on the map "records" the info received.
     *
     * @param request WriteRequest (contains id and value)
     */

    public synchronized void write(WriteRequest request, StreamObserver<WriteResponse> responseObserver){
        String id = request.getId();
        int value = request.getValue();
        int seq = request.getSeq();
        int cid = request.getCid();
        if (seq + 1 > records.get(id)[1]) {
            records.put(id, new Integer[]{value, seq + 1, cid});
        } else if (seq + 1 == records.get(id)[1] && cid > records.get(id)[1]){
            records.put(id, new Integer[]{value, seq + 1, records.get(id)[2]});
        }
        responseObserver.onNext(WriteResponse.newBuilder().setSeq(records.get(id)[1]).setCid(records.get(id)[2]).build());
        responseObserver.onCompleted();
    }
}
