package com.hominhtung.seenscreen.firebase;

import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by HOMINHTUNG-PC on 3/29/2018.
 */

public class FirebaseToken extends FirebaseInstanceIdService {

    private final String TAG = "FirebaseToken";
    private static String idDriver = "admin";

    public FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();

    //this method will be called
    //when the token is generated
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token and info device.
        String DeviceName = getDeviceName() + "," + getAndroidVersion() + "," + getCPUInfo() + "," + getScreenWidth() + "x" + getScreenWidth();
        String token = FirebaseInstanceId.getInstance().getToken();

        Log.d(TAG, "Refreshed token: " + token);
        Log.d(TAG, "device: " + DeviceName);

        updateToken(token,DeviceName);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

    }

    private void updateToken(String token, String DeviceName){

        DocumentReference driver = mFirebaseFirestore.collection("Driver").document(idDriver);
        driver.update("token", token);
        driver.update("device", DeviceName)
                .addOnSuccessListener(new OnSuccessListener< Void >() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Updated Successfully");
                    }
                });
    }



    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private static String getCPUInfo() {
        StringBuffer sb = new StringBuffer();
        sb = sb.append("").append(Build.CPU_ABI);
        return String.valueOf(sb);
    }

    public static String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "Android SDK: " + sdkVersion + "(" + release + ")";
    }
}


