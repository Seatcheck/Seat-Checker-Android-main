package app.rubbickcube.seatcheck.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by hp on 2/21/2017.
 */

public class Vicinity {

    @SerializedName("geometry")
    private Geometry geometry; // ye he..

    @SerializedName("types")
    private List<String> types;

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    @SerializedName("place_id")
    private String place_id;

    @SerializedName("vicinity")
    private String vicinity;

    @SerializedName("name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    /*private VicinityTypes types;*/



}
