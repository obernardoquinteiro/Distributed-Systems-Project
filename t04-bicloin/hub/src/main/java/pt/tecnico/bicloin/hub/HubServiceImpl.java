package pt.tecnico.bicloin.hub;

import io.grpc.stub.StreamObserver;
import pt.tecnico.bicloin.hub.domain.Station;
import pt.tecnico.bicloin.hub.domain.User;
import pt.tecnico.bicloin.hub.grpc.*;
import pt.tecnico.rec.grpc.ReadRequest;
import pt.tecnico.rec.grpc.WriteRequest;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static io.grpc.Status.INVALID_ARGUMENT;
import static pt.tecnico.bicloin.hub.HubMain.*;

public class HubServiceImpl extends HubServiceGrpc.HubServiceImplBase {

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
        } catch (Exception e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Servidor nao esta a responder.").asRuntimeException());
        }
    }


    /**
     * Receives a BalanceRequest and returns the server response with the user balance.
     *
     * @param request BalanceRequest (contains username)
     */

    public synchronized void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver){
        String username = request.getUsername();

        try {
            User user = userList.stream().filter(a -> a.getUsername().equals(username)).collect(Collectors.toList()).get(0);

            BalanceResponse response = BalanceResponse.newBuilder()
                    .setBalance(record.read(ReadRequest.newBuilder().setId("u/" + user.getUsername() + "/balance").build())
                            .getValue()).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("utilizador nao existe.").asRuntimeException());
        }
    }


    /**
     * Receives a TopUpRequest and returns the server response with the user balance after top-up.
     *
     * @param request TopUpRequest (contains username, cellphone and cash)
     */

    public synchronized void topUp(TopUpRequest request, StreamObserver<TopUpResponse> responseObserver){

        String username = request.getUsername();
        String cellphone = request.getCellphone();
        int cash = request.getCash();

        try {
            User user = userList.stream().filter(a -> a.getUsername().equals(username)).collect(Collectors.toList()).get(0);

            int balance = record.read(ReadRequest.newBuilder().setId("u/" + user.getUsername() + "/balance").build()).getValue();

            if (user.getCellphone().equals(cellphone)) {
                if (cash >= 1 && cash <= 20) {
                    balance += cash * 10;
                    record.write(WriteRequest.newBuilder().setId("u/" + user.getUsername() + "/balance").setValue(balance).build());
                    TopUpResponse response = TopUpResponse.newBuilder().setBalance(balance).build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                } else {
                    responseObserver.onError(INVALID_ARGUMENT.withDescription("saldo a submeter deve estar entre 1 e 20.").asRuntimeException());
                }
            } else {
                responseObserver.onError(INVALID_ARGUMENT.withDescription("numero de telemovel nao corresponde ao user.").asRuntimeException());
            }
        } catch (Exception e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("utilizador nao existe.").asRuntimeException());
        }
    }


    /**
     * Receives an InfoStationRequest and returns the server response with info about the selected station.
     *
     * @param request InfoStationRequest (contains abrev)
     */

    public synchronized void infoStation(InfoStationRequest request, StreamObserver<InfoStationResponse> responseObserver){

        String abrev = request.getAbrev();
        String url;

        try {
            Station station = stationList.stream().filter(a -> a.getAbrev().equals(abrev)).collect(Collectors.toList()).get(0);
            url = "https://www.google.com/maps/place/" + station.getCoordsLat() + "," + station.getCoordsLong();
            InfoStationResponse response = InfoStationResponse.newBuilder().setNome(station.getName()).setLat(station.getCoordsLat())
                    .setLong(station.getCoordsLong()).setDocas(station.getDocas()).setPrize(station.getPrize())
                    .setBikes(record.read(ReadRequest.newBuilder().setId("s/" + station.getAbrev() + "/bikes").build()).getValue())
                    .setLevantamentos(record.read(ReadRequest.newBuilder().setId("s/" + station.getAbrev() + "/levantamentos").build()).getValue())
                    .setDevolucoes(record.read(ReadRequest.newBuilder().setId("s/" + station.getAbrev() + "/devolucoes").build()).getValue()).setUrl(url).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("estacao nao existe.").asRuntimeException());
        }
    }


    /**
     * Receives a LocateStationRequest and returns the server response with the abrevs of the k stations closest to the user.
     *
     * @param request LocateStationRequest (contains coordsLat, coordsLong, k)
     */

    public synchronized void locateStation(LocateStationRequest request, StreamObserver<LocateStationResponse> responseObserver){

        double coordsLat = Double.parseDouble(request.getCoordsLat());
        double coordsLong = Double.parseDouble(request.getCoordsLong());
        int k = request.getK();
        List<String> abrevs = new ArrayList<>();

        List<Station> aux = stationList;

        for (Station station : stationList){
            station.setDistance(harvesine(coordsLat, coordsLong, Double.parseDouble(station.getCoordsLat()), Double.parseDouble(station.getCoordsLong())));
        }

        aux.sort(Comparator.comparing(Station::getDistance));

        if (k > stationList.size()){
            k = stationList.size();
        }

        for (int i = 0; i < k; i++){
            abrevs.add(aux.get(i).getAbrev());
        }

        LocateStationResponse response = LocateStationResponse.newBuilder().addAllAbrev(abrevs).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    /**
     * Receives a BikeUpRequest and returns the server response with OK if possible to bike-up.
     *
     * @param request BikeUpRequest (contains username, coordsLat, coordsLong, abrev)
     */

    public synchronized void bikeUp(BikeUpRequest request, StreamObserver<BikeUpResponse> responseObserver){

        String username = request.getUsername();
        double coordsLat = Double.parseDouble(request.getCoordsLat());
        double coordsLong = Double.parseDouble(request.getCoordsLong());
        String abrev = request.getAbrev();
        String rp = "";

        try {
            Station station = stationList.stream().filter(a -> a.getAbrev().equals(abrev)).collect(Collectors.toList()).get(0);
            User user = userList.stream().filter(a -> a.getUsername().equals(username)).collect(Collectors.toList()).get(0);

            //Sets variables on Class User to help verify BikeUp conditions
            int balance = record.read(ReadRequest.newBuilder().setId("u/" + user.getUsername() + "/balance").build()).getValue();
            int hasBike = record.read(ReadRequest.newBuilder().setId("u/" + user.getUsername() + "/hasBike").build()).getValue();

            //Sets variables on Class Station to help verify BikeUp conditions
            int bikes = record.read(ReadRequest.newBuilder().setId("s/" + station.getAbrev() + "/bikes").build()).getValue();
            int levantamentos = record.read(ReadRequest.newBuilder().setId("s/" + station.getAbrev() + "/levantamentos").build()).getValue();

            if (harvesine(coordsLat, coordsLong, Double.parseDouble(station.getCoordsLat()), Double.parseDouble(station.getCoordsLong())) * 1000 < 200) {
                if (balance - 10 >= 0 && hasBike == 0) {
                    if (bikes > 0) {
                        record.write(WriteRequest.newBuilder().setId("u/" + user.getUsername() + "/balance").setValue(balance - 10).build());
                        record.write(WriteRequest.newBuilder().setId("u/" + user.getUsername() + "/hasBike").setValue(1).build());
                        record.write(WriteRequest.newBuilder().setId("s/" + station.getAbrev() + "/bikes").setValue(bikes - 1).build());
                        record.write(WriteRequest.newBuilder().setId("s/" + station.getAbrev() + "/levantamentos").setValue(levantamentos + 1).build());
                        rp = "OK";
                        BikeUpResponse response = BikeUpResponse.newBuilder().setResponse(rp).build();
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                    } else {
                        responseObserver.onError(INVALID_ARGUMENT.withDescription("nao ha mais bicicletas disponiveis.").asRuntimeException());
                    }
                } else {
                    responseObserver.onError(INVALID_ARGUMENT.withDescription("impossivel alugar bike, saldo indisponivel ou ja possui atualmente uma bicicleta.").asRuntimeException());
                }
            } else {
                responseObserver.onError(INVALID_ARGUMENT.withDescription("fora de alcance.").asRuntimeException());
            }
        } catch (Exception e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("estacao nao existe.").asRuntimeException());
        }
    }


    /**
     * Receives a BikeDownRequest and returns the server response with OK if possible to bike-down.
     *
     * @param request BikeDownRequest (contains username, coordsLat, coordsLong, abrev)
     */

    public synchronized void bikeDown(BikeDownRequest request, StreamObserver<BikeDownResponse> responseObserver){

        String username = request.getUsername();
        double coordsLat = Double.parseDouble(request.getCoordsLat());
        double coordsLong = Double.parseDouble(request.getCoordsLong());
        String abrev = request.getAbrev();
        String rp = "";

        try {
            Station station = stationList.stream().filter(a -> a.getAbrev().equals(abrev)).collect(Collectors.toList()).get(0);
            User user = userList.stream().filter(a -> a.getUsername().equals(username)).collect(Collectors.toList()).get(0);

            //Sets variables on Class User to help verify BikeUp conditions
            int balance = record.read(ReadRequest.newBuilder().setId("u/" + user.getUsername() + "/balance").build()).getValue();
            int hasBike = record.read(ReadRequest.newBuilder().setId("u/" + user.getUsername() + "/hasBike").build()).getValue();

            //Sets variables on Class Station to help verify BikeUp conditions
            int bikes = record.read(ReadRequest.newBuilder().setId("s/" + station.getAbrev() + "/bikes").build()).getValue();
            int devolucoes = record.read(ReadRequest.newBuilder().setId("s/" + station.getAbrev() + "/devolucoes").build()).getValue();

            if (harvesine(coordsLat, coordsLong, Double.parseDouble(station.getCoordsLat()), Double.parseDouble(station.getCoordsLong())) * 1000 < 200) {
                if (hasBike == 1) {
                    if (bikes + 1 <= station.getDocas()) {
                        record.write(WriteRequest.newBuilder().setId("u/" + user.getUsername() + "/balance").setValue(balance + station.getPrize()).build());
                        record.write(WriteRequest.newBuilder().setId("u/" + user.getUsername() + "/hasBike").setValue(0).build());
                        record.write(WriteRequest.newBuilder().setId("s/" + station.getAbrev() + "/bikes").setValue(bikes + 1).build());
                        record.write(WriteRequest.newBuilder().setId("s/" + station.getAbrev() + "/devolucoes").setValue(devolucoes + 1).build());
                        rp = "OK";
                        BikeDownResponse response = BikeDownResponse.newBuilder().setResponse(rp).build();
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                    } else {
                        responseObserver.onError(INVALID_ARGUMENT.withDescription("docas estao cheias.").asRuntimeException());
                    }
                } else {
                    responseObserver.onError(INVALID_ARGUMENT.withDescription("nao ha bicicleta para devolver.").asRuntimeException());
                }
            } else {
                responseObserver.onError(INVALID_ARGUMENT.withDescription("fora de alcance").asRuntimeException());
            }
        } catch (Exception e){
            responseObserver.onError(INVALID_ARGUMENT.withDescription("estacao nao existe.").asRuntimeException());
        }
    }


    /**
     * Receives 4 coordinates (2 locations) and returns the distance between those 2 locations.
     *
     * @param lat1 Latitude coordinate 1
     * @param long1 Longitude coordinate 1
     * @param lat2 Latitude coordinate 2
     * @param long2 Longitude coordinate 2
     */

    public double harvesine(double lat1, double long1, double lat2, double long2){
        double latDistance, longDistance, a, c, distance;
        final int R = 6371;
        latDistance = lat1 - lat2;
        longDistance = long1 - long2;
        latDistance = latDistance * Math.PI /180;
        longDistance = longDistance * Math.PI / 180;
        a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2 ) + Math.cos(lat2 * Math.PI / 180) * Math.cos(1 * Math.PI / 180) * Math.sin(longDistance / 2) * Math.sin(longDistance / 2);
        c = 2* Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        distance = R * c;
        return distance;
    }

    /**
     * Receives a GetDistanceRequest and returns the server response with the distance between 2 locations.
     *
     * @param request GetDistanceRequest (contains 4 coordinates)
     */

    public synchronized void getDistance(GetDistanceRequest request, StreamObserver<GetDistanceResponse> responseObserver){
        double distance = harvesine(request.getLat1(), request.getLong1(), request.getLat2(), request.getLong2());
        GetDistanceResponse response = GetDistanceResponse.newBuilder().setDistance(distance).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public synchronized void sysStatus(SysStatusRequest request, StreamObserver<SysStatusResponse> responseObserver){
        List<String> servers = request.getServersList();
        StringBuilder status = new StringBuilder(request.getStatus());
        for (String uri : servers){
            try {
                if (record.ping(pt.tecnico.rec.grpc.PingRequest.newBuilder().setUri(uri).build()).getOutput().equals("Pong.")) {
                    status.append("Rec Server: ").append(uri).append(" OK.\n");
                }
            } catch(Exception e) {
                status.append("Rec Server: ").append(uri).append(" DOWN.\n");
            }
        }
        SysStatusResponse response = SysStatusResponse.newBuilder().setStatus(status.toString()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
