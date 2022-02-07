package models;

public class Cabin {

    private final int cabinID;
    private final int peopleCount;

    public Cabin(int cabinID, int peopleCount) {
        this.cabinID = cabinID;
        this.peopleCount = peopleCount;
    }

    public int getCabinID() {
        return cabinID;
    }

    public int getPeopleCount() {
        return peopleCount;
    }
}