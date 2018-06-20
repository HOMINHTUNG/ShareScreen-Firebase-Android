package com.hominhtung.sharescreen;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import static com.hominhtung.sharescreen.FirebaseMessaging.TOKEN_ADMIN;

public class MainActivity extends Activity {

    //  Database firebase
    static FirebaseDatabase db = FirebaseDatabase.getInstance();
    static DatabaseReference myRef = db.getReference("bitmap_base64");

    //  Notification FirebaseStore
    private static String FB_API = "https://fcm.googleapis.com/fcm/send";
    public static String FB_KEY_API ="key=AIzaSyDBLpnbl8fndPh8cKc-nflsq48MnTFGmIA";
    private static String FB_CONTENT_TYPE = "application/json";
    private static String TOKEN_DRIVER = "dhxulYINeH4:APA91bHkxKTzWv3Kmr2TCrFZ58GrIWOKrAPv8BBTGoKHRsWANG5R1jQHLiRSkfIeqYIalTHd4Mn-W8obvNYjGie6xqKQtq-JQNGZC5gym-wp4iGeq5sPtBm8d0ol4BEa3sHor3KX7tbBdzEvbpvRegMUj3_rZioOjw";

    //  Record Screen
    private static final String TAG = MainActivity.class.getName();
    private static final int REQUEST_CODE = 100;
    private static final String SCREENCAP_NAME = "screencap";
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private static MediaProjection sMediaProjection;
    private MediaProjectionManager mProjectionManager;
    private ImageReader mImageReader;
    private Handler mHandler;
    private Display mDisplay;
    private VirtualDisplay mVirtualDisplay;
    private int mDensity;
    private int mWidth = 640;
    private int mHeight = 480;
    private int mRotation;
    private final int runTime = 300;
    public static Dialog dialog;

    //  Paletet main
    private Bitmap bitmap;
    private Image image;
    private String encoder;

    //  Floatbutton
    private FloatingActionButton btnFab;
    private TextView txtTimer;
    CountDownTimer mCoutDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//      call for the projection manager
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

//      BroadcastReceiver listener event CALL
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

//              dialog call when admin request
                dialogCallON("CUỘC GỌI THÂN THƯƠNG", "Gửi yêu cầu share screen!");
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("CALL"));

//      BroadcastReceiver listener event ADMIN_OFF
        BroadcastReceiver receiverAdminOff = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

//              Turn off share screen when admin off call
                stopProjection();

                btnFab.hide();
                mCoutDownTimer.cancel();
                txtTimer.setText("");

            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiverAdminOff, new IntentFilter("ADMIN_OFF"));

//      Timer record text
        txtTimer = findViewById(R.id.txt_time);
        txtTimer.setText("");

//      FloatButton
        btnFab = findViewById(R.id.floatAction);
        btnFab.hide();

//      Movement when touch buton Fab
        btnFab.setOnTouchListener(new View.OnTouchListener() {
            int[] temp = new int[]{0, 0};

            public boolean onTouch(View v, MotionEvent event) {
                btnFab.show();
                int eventaction = event.getAction();

                int x = (int) event.getRawX();
                int y = (int) event.getRawY();

                switch (eventaction) {

                    case MotionEvent.ACTION_DOWN: // touch down so check if the
                        temp[0] = (int) event.getX();
                        temp[1] = y - v.getTop();
                        break;

                    case MotionEvent.ACTION_MOVE: // touch drag with the ball
                        v.layout(x - temp[0], y - temp[1], x + v.getWidth()
                                - temp[0], y - temp[1] + v.getHeight());

                        // v.postInvalidate();
                        break;

                    case MotionEvent.ACTION_UP:
                        break;
                }

                return false;
            }

        });

//      Show dialog YES/NO turn off share screen
        btnFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogCallOFF("Thông báo", "Bạn có chắc chắn muốn dừng chia sẻ màn hình!");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopFireBase();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    sMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);

                    // display metrics
                    DisplayMetrics metrics = getResources().getDisplayMetrics();
                    mDensity = metrics.densityDpi;
                    mDisplay = getWindowManager().getDefaultDisplay();

                    // create virtual display depending on device width / height
                    createVirtualDisplay();

                    // register media projection stop callback
                    sMediaProjection.registerCallback(new MediaProjection.Callback() {
                        @Override
                        public void onStop() {
                            Log.e("ScreenCapture", "stopping projection.");

                            if (mVirtualDisplay != null) mVirtualDisplay.release();
                            if (mImageReader != null)
                                mImageReader.setOnImageAvailableListener(null, null);

                            sMediaProjection.unregisterCallback(this);
                        }
                    }, mHandler);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //  UI Widget Callbacks
    private void startProjection() {
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    private void stopProjection() {

//      sMediaProjection != null
        try {
            sMediaProjection.stop();
            stopFireBase();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //  Factoring Virtual Display creation
    private void createVirtualDisplay() {

//      get width and height
        Point size = new Point();
        mDisplay.getSize(size);

//      start capture reader
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        mVirtualDisplay = sMediaProjection.createVirtualDisplay(SCREENCAP_NAME, mWidth, mHeight, mDensity, VIRTUAL_DISPLAY_FLAGS, mImageReader.getSurface(), null, mHandler);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {

                image = null;
                bitmap = null;

                try {
                    image = reader.acquireNextImage();
                    if (image != null) {
                        Image.Plane[] planes = image.getPlanes();
                        ByteBuffer buffer = planes[0].getBuffer();


//                       create bitmap
                        bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                        bitmap.copyPixelsFromBuffer(buffer);

                        sendBitmapBase64(bitmap);

//                        IMAGES_PRODUCED++;
//                        Log.e(TAG, "captured image: " + IMAGES_PRODUCED);

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (bitmap != null) {
                        //                 Handler handler = new Handler();
                        //                handler.postDelayed(new Runnable() {
//
                        //                         @Override
                        //                         public void run() {
                        try {
                            Thread.sleep(runTime);
                            bitmap.recycle();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //                         }
                        //                       }, 1000 );
                    }
                    if (image != null) {
                        try {
                            image.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        image.close();
                    }
                }
            }
        }, mHandler);
    }

    private void sendBitmapBase64(Bitmap bitmap) {

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            encoder = Base64.encodeToString(byteArray, Base64.DEFAULT);
            Log.d(TAG, "bitmap: " + encoder);
        } catch (Exception e) {
            e.printStackTrace();
        }

//      Load data
        uploadFireBase();

    }

    //  check load date lên firebase
    public void uploadFireBase() {
        try {
            myRef.setValue(encoder, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    Log.d(TAG, "upload ERROR: " + databaseError);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopFireBase() {
        try {
            myRef.setValue("", new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    Log.d(TAG, "upload ERROR: " + databaseError);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dialogCallON(String mTitle, String mMessage) {

        try {
            dialog = new Dialog(this); // Context, this, etc.
            dialog.setContentView(R.layout.dialog_layout);
            TextView title = (TextView) dialog.findViewById(R.id.txt_title);
            title.setText(mTitle);

            TextView message = (TextView) dialog.findViewById(R.id.txt_dia);
            message.setText(mMessage);

            Button btn_yes = dialog.findViewById(R.id.btn_yes);
            btn_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startProjection();

//                  Animation FloatButton, set default 10000s
                    animFloat(10000000);

//                  send mess YES/NO apply call
                    sendMessage(TOKEN_ADMIN, "YES");

                    dialog.dismiss();

                }
            });
            Button btn_no = dialog.findViewById(R.id.btn_no);
            btn_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage(TOKEN_ADMIN, "NO");
                    dialog.dismiss();
                }
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dialogCallOFF(String mTitle, String mMessage) {

        try {
            dialog = new Dialog(this); // Context, this, etc.
            dialog.setContentView(R.layout.dialog_layout);

            TextView message = (TextView) dialog.findViewById(R.id.txt_dia);
            message.setText(mMessage);

            TextView title = (TextView) dialog.findViewById(R.id.txt_title);
            title.setText(mTitle);

            final Button btn_yes = dialog.findViewById(R.id.btn_yes);
            btn_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    stopProjection();
                    sendMessage(TOKEN_ADMIN, "DRIVER_OFF");

                    btnFab.hide();
                    mCoutDownTimer.cancel();
                    txtTimer.setText("");
                    dialog.dismiss();

                }
            });
            Button btn_no = dialog.findViewById(R.id.btn_no);
            btn_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //  Animation button Fab and countdowntimer print text txtTimer
    private void animFloat(int time) {

        btnFab.show();
        final Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_animation);
        final Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out_animation);

        mCoutDownTimer = new CountDownTimer(time, 1000) {
            long seconds = 0;

            public void onTick(long millisUntilFinished) {
                seconds++;

                txtTimer.setText(String.format("%02d", seconds / 60) + ":" + String.format("%02d", seconds % 60));
                if (millisUntilFinished % 2 == 0) {
                    btnFab.startAnimation(animFadeIn);
                } else {
                    btnFab.startAnimation(animFadeOut);
                }
            }

            public void onFinish() {
                txtTimer.setText("");
            }

        }.start();
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
                    jsonData.put("title", TOKEN_DRIVER);
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
