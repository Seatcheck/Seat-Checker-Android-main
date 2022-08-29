package app.rubbickcube.seatcheck;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.UnsupportedEncodingException;


/**
 * Created by hp on 10/25/2016.
 */

public class LifeController {

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler asyncHttpResponseHandler) {
        client.addHeader("Authorization","bearer "+ Prefs.getString("token",""));
      Log.d("token",""+ Prefs.getString("token",""));
        client.get(getAbsoluteUrl(url), params, asyncHttpResponseHandler);
    }
    public static void getWithToken(String url, RequestParams params, AsyncHttpResponseHandler asyncHttpResponseHandler) {
        client.get(getAbsoluteUrl(url), params, asyncHttpResponseHandler);

    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler asyncHttpResponseHandler) {
        client.addHeader("Authorization","bearer "+ Prefs.getString("token",""));
        Log.d("token",""+ Prefs.getString("token",""));
        client.post(getAbsoluteUrl(url), params, asyncHttpResponseHandler);
    }

    public static void patch(String url, RequestParams params, AsyncHttpResponseHandler asyncHttpResponseHandler) {
        client.addHeader("Authorization","bearer "+ Prefs.getString("token",""));

        client.patch(getAbsoluteUrl(url), params, asyncHttpResponseHandler);
    }


    public static void postFile(String url, RequestParams params, AsyncHttpResponseHandler asyncHttpResponseHandler) {
        client.post(getAbsoluteUrl(url), params, asyncHttpResponseHandler);

    }


    private static String getAbsoluteUrl(String relativeUrl) {
        return  relativeUrl;
    }

    public static String getResponse(byte[] responseBody, Context context) {



        String str;
        String jresponse = null;
        try {

            
            if(responseBody == null) {

   // Utils.LifeAlertDialog(context,"","Ops! Something went wrong.");

                return "null";
            }

            str = new String(responseBody,"UTF-8");
            jresponse = str.replaceAll("\\\\", "");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        catch (NullPointerException ex) {

           // Toast.makeText(context, "Internet connection is required", Toast.LENGTH_SHORT).show();
        }

        return jresponse;

    }

    public static String getResponseWithoutReplace(byte[] responseBody, Context context) {




        String str;
        String jresponse = null;

        if(responseBody == null) {

            // Utils.LifeAlertDialog(context,"","Ops! Something went wrong.");

            return "null";
        }
        try {
            str = new String(responseBody,"UTF-8");
            jresponse = str.toString();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return jresponse;

    }
}
