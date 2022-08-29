package app.invision.morse.api

import app.rubbickcube.seatcheck.api.EndPointsConstants


/**
 * Created by hp on 10/14/2017.
 */
class ApiUtils {

    companion object  {
        val BASE_URL = EndPointsConstants.API_BASE_URL
        fun getSOService(): SOService {
            return  RetrofitClient.getClient(BASE_URL)!!.create(SOService::class.java)
        }
    }

}