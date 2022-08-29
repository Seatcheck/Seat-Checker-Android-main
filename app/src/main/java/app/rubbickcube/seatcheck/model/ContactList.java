package app.rubbickcube.seatcheck.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ContactList {

@SerializedName("_id")
@Expose
private String id;
@SerializedName("user")
@Expose
private String user;
@SerializedName("contact")
@Expose
private Users contact;
@SerializedName("createdAt")
@Expose
private String createdAt;
@SerializedName("updatedAt")
@Expose
private String updatedAt;
@SerializedName("__v")
@Expose
private Integer v;

public String getId() {
return id;
}

public void setId(String id) {
this.id = id;
}

public String getUser() {
return user;
}

public void setUser(String user) {
this.user = user;
}

public Users getContact() {
return contact;
}

public void setContact(Users contact) {
this.contact = contact;
}

public String getCreatedAt() {
return createdAt;
}

public void setCreatedAt(String createdAt) {
this.createdAt = createdAt;
}

public String getUpdatedAt() {
return updatedAt;
}

public void setUpdatedAt(String updatedAt) {
this.updatedAt = updatedAt;
}

public Integer getV() {
return v;
}

public void setV(Integer v) {
this.v = v;
}

}