package models;

public class Station implements EqualsOverride {

    /** This store the name of the station. */
    private String name;

    // TODO: Add new field called 'line' to store the station's line code.

    /**
     * Constructs a station object. Could include the line code of the station.
     * in the future.
     *
     * @param name The name of the station
     */
    public Station(String name) {
        this.name = name;
    }

    /** Retrieve the station name from the station object. */
    public String getStationName() {
        return name;
    }

    /** Sets the station name of the station object. */
    public void setStationName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        // Check if the objects compared are the same.
        if (this == o) {
            return true;
        }

        // Null check.
        if (o == null) {
            return false;
        }

        // Check if both objects in the comparison belong to the same class.
        if (getClass() != o.getClass()) {
            return false;
        }

        // Type cast the object.
        Station station = (Station) o;

        // Compare the name attribute of both objects and return a boolean value.
        return this.name.equals(station.name);
    }

    @Override
    public int hashCode() {
        // Return the hash code of the name field.
        return name.hashCode();
    }
}