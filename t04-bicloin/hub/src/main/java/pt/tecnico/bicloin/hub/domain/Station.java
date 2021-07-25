package pt.tecnico.bicloin.hub.domain;

import com.google.api.SystemParameterRule;

public class Station {

    private String name;
    private String abrev;
    private String coordsLat;
    private String coordsLong;
    private int docas;
    private int prize;
    private double distance;

    public Station(String name, String abrev, String coordsLat, String coordsLong, int docas, int prize){
        this.name = name;
        this.abrev = abrev;
        this.coordsLat = coordsLat;
        this.coordsLong = coordsLong;
        this.docas = docas;
        this.prize = prize;
        this.distance = 0.0;
    }

    public int getPrize(){
        return this.prize;
    }

    public int getDocas(){
        return this.docas;
    }

    public String getAbrev() { return this.abrev; }

    public String getName() { return this.name; }

    public String getCoordsLat() { return this.coordsLat; }

    public String getCoordsLong() { return  this.coordsLong; }

    public double getDistance() { return this.distance; }

    public void setDistance(double distance) { this.distance = distance; }

}
