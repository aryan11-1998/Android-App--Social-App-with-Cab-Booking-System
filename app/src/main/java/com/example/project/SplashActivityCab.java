package com.example.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.github.ybq.android.spinkit.style.RotatingCircle;
import com.github.ybq.android.spinkit.style.WanderingCubes;


public class SplashActivityCab extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_cab);

        ProgressBar progressBar = (ProgressBar)findViewById(R.id.cabloader);
        Sprite doubleBounce = new WanderingCubes();
        progressBar.setIndeterminateDrawable(doubleBounce);




        Thread thread = new Thread(){
            public void run(){
                try {
                    sleep(2500);
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    Intent welcome = new Intent(SplashActivityCab.this,WelcomeCabActivity.class);
                    startActivity(welcome);
                }
            }
        };
        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}