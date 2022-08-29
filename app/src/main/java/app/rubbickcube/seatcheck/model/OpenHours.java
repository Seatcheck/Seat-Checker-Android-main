package app.rubbickcube.seatcheck.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by hp on 2/25/2017.
 */

public class OpenHours {

    @SerializedName("open_now")
    private boolean isOpen;

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }
}
