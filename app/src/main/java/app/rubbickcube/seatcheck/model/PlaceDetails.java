package app.rubbickcube.seatcheck.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by hp on 2/22/2017.
 */


public class PlaceDetails {



    private String formatted_address;
    private String international_phone_number;
    private String name;
   // private int rating;
    private String vicinity;
    private String website;

    @SerializedName("geometry")
    private Geometry geometry;

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    @SerializedName("opening_hours")
    private OpenHours openHours;


    public OpenHours getOpenHours() {
        return openHours;
    }

    public void setOpenHours(OpenHours openHours) {
        this.openHours = openHours;
    }

    public String getFormatted_address() {
        return formatted_address;
    }

    public void setFormatted_address(String formatted_address) {
        this.formatted_address = formatted_address;
    }

    public String getInternational_phone_number() {
        return international_phone_number;
    }

    public void setInternational_phone_number(String international_phone_number) {
        this.international_phone_number = international_phone_number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

   /* public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
*/
    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
