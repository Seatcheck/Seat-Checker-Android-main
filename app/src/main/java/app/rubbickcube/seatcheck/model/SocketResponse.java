package app.rubbickcube.seatcheck.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SocketResponse {

@SerializedName("message")
@Expose
private MessageObject message;
@SerializedName("user")
@Expose
private UserObject user;
@SerializedName("time")
@Expose
private String time;

public MessageObject getMessage() {
return message;
}

public void setMessage(MessageObject message) {
this.message = message;
}

public UserObject getUser() {
return user;
}

public void setUser(UserObject user) {
this.user = user;
}

public String getTime() {
return time;
}

public void setTime(String time) {
this.time = time;
}

}


