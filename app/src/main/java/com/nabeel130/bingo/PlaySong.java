package com.nabeel130.bingo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
    private TextView currentTime;
    private TextView totalTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);
        textView = findViewById(R.id.textView);
        seekBar = findViewById(R.id.seekBar);
        play = findViewById(R.id.play);
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        currentTime = findViewById(R.id.currentDuration);
        totalTime = findViewById(R.id.totalDuration);
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
                currentTime.setText(createTime(seekBar.getProgress()));
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

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message message){
            currentTime.setText(createTime(message.what));
        }
    };

    private void manageSeekBar(){

        updateSeekBar = new Thread(){
            @Override
            public void run(){
                int currentPosition = 0;
                try{
                    while(currentPosition < mediaPlayer.getDuration()){
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                        Message message = new Message();
                        message.what = currentPosition;
                        handler.sendMessage(message);
                        sleep(1000);
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
        currentTime.setText("00:00");
        totalTime.setText("00:00");
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
        totalTime.setText(createTime(mediaPlayer.getDuration()));
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
    public String createTime(int duration)
    {
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time+=min+":";
        if(sec<10)
        {
            time+="0";
        }
        time+=sec;

        return time;
    }
}

