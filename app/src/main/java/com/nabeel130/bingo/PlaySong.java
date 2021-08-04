package com.nabeel130.bingo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class PlaySong extends AppCompatActivity {
    TextView textView;
    ImageView play,previous,next;
    ArrayList<File> songs;
    static MediaPlayer mediaPlayer;
    int position;
    SeekBar seekBar;
    Thread updateSeekBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);
        textView = findViewById(R.id.textView);
        seekBar = findViewById(R.id.seekBar);
        play = findViewById(R.id.play);
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        songs =(ArrayList) bundle.getParcelableArrayList("songList");
//      textContent = bundle.getString("currSong");
        position = bundle.getInt("position");
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        playSong(position);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        next.setOnClickListener(v -> {playNextSong(); play.setImageResource(R.drawable.pause);});
        previous.setOnClickListener(v -> {playPreviousSong(); play.setImageResource(R.drawable.pause);});

        play.setOnClickListener(v -> {
            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                play.setImageResource(R.drawable.play);
            }
            else{
                mediaPlayer.start();
                play.setImageResource(R.drawable.pause);
            }
        });

    }

    private void manageSeekBar(){

        updateSeekBar = new Thread(){
            @Override
            public void run(){
                int currentPosition = 0;
                try{
                    while(currentPosition < mediaPlayer.getDuration()){
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                        sleep(500);
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        };
        updateSeekBar.start();
    }

    private void playSong(int position){
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        }catch (Exception ignored){

        }
        textView.setText(songs.get(position).getName());
        Uri uri2 = Uri.parse(songs.get(position).toString());
        mediaPlayer = MediaPlayer.create(PlaySong.this,uri2);
        mediaPlayer.start();
        try {
            if (updateSeekBar.isAlive())
                updateSeekBar.interrupt();
        }
        catch (Exception ignored){

        }
        manageSeekBar();
        seekBar.setMax(mediaPlayer.getDuration());
        mediaPlayer.setOnCompletionListener(mp -> playNextSong());
    }

    private void playNextSong(){
        if(position < songs.size()-1)
            position++;
        else
            position = 0;
        playSong(position);
    }

    private void playPreviousSong(){
        if(position == 0)
            position = songs.size()-1;
        else
            position--;
        playSong(position);
    }
}