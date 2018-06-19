package com.example.affine.chatapp2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataModel {
    @SerializedName("msgId")
    @Expose
    private long msgId;
    @SerializedName("mesgType")
    @Expose
    private String mesgType;
    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("userId")
    @Expose
    private int userId;

    @SerializedName("deliveredStatus")
    @Expose
    private String deliveredStatus;

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public String getMesgType() {
        return mesgType;
    }

    public void setMesgType(String mesgType) {
        this.mesgType = mesgType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDeliveredStatus() {
        return deliveredStatus;
    }

    public void setDeliveredStatus(String deliveredStatus) {
        this.deliveredStatus = deliveredStatus;
    }
}

