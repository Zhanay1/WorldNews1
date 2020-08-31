package com.example.globalnews;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class HomeActivity extends AppCompatActivity {
    private static int SPLASH_OUT_TIME = 2000;
    SharedPref mySharedPref ;
    private static int currentTheme;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mySharedPref = new SharedPref(this);
        if(mySharedPref.loadNightModeState() == true){
            setTheme(R.style.DarkTheme);
            currentTheme = R.style.DarkTheme;
        }else{
            setTheme(R.style.AppTheme);
            currentTheme = R.style.AppTheme;
        }
        setContentView(R.layout.activity_home);
//        getSupportActionBar().hide();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_OUT_TIME);

    }
}
