package app.rubbickcube.seatcheck.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserContacts {

@SerializedName("success")
@Expose
private Boolean success;
@SerializedName("contactList")
@Expose
private List<ContactList> contactList = null;

public Boolean getSuccess() {
return success;
}

public void setSuccess(Boolean success) {
this.success = success;
}

public List<ContactList> getContactList() {
return contactList;
}

public void setContactList(List<ContactList> contactList) {
this.contactList = contactList;
}

}