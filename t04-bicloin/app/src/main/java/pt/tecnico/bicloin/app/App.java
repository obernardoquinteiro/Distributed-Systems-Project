package pt.tecnico.bicloin.app;

import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.HubFrontend;
import pt.tecnico.bicloin.hub.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class App {

    HubFrontend frontend;

    public App(HubFrontend frontend) {
        this.frontend = frontend;
    }

    // App methods that send requests to the HubServiceImpl and returns responses to the user
    // Before each server request the method "checkServerConnection" is run to connect to an available hub


    /**
     * Prints to the console the user balance.
     *
     * @param user User username
     */

    public void balance(String user){
        try {
            BalanceRequest request = BalanceRequest.newBuilder().setUsername(user).build();
            System.out.println(user + " " + frontend.getBalance(request).getBalance() + " BIC");
        } catch (StatusRuntimeException e) {
            System.out.println("ERRO " + e.getStatus().getDescription());
        }
    }

    /**
     * Prints to the console the user balance after the top-up.
     *
     * @param user User username
     * @param cellphone Cellphone associated to the user
     */

    public void topUp(String user, String cellphone, int cash){
        try {
            TopUpRequest request = TopUpRequest.newBuilder().setUsername(user).setCellphone(cellphone).setCash(cash).build();
            System.out.println(user + " " + frontend.addSaldo(request).getBalance() + " BIC");
        } catch (StatusRuntimeException e) {
            System.out.println("ERRO " + e.getStatus().getDescription());
        }
    }

    /**
     * Prints to the console the information about a specific station.
     *
     * @param abrev Station abreviation.
     */

    public void infoStation(String abrev){
        try {
            InfoStationRequest request = InfoStationRequest.newBuilder().setAbrev(abrev).build();
            InfoStationResponse response = frontend.getStationInfo(request);
            System.out.println(response.getNome() + ", lat " + response.getLat() + ", " + response.getLong() + " long, " +
                    response.getDocas() + " docas, " + response.getPrize() + " BIC premio, " +
                    response.getBikes() + " bicicletas, " + response.getLevantamentos() + " levantamentos, " +
                    response.getDevolucoes() + " devolucoes, " + response.getUrl());
        } catch (StatusRuntimeException e) {
            System.out.println("ERRO " + e.getStatus().getDescription());
        }
    }


    /**
     * Prints to the console the k number of stations closest to the user.
     *
     * @param coordsLat User latitude coordinates
     * @param coordsLong User longitude coordinates
     * @param k Number of stations to be searched
     */

    public void locateStation(String coordsLat, String coordsLong, int k){
        try {
            LocateStationRequest request = LocateStationRequest.newBuilder().setCoordsLat(coordsLat).setCoordsLong(coordsLong).setK(k).build();
            LocateStationResponse response = frontend.locateStation(request);
            for (int i = 0; i < response.getAbrevList().size(); i++) {

                InfoStationRequest request2 = InfoStationRequest.newBuilder().setAbrev(response.getAbrevList().get(i)).build();
                InfoStationResponse response2 = frontend.getStationInfo(request2);

                GetDistanceRequest request3 = GetDistanceRequest.newBuilder().setLat1(Double.parseDouble(coordsLat))
                        .setLong1(Double.parseDouble(coordsLong)).setLat2(Double.parseDouble(response2.getLat()))
                        .setLong2(Double.parseDouble(response2.getLong())).build();
                GetDistanceResponse response3 = frontend.getDistance(request3);


                System.out.println(response.getAbrev(i) + ", lat " + response2.getLat() + ", " + response2.getLong() + " long, " +
                        response2.getDocas() + " docas, " + response2.getPrize() + " BIC premio, " +
                        response2.getBikes() + " bicicletas, a " + ((int) (response3.getDistance() * 1000)) + " metros.");
            }
        } catch (StatusRuntimeException e) {
            System.out.println("ERRO " + e.getStatus().getDescription());
        }
    }


    /**
     * Prints to the console OK if its possible to bike-up at the selected station.
     *
     * @param username User username
     * @param coordsLat User latitude coordinates
     * @param coordsLong User longitude coordinates
     * @param abrev Station abreviation.
     */

    public void bikeUp(String username, String coordsLat, String coordsLong, String abrev){
        try {
            BikeUpRequest request = BikeUpRequest.newBuilder().setUsername(username).setCoordsLat(coordsLat).setCoordsLong(coordsLong).setAbrev(abrev).build();
            System.out.println(frontend.bikeUp(request).getResponse());
        } catch (StatusRuntimeException e) {
            System.out.println("ERRO " + e.getStatus().getDescription());
        }
    }

    /**
     * Prints to the console OK if its possible to bike-down at the selected station.
     *
     * @param username User username
     * @param coordsLat User latitude coordinates
     * @param coordsLong User longitude coordinates
     * @param abrev Station abreviation.
     */

    public void bikeDown(String username, String coordsLat, String coordsLong, String abrev){
        try {
            BikeDownRequest request = BikeDownRequest.newBuilder().setUsername(username).setCoordsLat(coordsLat).setCoordsLong(coordsLong).setAbrev(abrev).build();
            System.out.println(frontend.bikeDown(request).getResponse());
        } catch (StatusRuntimeException e) {
            System.out.println("ERRO " + e.getStatus().getDescription());
        }
    }

    /**
     * Prints to the console the location of the user as a Google Maps link.
     *
     * @param username User username
     * @param coordsLat User latitude coordinates
     * @param coordsLong User longitude coordinates
     */

    public void at(String username, String coordsLat, String coordsLong){
        System.out.println(username + " em https://www.google.com/maps/place/" + coordsLat + "," + coordsLong);
    }


    /**
     * Prints to the console the state of the servers in Zookeeper.
     */

    public void sysStatus() throws ZKNamingException {
        SysStatusRequest request = SysStatusRequest.newBuilder().setStatus("").build();
        System.out.print(frontend.sysStatus(request).getStatus());
    }


    /**
     * Prints to the console the result of a PingRequest.
     */

    public void ping() {
        PingRequest request = PingRequest.newBuilder().build();
        System.out.println(frontend.ping(request).getOutput());
    }
}
