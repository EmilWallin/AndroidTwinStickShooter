package com.example.pixelshooter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;

public class MainMenuActivity extends AppCompatActivity {

    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        playMusic(findViewById(R.id.switchMusic));
    }

    //start game button click
    public void startGame(View view) {
        Intent intent = new Intent(this, PixelShooterMain.class);
        startActivity(intent);
    }

    //instructions button click
    public void instructionsScreen(View view) {
        Intent intent = new Intent(this, InstructionsActivity.class);
        startActivity(intent);
    }

    //Play music toggle, called when the checkbox is clicked. MediaPlayer for continous playing (no seamless loops though)
    public void playMusic(View view){
        Switch switchMusic = (Switch)findViewById(R.id.switchMusic);
        if(switchMusic.isChecked()) {
            if(player == null) {
                player = MediaPlayer.create(this, R.raw.pixelshootermusicloop);
                player.setLooping(true);
            }
            player.start();
        } else{
            if(player != null){
                player.pause();
            }
        }
    }

    //Turn off music when app is closed (otherwise it runs until the thread closes)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.stop();
    }
}