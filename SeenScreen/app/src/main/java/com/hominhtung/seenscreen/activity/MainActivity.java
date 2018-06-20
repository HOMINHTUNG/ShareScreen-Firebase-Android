package com.hominhtung.seenscreen.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hominhtung.seenscreen.R;

import static com.hominhtung.seenscreen.activity.ShowDriverActivity.FB_TOKEN_DRIVER;
import static com.hominhtung.seenscreen.activity.ShowDriverActivity.dialogCall;
import static com.hominhtung.seenscreen.activity.ShowDriverActivity.sendMessage;

public class MainActivity extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference().child("bitmap_base64");
    private String mBitmapBase64 = null;
    private String TAG = "MainActivity";
    private PowerManager.WakeLock mWakeLock;




    private Button button;
    private ImageView imageView;

    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PowerManager mPowerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "Power Manager");

        button = (Button)findViewById(R.id.button);
        imageView = (ImageView)findViewById(R.id.imageView);

        dataChangeServer();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage(FB_TOKEN_DRIVER,"ADMIN_OFF");
            }
        });

        BroadcastReceiver receiverOFF = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                dialogCall("Driver đã ngừng cuộc gọi!",MainActivity.this);
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiverOFF, new IntentFilter("DRIVER_OFF"));

    }

    // LOCK LIGHT
    @Override
    protected void onResume(){
        super.onResume();
        mWakeLock.acquire();
    }

    //Load image record in database firebase
    private void dataChangeServer(){
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                try{
                    mBitmapBase64 = dataSnapshot.getValue(String.class);
                    imageView.setImageBitmap(base64ToBitmap(mBitmapBase64));

                }catch (Exception e){

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Khong doc duoc du lieu.", error.toException());
            }
        });
    }

    private Bitmap base64ToBitmap (String encodedImage) {
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }

}
