package app.invision.morse.api

import app.rubbickcube.seatcheck.model.ChatUser
import app.rubbickcube.seatcheck.model.ChatUserList
import app.rubbickcube.seatcheck.model.Chats
import app.rubbickcube.seatcheck.model.SucessResponse
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

interface SOService {
//
//    @Multipart
//    @POST
//    fun sendchatMessage(@Url url: String?,
//                        @Header("Authorization") token: String?,
//                        @Part("message") message: RequestBody) : Call<MessageSendResponse>
//
//    @POST
//    @FormUrlEncoded
//    fun sendUserStatus(@Url url: String?,
//                       @Field("appUserId") apUserId: Int,
//                       @Field("isOnline") appUserId : Boolean,
//                       @Field("socketId") socketId: String) : Call<UserStatusResponse>
//
//
//    @GET
//    fun getUserOnline(@Url url: String?) : Call<FetchUserStatusResponse>


    @POST
    @Multipart
    fun getUserChatList(@Url url: String?,
                        @Part("sender_id") senderId: RequestBody): Call<Array<ChatUserList>>

    @POST
    @Multipart
    fun userVisibility(@Url url: String?,
                       @Part("userid") userid: RequestBody,
                       @Part("visibility") visibility: RequestBody
    ): Call<SucessResponse>

    @POST
    @Multipart
    fun setSeatCheckChatUser(@Url url: String?,
                             @Part("name") name: RequestBody,
                             @Part("email") email: RequestBody,
                             @Part("seatcheck_user_id") seatcheck_user_id: RequestBody,
                             @Part("profile_pic") profile_pic: RequestBody,
                             @Part("status") status: RequestBody,
                             @Part("device_id") device_id: RequestBody,
                             @Part("lat") lat: RequestBody,
                             @Part("lng") lng: RequestBody,
                             @Part("timezone") timezone: RequestBody
    ): Call<SucessResponse>


    @POST
    @Multipart
    fun sendchatMessage(@Url url: String?,
                        @Part("sender_id") sender_id: RequestBody,
                        @Part("receiver_id") receiver_id: RequestBody,
                        @Part("message") message: RequestBody,
                        @Part("iosTime") iosTime: RequestBody
    ): Call<SucessResponse>


    @POST
    @Multipart
    fun getChats(@Url url: String?,
                 @Part("sender_id") sender_id: RequestBody,
                 @Part("receiver_id") receiver_id: RequestBody): Call<Array<Chats>>

    @POST
    @Multipart
    fun getPeopleAroundMe(@Url url: String?,
                          @Part("lat") lat: RequestBody,
                          @Part("lng") lng: RequestBody,
                          @Part("radius") radius: RequestBody
    ): Call<MutableList<ChatUser>>

    @POST
    @Multipart
    fun deleteChat(@Url url: String?,
                   @Part("sender_id") sender_id: RequestBody,
                   @Part("receiver_id") receiver_id: RequestBody): Call<SucessResponse>


//    @GET
//    fun getChats(@Url url: String?,
//                 @Header("Authorization") token: String?) : Call<ChatsResponse>
//
//
//    @Multipart
//    @POST
//    fun fireReadMsgCall(@Url url: String?,
//                        @Header("Authorization") token: String,
//                        @Part("msgId") message_id: RequestBody): Call<MessageSendResponse>


}