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
public class Records {


    private String resturantName;
    private String resturantAddress;
    private int seats;
    private String interestOption;
    private String meetingStatus;
    private int totalRequests ;
    private String resturantPhone;
    private BackendlessUser User;


    public void setInterestOption(String interestOption) {
        this.interestOption = interestOption;
    }

    public String getInterestOption() {
        return interestOption;
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



    public String getMeetingStatus() {
        return meetingStatus;
    }

    public void setMeetingStatus(String meetingStatus) {
        this.meetingStatus = meetingStatus;
    }


    public String getResturantPhone() {
        return resturantPhone;
    }

    public void setResturantPhone(String resturantPhone) {
        this.resturantPhone = resturantPhone;
    }

    public BackendlessUser getUser() {
        return User;
    }

    public void setUser(BackendlessUser user) {
        User = user;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(int totalRequests) {
        this.totalRequests = totalRequests;
    }
}
