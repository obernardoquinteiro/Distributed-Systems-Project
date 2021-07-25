package pt.tecnico.rec;

import pt.tecnico.rec.grpc.ReadResponse;

import java.util.ArrayList;
import java.util.List;

public class RespCollector {

    public List<ReadResponse> responses = new ArrayList<>();
    private final int quorum;

    public RespCollector(int quorum) {
        this.quorum = quorum;
    }

    public List<ReadResponse> getQuorumList(){
        while(responses.size() > quorum){
            responses.remove(responses.size() - 1);
        }
        return responses;
    }
}
