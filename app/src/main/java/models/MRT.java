package models;

import java.util.List;

public class MRT {
    
    private final String name;
    private final Station station;
    private final int direction;
    private int distance;
    private List<Cabin> cabins;

    public MRT(String name, Station station, int direction) {
        this.name = name;
        this.station = station;
        this.direction = direction;
    }
    
    public String getName() {
        return name;
    }

    public Station getStation() {
        return station;
    }

    public int getDirection() {
        return direction;
    }

    public int getDistance() {
        return distance;
    }

    public List<Cabin> getCabins() {
        return cabins;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setCabins(List<Cabin> cabins) {
        this.cabins = cabins;
    }
}