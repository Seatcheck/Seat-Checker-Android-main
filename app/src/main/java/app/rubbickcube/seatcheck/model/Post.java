package app.rubbickcube.seatcheck.model;


import com.backendless.BackendlessUser;

import java.util.Date;

/*  contact["Users"] = Backendless.UserService.CurrentUser()
        contact["Quantity"] = seat!!
        contact["time"] = selectedHour?.times(3600)!!
        contact["extendedTime"] = getCuurentDateTime()
        contact["createdTwo"] = addTime(selectedHour!!,selectedMins)
        contact["resturantName"] = Prefs.getString("name","")
        contact["resturantAddress"] = Prefs.getString("vicinity","")
        contact["resturantPhone"] = Prefs.getString("phone","")
        contact["resturantLocationLatitude"] = Prefs.getString("lat","")
        contact["resturantLocationLongitude"] = Prefs.getString("lng","")
        contact["shouldGoLive"] = postOnMap!!

*/
public class Post {

    private String objectId;
    private int time;
    private int Quantity;
    private String resturantName;
    private String resturantAddress;
    private Date createdTwo;
    private Date extendedTime;
    private String resturantPhone;
    private String resturantLocationLatitude;
    private String resturantLocationLongitude;
    private String shouldGoLive;
    private Boolean showOnMap;
    private Boolean inRange;
    private BackendlessUser User;
    private Invites invite;
    private Date created;
    private Records record;
    private String interestOption;


    public String getInterestOption() {
        return interestOption;
    }

    public void setInterestOption(String interestOption) {
        this.interestOption = interestOption;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getQuantity() {
        return Quantity;
    }

    public void setQuantity(int quantity) {
        Quantity = quantity;
    }

    public String getResturantName() {
        return resturantName;
    }

    public void setResturantName(String resturantName) {
        this.resturantName = resturantName;
    }

    public String getResturantAddress() {
        return resturantAddress;
    }

    public void setResturantAddress(String resturantAddress) {
        this.resturantAddress = resturantAddress;
    }

    public Date getCreatedTwo() {
        return createdTwo;
    }

    public void setCreatedTwo(Date createdTwo) {
        this.createdTwo = createdTwo;
    }

    public Date getExtendedTime() {
        return extendedTime;
    }

    public void setExtendedTime(Date extendedTime) {
        this.extendedTime = extendedTime;
    }

    public String getResturantPhone() {
        return resturantPhone;
    }

    public void setResturantPhone(String resturantPhone) {
        this.resturantPhone = resturantPhone;
    }

    public String getResturantLocationLatitude() {
        return resturantLocationLatitude;
    }

    public void setResturantLocationLatitude(String resturantLocationLatitude) {
        this.resturantLocationLatitude = resturantLocationLatitude;
    }

    public String getResturantLocationLongitude() {
        return resturantLocationLongitude;
    }

    public void setResturantLocationLongitude(String resturantLocationLongitude) {
        this.resturantLocationLongitude = resturantLocationLongitude;
    }

    public String getShouldGoLive() {
        return shouldGoLive;
    }

    public void setShouldGoLive(String shouldGoLive) {
        this.shouldGoLive = shouldGoLive;
    }

    public Boolean getShowOnMap() {
        return showOnMap;
    }

    public void setShowOnMap(Boolean showOnMap) {
        this.showOnMap = showOnMap;
    }

    public Boolean getInRange() {
        return inRange;
    }

    public void setInRange(Boolean inRange) {
        this.inRange = inRange;
    }

    public BackendlessUser getUser() {
        return User;
    }

    public void setUser(BackendlessUser user) {
        User = user;
    }

    public Invites getInvite() {
        return invite;
    }

    public void setInvite(Invites invite) {
        this.invite = invite;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Records getRecord() {
        return record;
    }

    public void setRecord(Records record) {
        this.record = record;
    }
}
