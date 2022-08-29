package app.rubbickcube.seatcheck.model;

public class TempPostDetails {

    /* seatcheck_name.text = intent.getStringExtra("seatcheck_name")
        seatcheck_location_name.text = intent.getStringExtra("seatcheck_location_name")
        seatcheck_location_desc.text = intent.getStringExtra("seatcheck_location_desc")
        createdTwo = intent.getStringExtra("createdTwo")
        post_id = intent.getStringExtra("seatcheck_post_id")
*/

    private String name;
    private String locationName;
    private String locationDesc;
    private String createdTwo;
    private String postId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationDesc() {
        return locationDesc;
    }

    public void setLocationDesc(String locationDesc) {
        this.locationDesc = locationDesc;
    }

    public String getCreatedTwo() {
        return createdTwo;
    }

    public void setCreatedTwo(String createdTwo) {
        this.createdTwo = createdTwo;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}

