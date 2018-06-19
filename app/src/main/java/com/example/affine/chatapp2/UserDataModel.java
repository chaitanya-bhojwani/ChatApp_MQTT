package com.example.affine.chatapp2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserDataModel {
    @SerializedName("mesgType")
    @Expose
    private String mesgType;
    @SerializedName("UserId")
    @Expose
    private int UserId;
    @SerializedName("UserName")
    @Expose
    private String UserName;
    @SerializedName("UserStatus")
    @Expose
    private String UserStatus;
    @SerializedName("dpLink")
    @Expose
    private String dpLink;

    public String getMesgType() {
        return mesgType;
    }

    public void setMesgType(String mesgType) {
        this.mesgType = mesgType;
    }

    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        this.UserId = userId;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        this.UserName = userName;
    }

    public String getUserStatus() {
        return UserStatus;
    }

    public void setUserStatus(String userStatus) {
        this.UserStatus = userStatus;
    }

    public String getDpLink() {
        return dpLink;
    }

    public void setDpLink(String dpLink) {
        this.dpLink = dpLink;
    }
}
