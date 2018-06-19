package com.example.affine.chatapp2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ActivityChangeUtil {
    public static void change(Context context, Class aClass) {
        Intent starter = new Intent(context, aClass);
        context.startActivity(starter);
//        ((Activity) context).overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
    }

    public static void changeWithFinish(Context context, Class aClass) {
        Intent starter = new Intent(context, aClass);
        context.startActivity(starter);
//        ((Activity) context).overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
        ((Activity) context).finish();
    }

    public static void change(Context context, Class aClass, String data) {
        Intent starter = new Intent(context, aClass);
        starter.putExtra("data", data);
        context.startActivity(starter);
//        ((Activity) context).overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
    }

    public static void change(Context context, Class aClass, Long data) {
        Intent starter = new Intent(context, aClass);
        starter.putExtra("data", data);
        context.startActivity(starter);
//        ((Activity) context).overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
    }

    public static void change(Context context, Class aClass, Bundle data) {
        Intent starter = new Intent(context, aClass);
        starter.putExtra("data", data);
        context.startActivity(starter);
//        ((Activity) context).overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
    }

    public static void changeActivityForResult(Context context, Class aClass, Bundle data) {
        Intent starter = new Intent(context, aClass);
        starter.putExtra("data", data);
        ((Activity) context).startActivityForResult(starter, 0);
    }

    public static void finish(Activity activity) {
        activity.finish();
//        activity.overridePendingTransition(R.anim.activity_left_to_right, R.anim.activity_right_to_left);
    }

    public static void finishWithAffinity(Activity activity) {
        activity.finishAffinity();
//        activity.overridePendingTransition(R.anim.activity_left_to_right, R.anim.activity_right_to_left);
    }

}
