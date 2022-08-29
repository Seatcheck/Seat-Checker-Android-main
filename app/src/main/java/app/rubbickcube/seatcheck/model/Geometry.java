package app.rubbickcube.seatcheck.model;


import com.google.gson.annotations.SerializedName;

/**
 * Created by hp on 2/21/2017.
 */

public class Geometry {


    @SerializedName("location")
    private Location location;//mazeed nested json ye he


    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
