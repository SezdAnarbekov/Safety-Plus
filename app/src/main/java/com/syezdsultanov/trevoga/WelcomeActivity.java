package com.syezdsultanov.trevoga;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.ImageView;

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
                Intent home = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(home);
                finish();
            }
        }), welcomeTime);
    }
}
