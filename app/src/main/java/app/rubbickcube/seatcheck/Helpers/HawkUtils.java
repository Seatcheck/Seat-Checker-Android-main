package app.rubbickcube.seatcheck.Helpers;

import com.orhanobut.hawk.Hawk;

public class HawkUtils {



    public static void putHawk(String key,Object T) {

        Hawk.put(key,T);
    }


    public static Object getHawk(String key) {


        if(Hawk.get(key) == null) {

            return  null;
        }else {
            Object obj = Hawk.get(key);
            return  obj;
        }
    }

    public static void deleteHawk(String key) {

        Hawk.delete(key);
    }

    public static void deleteHawk() {

        Hawk.deleteAll();
    }
}
