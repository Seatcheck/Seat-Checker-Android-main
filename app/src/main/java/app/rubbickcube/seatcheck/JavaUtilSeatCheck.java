package app.rubbickcube.seatcheck;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import app.rubbickcube.seatcheck.model.Chats;

public class JavaUtilSeatCheck {

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    public static List<Chats> doBubbleSortLogic(Chats[] chats) {
        List<Chats> tempList = new ArrayList<>();
        Chats[] chatsArr = chats;
        Chats temp = null;
        for (int i = 0; i < chatsArr.length; i++) {
            for (int j = i + 1; j < chatsArr.length; j++) {
                if (chatsArr[i].getId() > chatsArr[j].getId()) {
                    temp = chatsArr[i];
                    chatsArr[i] = chatsArr[j];
                    chatsArr[j] = temp;
                }
            }
        }
        for (int i = 0; i < chatsArr.length; i++) {
            tempList.add(chatsArr[i]);
        }
        return tempList;
    }

    public static void showAlertDialogWithFinishActivity(final Context c, String title, String message) {
        new AlertDialog.Builder(c).setTitle(title).setMessage(message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                Activity a = (Activity) c;
                a.finish();

            }
        }).show();
    }
}
