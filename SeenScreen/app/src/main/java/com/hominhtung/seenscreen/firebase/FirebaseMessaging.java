package com.hominhtung.seenscreen.firebase;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by HOMINHTUNG-PC on 3/29/2018.
 */

public class FirebaseMessaging extends FirebaseMessagingService {

    private final String TAG = "FirebaseMessaging";


    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String from = remoteMessage.getFrom();
        Log.d(TAG, "Tin nhắn đến từ SENDER_ID: " + from);

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Thông báo: " + remoteMessage.getNotification().getBody());

            try {
                String action = remoteMessage.getNotification().getBody().toString();
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(action));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
