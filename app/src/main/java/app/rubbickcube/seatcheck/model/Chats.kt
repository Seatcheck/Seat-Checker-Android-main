package app.rubbickcube.seatcheck.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by YASIR on 10/28/2017.
 */

class Chats {

    @SerializedName("id")
    @Expose
    private var id: Int? = null

    @SerializedName("sender")
    @Expose
    private var sender: Int? = null

    @SerializedName("receiver")
    @Expose
    private var receiver: Int? = null

    @SerializedName("type")
    @Expose
    private var type: Int? = null

    @SerializedName("message")
    @Expose
    private var message: String? = null

    @SerializedName("is_read")
    @Expose
    private var isRead: Int? = null

    @SerializedName("created_at")
    @Expose
    private var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    private var updatedAt: String? = null

    @SerializedName("user")
    @Expose
    private var user: ChatUser? = null

    @SerializedName("time")
    @Expose
    private var time: String? = null

    @SerializedName("iosTime")
    @Expose
    private var iosTime: String? = null

    fun setIosTime(time: String) {
        this.iosTime = time
    }

    fun getIosTime(): String? {
        return iosTime
    }

    private var seatcheckUserId: String? = null


    fun getSeatcheckUserId(): String? {
        return seatcheckUserId
    }

    fun setSeatcheckUserId(seatcheckUserId: String?) {
        this.seatcheckUserId = seatcheckUserId
    }

    fun getId(): Int? {
        return id
    }

    fun setId(id: Int?) {
        this.id = id
    }

    fun getSender(): Int? {
        return sender
    }

    fun setSender(sender: Int?) {
        this.sender = sender
    }

    fun getReceiver(): Int? {
        return receiver
    }

    fun setReceiver(receiver: Int?) {
        this.receiver = receiver
    }

    fun getType(): Int? {
        return type
    }

    fun setType(type: Int?) {
        this.type = type
    }

    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String) {
        this.message = message
    }

    fun getIsRead(): Int? {
        return isRead
    }

    fun setIsRead(isRead: Int?) {
        this.isRead = isRead
    }

    fun getCreatedAt(): String? {
        return createdAt
    }

    fun setCreatedAt(createdAt: String) {
        this.createdAt = createdAt
    }

    fun getUpdatedAt(): String? {
        return updatedAt
    }

    fun setUpdatedAt(updatedAt: String) {
        this.updatedAt = updatedAt
    }

    fun getUser(): ChatUser? {
        return user
    }

    fun setUser(user: ChatUser) {
        this.user = user
    }

    fun getTime(): String? {
        return time
    }

    fun setTime(time: String) {
        this.time = time
    }


}

