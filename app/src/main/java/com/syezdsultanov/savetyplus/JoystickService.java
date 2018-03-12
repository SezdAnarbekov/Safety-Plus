package com.syezdsultanov.savetyplus;

import android.Manifest;
import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static android.view.WindowManager.LayoutParams;

public class JoystickService extends Service {

    private MediaRecorder myRecorder;
    private LocationManager mLocationManager;
    private WindowManager mWindowManager;
    private LayoutParams params;
    private LinearLayout mLinearLayout;
    private ImageView mJoystickImageView;
    private Chronometer mChronometer;
    private long lastPressTime, pressTime;
    private String sms, phoneNumber, outputFile;
    private volatile boolean flag = true;
    private int smsCount;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mJoystickImageView = new ImageView(this);
        mJoystickImageView.setImageResource(R.drawable.sos_image_start);
        mChronometer = new Chronometer(this);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.setTextSize(15.0f);
        mChronometer.setTextColor(Color.RED);
        mChronometer.setVisibility(View.GONE);
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = LayoutParams.TYPE_PHONE;
        }
        params = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        mLinearLayout = new LinearLayout(this);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParamsview = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mLinearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mChronometer.setLayoutParams(layoutParamsview);
        mJoystickImageView.setLayoutParams(layoutParamsview);
        mLinearLayout.addView(mJoystickImageView);
        mLinearLayout.addView(mChronometer);
        params.x = 0;
        params.y = 100;
        mWindowManager.addView(mLinearLayout, params);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, final int startId) {
        SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        phoneNumber = pref.getString("number", "");
        sms = pref.getString("text", "");
        if (sms.equals("")) {
            sms = "Safety Plus\n";
        }


        mJoystickImageView.setOnTouchListener(new View.OnTouchListener() {
            private final LayoutParams paramsF = params;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private Thread t;
            private boolean isCronometerStarted;


            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        pressTime = System.currentTimeMillis();
                        if (pressTime - lastPressTime <= 300) {
                            if (myRecorder != null) {
                                myRecorder.stop();
                                myRecorder.reset();
                                myRecorder.release();
                                myRecorder = null;
                                saveToDownloads();
                            }
                            if (isCronometerStarted) {
                                mChronometer.stop();
                                mChronometer = null;
                            }
                            JoystickService.this.stopSelf();
                            if (t != null) {
                                smsCount = 0;
                                flag = false;
                            }
                        }
                        lastPressTime = pressTime;
                        initialX = paramsF.x;
                        initialY = paramsF.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:

                        if ((System.currentTimeMillis() - pressTime) > 1000) {
                            mJoystickImageView.setImageResource(R.drawable.sos_image);
                            mChronometer.setVisibility(View.VISIBLE);
                            mChronometer.start();
                            isCronometerStarted = true;
                            if (t == null) {
                                t = new TheThread(startId);
                                t.start();
                            }
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                        paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mLinearLayout, paramsF);
                        break;
                }

                return true;
            }
        });
        return START_STICKY;
    }

    private void saveToDownloads() {
        File dir = new File(outputFile);
        DownloadManager downloadManager = (DownloadManager)
                JoystickService.this.getSystemService(DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.addCompletedDownload(dir.getName(), dir.getName(), true,
                    "video/3gpp", dir.getAbsolutePath(), dir.length(), true);
        }
    }

    private void sendMessageWithLocation() {
        smsCount++;
        StringBuilder smsBody = new StringBuilder(sms);
        if (smsCount > 1) {
            smsBody = new StringBuilder("Safety Plus\t");
        }

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED && ActivityCompat.
                    checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                smsBody.append("\nI'm here:\t");
                smsBody.append("http://maps.google.com?q=");
                smsBody.append(latitude);
                smsBody.append(",");
                smsBody.append(longitude);
                sendMessage(smsBody);
            } else {
                sendMessage(smsBody);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendMessage(StringBuilder smsBody) {
        try {
            String numbers[] = phoneNumber.split(",");
            for (String number : numbers) {
                SmsManager sms = SmsManager.getDefault();
                if (smsBody.length() < 160) {
                    sms.sendTextMessage(number.trim(), null,
                            smsBody.toString(), null, null);
                } else {
                    ArrayList<String> parts = sms.divideMessage(smsBody.toString());
                    sms.sendMultipartTextMessage(number.trim(), null, parts,
                            null, null);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Message sent to invalid destination.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mJoystickImageView != null) {
            //      mWindowManager.removeView(mJoystickImageView);
            mWindowManager.removeView(mLinearLayout);
            mJoystickImageView = null;
        }
        flag = false;
        if (myRecorder != null) {
            myRecorder.release();
            myRecorder = null;
        }

    }

    final class TheThread extends Thread {
        private final int serviceId;

        TheThread(int serviceId) {
            this.serviceId = serviceId;
        }

        @Override
        public void run() {
            if (ActivityCompat.checkSelfPermission(JoystickService.this,
                    Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED && ActivityCompat.
                    checkSelfPermission(JoystickService.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            }
            while (flag) {
                try {
                    if (ActivityCompat.checkSelfPermission(JoystickService.this,
                            Manifest.permission.SEND_SMS)
                            == PackageManager.PERMISSION_GRANTED) {
                        sendMessageWithLocation();
                        Thread.sleep(10000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            stopSelf(this.serviceId);
        }

        private void startRecording() {
            Date date = new Date();
            outputFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .getAbsolutePath() + "/" + date.toString() + "sp_audiorecords.3gp";
            myRecorder = new MediaRecorder();
            myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            myRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            myRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            myRecorder.setOutputFile(outputFile);
            try {
                myRecorder.prepare();
                myRecorder.start();
            } catch (IllegalStateException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}

