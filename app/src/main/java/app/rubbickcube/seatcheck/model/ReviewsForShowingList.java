package app.rubbickcube.seatcheck.model;

import com.backendless.BackendlessUser;

public class ReviewsForShowingList {

    private BackendlessUser fromUser;
    private Double numberOfStars;
    private String messageTest;
    private String created;





    public BackendlessUser getFromUser() {
        return fromUser;
    }

    public void setFromUser(BackendlessUser fromUser) {
        this.fromUser = fromUser;
    }

    public Double getNumberOfStars() {
        return numberOfStars;
    }

    public void setNumberOfStars(Double numberOfStars) {
        this.numberOfStars = numberOfStars;
    }

    public String getMessageTest() {
        return messageTest;
    }

    public void setMessageTest(String messageTest) {
        this.messageTest = messageTest;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }
}


