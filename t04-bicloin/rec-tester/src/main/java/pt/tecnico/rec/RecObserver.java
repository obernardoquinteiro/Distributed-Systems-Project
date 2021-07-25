package pt.tecnico.rec;

import io.grpc.stub.StreamObserver;
import pt.tecnico.rec.grpc.ReadResponse;

import static pt.tecnico.rec.RecFrontendQuorum.finishLatch;

public class RecObserver<R> implements StreamObserver<R> {

    private final String name;
    private final RespCollector collector;

    public RecObserver(String name, RespCollector collector) {
        this.name = name;
        this.collector = collector;
    }

    @Override
    public void onNext(R r) {
        System.out.print("Received response from replica " + name + ": " + r);
        if (this.collector != null) {
            collector.responses.add((ReadResponse) r);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Received error on replica " + name + ": " + throwable);
        finishLatch.countDown();
    }

    @Override
    public void onCompleted() {
        System.out.println("Request completed, " + name);
        finishLatch.countDown();
    }
}
