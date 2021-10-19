package com.example.pixelshooter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

//Projekt av Emil Wallin. Skapat 16-04-2021

public class PixelShooterMain extends AppCompatActivity {

    private PixelShooterGame mPixelShooterGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        Log.d("Debugging:","Displaymetrics: " + displayMetrics.widthPixels + "" +  displayMetrics.heightPixels);

        mPixelShooterGame = new PixelShooterGame(this, displayMetrics.widthPixels, displayMetrics.heightPixels);
        setContentView(mPixelShooterGame);
    }

    //Resume and Pause method calls
    @Override
    protected void onResume() {
        super.onResume();
        mPixelShooterGame.resume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mPixelShooterGame.pause();
    }

}