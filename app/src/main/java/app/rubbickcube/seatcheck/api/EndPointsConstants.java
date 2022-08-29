package app.rubbickcube.seatcheck.api;

import com.pixplicity.easyprefs.library.Prefs;

/**
 * Created by hp on 10/23/2016.
 */

public final class EndPointsConstants {


    // public static final String BASE_URL  = "http://18.223.115.65/api/";
//  public static final String BASE_URL
    public static final String BASE_URL = "http://18.224.27.39";
    public static final String SOCKET_BASE_URL = BASE_URL+":3000";
    public static final String API_BASE_URL = BASE_URL + "/api/";

    // // public static final String BASE_URL  = "http://ec2-35-161-255-204.us-west-2.compute.amazonaws.com/api/";
//  public static final String REGISTER = BASE_URL + "auth/register";
//  public static final String VERIFICATION = BASE_URL + "auth/verify";
//  public static final String PROFILE_UPDATE = BASE_URL + "me/update";
//  public static final String REFRESH_TOKEN = BASE_URL + "auth/refresh";
//  public static final String PROFILE_VIEW = BASE_URL + "getUser";
//  public static final String PROFILE_PHOTO = BASE_URL + "upload";
//  public static final String LOGOUT = BASE_URL + "user/logout";
//  public static final String CREATE_POST = BASE_URL + "user/createPost";
//  public static final String GET_ALL_POSTS = BASE_URL + "user/Posts";
//  public static final String CREATE_PIN = BASE_URL + "user/pin";
//  public static final String ALL_PINS = BASE_URL + "Pins/all";
//  public static final String SEND_MESSAGE = BASE_URL + "send/push";
//  public static final String VIEW_MESSAGE = BASE_URL + "user/messages";
//  public static final String DELET_MESSAGE = BASE_URL + "delete";
//  public static final String DELET_MARKER = BASE_URL + "delete/pin";
//  public static final String DELETE_ACCOUNT = BASE_URL + "user/delete";
//  public static final String SINGLE_USER = BASE_URL + "get/pin";
//  public static final String SWITCH_NOTIFICATION = BASE_URL + "switch/notification";
//  public static final String VALIDATE_DEVICE = BASE_URL + "user/validate";
//
//  //RequestsController EndPoints
//  public static final String VIEW_REQUEST = BASE_URL + "user/requests";
//  public static final String SEND_REQUEST = BASE_URL + "send/pushForRequest";
//  public static final String DELET_REQUEST = BASE_URL + "request/delete";
//
//  //Chat Module EndPonits
    public static final String SEND_CHAT_MESSAGE = API_BASE_URL + "message/send";
    public static final String CHAT_LIST = "chat/list";
    public static final String SET_USER = "user/set";
    public static final String CHAT_VIEW = API_BASE_URL + "message/view";
    public static final String GET_PEOPLE = API_BASE_URL + "users/all";
    public static final String VISIBILITY = API_BASE_URL + "user/visibility";
    public static final String UPDATE_USER_STATUS = Prefs.getString("socketUrl", "") + "message/view/";


}
