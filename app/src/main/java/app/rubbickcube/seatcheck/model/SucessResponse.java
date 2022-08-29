package app.rubbickcube.seatcheck.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SucessResponse {

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("success")
    @Expose
    private Boolean success;
     @SerializedName("chat_user_id")
    @Expose
    private int chat_user_id;
     @SerializedName("visibility")
    @Expose
    private int visibility;



    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public int getChat_user_id() {
        return chat_user_id;
    }

    public void setChat_user_id(int chat_user_id) {
        this.chat_user_id = chat_user_id;
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }
}
