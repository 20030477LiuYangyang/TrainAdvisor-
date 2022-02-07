package models;

public class Line {

    private String lineCode;
    private int lineStatusNumber;

    public Line(String lineCode, int lineStatusNumber) {
        this.lineCode = lineCode;
        this.lineStatusNumber = lineStatusNumber;
    }

    public String getLineCode() {
        return lineCode;
    }

    public int getLineStatusNumber() {
        return lineStatusNumber;
    }
}
