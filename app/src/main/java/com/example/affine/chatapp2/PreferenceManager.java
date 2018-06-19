package com.example.affine.chatapp2;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

/**
 * Created by jiva1 on 18/3/18.
 */

public class PreferenceManager {
    private final String PREF_NAME = "parkquility-preference";
    private final String UserId = "userId";
    private final String UserName = "userName";
    private final String Status = "Hey there ! I am using PQ.";
    private final String dp = "no Picture";
    private Context context;
    private SharedPreferences preferences;

    PreferenceManager(Context context) {
        this.context = context;
        this.initPreference();
    }

    private void initPreference() {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setName(String val) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(UserName, val);
        editor.commit();
    }

    public String getName() {
        return preferences.getString(UserName, "");
    }

    public void setId(int isDetailsFilled) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(UserId,isDetailsFilled);
        editor.commit();
    }

    public int getId() {
        return preferences.getInt(UserId, -1);
    }

    public void clear() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear().commit();
    }

    public void setStatus(String val) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Status, val);
        editor.commit();
    }

    public String getStatus() {
        return preferences.getString(Status, "Hey there ! I am using PQ. Yay yay yay yay yay yay yay yay yay yay yay yay yay");
    }

    public void setDp (String val) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(dp, val);
        editor.commit();
    }

    public String getDp() {
        return preferences.getString(dp, "no Picture");
    }

}
