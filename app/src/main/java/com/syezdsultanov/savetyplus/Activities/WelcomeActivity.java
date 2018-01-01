package com.syezdsultanov.savetyplus.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.ImageView;

import com.syezdsultanov.savetyplus.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);
        final ImageView imageLogo = findViewById(R.id.logo);
        int welcomeTime = 2000;
        new Handler().postDelayed((new Runnable() {
            @Override
            public void run() {
                imageLogo.animate().rotation(imageLogo.getRotation() - 1000).start();
                Intent main = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(main);
                finish();
            }
        }), welcomeTime);
    }
}
