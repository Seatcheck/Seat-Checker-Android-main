package app.rubbickcube.seatcheck.model;

import com.backendless.BackendlessUser;

import java.util.Date;

public class Invites {

    private String objectId;
    private String status;
    private String reason;
    private BackendlessUser sender;
    private BackendlessUser receiver;
    private String requestCreated;
    private String created;
    private Post post;

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BackendlessUser getSender() {
        return sender;
    }

    public void setSender(BackendlessUser sender) {
        this.sender = sender;
    }

    public BackendlessUser getReceiver() {
        return receiver;
    }

    public void setReceiver(BackendlessUser receiver) {
        this.receiver = receiver;
    }

    public String getRequestCreated() {
        return requestCreated;

    }

    public void setRequestCreated(String requestCreated) {
        this.requestCreated = requestCreated;
    }


    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }



    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }


    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }
}


