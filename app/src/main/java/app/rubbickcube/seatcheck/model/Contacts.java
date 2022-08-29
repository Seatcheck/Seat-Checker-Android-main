package app.rubbickcube.seatcheck.model;

import com.backendless.BackendlessUser;

import java.util.ArrayList;

public class Contacts {

    private BackendlessUser user;
    private ArrayList<BackendlessUser> contacts;
    private String ownerId;

    public BackendlessUser getUser() {
        return user;
    }

    public void setUser(BackendlessUser user) {
        this.user = user;
    }

    public ArrayList<BackendlessUser> getContacts() {
        return contacts;
    }

    public void setContacts(ArrayList<BackendlessUser> contacts) {
        this.contacts = contacts;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
