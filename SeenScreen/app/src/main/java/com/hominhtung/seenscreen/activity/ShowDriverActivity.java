package com.hominhtung.seenscreen.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hominhtung.seenscreen.R;
import com.hominhtung.seenscreen.adapter.driverAdapter;
import com.hominhtung.seenscreen.object.driverItem;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by HOMINHTUNG-PC on 3/29/2018.
 */

public class ShowDriverActivity extends AppCompatActivity {

    public static FirebaseFirestore cloud_db = FirebaseFirestore.getInstance();
    private static String TAG = "ShowDriverActivity";
    public static String FB_API = "https://fcm.googleapis.com/fcm/send";
    public static String FB_KEY_API ="key=AIzaSyDBLpnbl8fndPh8cKc-nflsq48MnTFGmIA";
    public static String FB_CONTENT_TYPE = "application/json";
    public static String FB_TOKEN_ADMIN = "fu3xRDLobuQ:APA91bGVVUXCfl7ySs6opzYRjVw9sw-i0hXBWxI6rJ_8JRKNawV5ck0yoqvJtAuddyO7fOpqmaj0wycaQziySYzXPSDjpSfSspYzRdseXf-_DCfgKSszhBYUpciYGAAQNDTCQmezjXdDAQ52aKhchk11oo5iUmbTNA";
    public static String FB_TOKEN_DRIVER = "dhxulYINeH4:APA91bHkxKTzWv3Kmr2TCrFZ58GrIWOKrAPv8BBTGoKHRsWANG5R1jQHLiRSkfIeqYIalTHd4Mn-W8obvNYjGie6xqKQtq-JQNGZC5gym-wp4iGeq5sPtBm8d0ol4BEa3sHor3KX7tbBdzEvbpvRegMUj3_rZioOjw";

    private ArrayList<driverItem> listDriver = new ArrayList<>();
    private driverAdapter mDriverAdapter = null;
    private ListView listView;
    private static Dialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_driver);

        listView = (ListView) findViewById(R.id.lv_driver);

        mDriverAdapter = new driverAdapter(ShowDriverActivity.this, listDriver);

        //list info driver show
        getListItems();
        listView.setAdapter(mDriverAdapter);


        BroadcastReceiver receiverNO = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //dialog call when admin request
                dialogCall("Driver không chấp nhận cuộc gọi!",ShowDriverActivity.this);
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiverNO, new IntentFilter("NO"));

        BroadcastReceiver receiverYES = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //dialog call when admin request
                intent = new Intent(ShowDriverActivity.this, MainActivity.class);
                startActivity(intent);
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiverYES, new IntentFilter("YES"));

    }


    private void getListItems() {
        cloud_db.collection("Driver")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try{
                                    listDriver.add(new driverItem(document.getString("token").toString(),document.getString("device").toString()));
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                mDriverAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Log.w(TAG, "Không lấy được dữ liệu", task.getException());
                        }
                    }
                });
    }


    // show when driver refuse call and send mess NO
    public static void dialogCall(String noiDung, Activity activity){
        try{
            dialog = new Dialog(activity); // Context, this, etc.
            dialog.setContentView(R.layout.dialog_layout);

            TextView text = (TextView) dialog.findViewById(R.id.txt_dia);
            text.setText(noiDung);

            Button btn_yes = dialog.findViewById(R.id.btn_yes);
            btn_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            Button btn_no = dialog.findViewById(R.id.btn_no);
            btn_no.setVisibility(View.GONE);

            dialog.show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    //  Send mess
    public static void sendMessage(final String token, final String body) {
        final okhttp3.MediaType JSON = okhttp3.MediaType.parse("application/json; charset=utf-8");
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                JSONObject json = new JSONObject();
                JSONObject jsonData = new JSONObject();
                try {
                    jsonData.put("body", body);
                    jsonData.put("title", FB_TOKEN_ADMIN);
                    json.put("notification", jsonData);
                    json.put("to", "/singledevice/" + token);

                    okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, json.toString());
                    okhttp3.Request request = new okhttp3.Request.Builder()
                            .header("Content-Type", FB_CONTENT_TYPE)
                            .header("Authorization", FB_KEY_API)
                            .url(FB_API)
                            .post(body)
                            .build();

                    okhttp3.Response response = client.newCall(request).execute();
                    String finalResponse = response.body().string();
                    Log.w(TAG, "response: " + finalResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

}
