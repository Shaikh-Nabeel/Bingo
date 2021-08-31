package com.nabeel130.bingo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class PlaySong extends AppCompatActivity {

    private TextView textView;
    private ImageView play;
    private static ArrayList<File> songs;
    static MediaPlayer mediaPlayer;
    private static int position;
    SeekBar seekBar;
    Thread updateSeekBar;
    private TextView currentTime;
    private TextView totalTime;
    public static String className;
    private ImageView imageViewOfSong;

    public void setList(ArrayList<File> list,int position){
        if(className == null || className.equals("FavoriteSong"))
            return;
        songs = list;
        PlaySong.position = position;
    }

    public void setList(){
        if(className == null)
                return;
        if(className.equals("FavoriteSong"))
            songs = FavoriteSongs.finalList;
        else
            songs = MainActivity.list;
    }

//    @SuppressLint("UseCompatLoadingForDrawables")
//    @Override
//    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        if(mediaPlayer != null && seekBar != null && textView != null && currentTime != null && totalTime != null){
//            outState.putInt("seekBarPos", seekBar.getProgress());
//            int i;
//            if(play.getBackground() == getDrawable(R.drawable.play))
//                i=1;
//            else
//                i=0;
//            outState.putInt("playBtnState", i);
//            outState.putString("songName", textView.getText().toString());
//            outState.putString("currentTime", currentTime.getText().toString());
//            outState.putString("totalTime", totalTime.getText().toString());
//
//        }
//        super.onSaveInstanceState(outState);
//    }
//
//    @Override
//    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        seekBar.setProgress(savedInstanceState.getInt("seekBarPos"));
//        textView.setText(savedInstanceState.getString("songName"));
//        currentTime.setText(savedInstanceState.getString("currentTime"));
//        totalTime.setText(savedInstanceState.getString("totalTime"));
//        int i = savedInstanceState.getInt("playBtnState");
//        if(i==1)
//            play.setImageResource(R.drawable.play);
//        else
//            play.setImageResource(R.drawable.pause);
//    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //UI variables
        textView = findViewById(R.id.textView);
        seekBar = findViewById(R.id.seekBar);
        play = findViewById(R.id.play);
        ImageView previous = findViewById(R.id.previous);
        ImageView next = findViewById(R.id.next);
        currentTime = findViewById(R.id.currentDuration);
        totalTime = findViewById(R.id.totalDuration);
        imageViewOfSong = findViewById(R.id.imageViewOfCurrSong);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        int currPos = bundle.getInt("position");
        className = bundle.getString("className");

        if(className.equals(getString(R.string.favorite_song))){
            songs = FavoriteSongs.finalList;
        }else if(className.equals(getString(R.string.main_activity))){
            songs = MainActivity.list;
        }else{
            songs = new ArrayList<>();
        }
        position = currPos;

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
            public void onStartTrackingTouch(SeekBar seekBar) { }

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

    private void updateCustomAdapter(int position){
        if(className.equals(getString(R.string.main_activity))) {
            MainActivity.clickedOnIndex = position;
            MainActivity.ca.notifyDataSetChanged();
        }
        else if(className.equals(getString(R.string.favorite_song))){
            FavoriteSongs.clickedOnIndex = position;
            FavoriteSongs.customAdapter.notifyDataSetChanged();
        }
    }

    private void setAlbumImage(){
        byte[] art;
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(songs.get(position).toString());
        try{
            art = metadataRetriever.getEmbeddedPicture();
            Bitmap bitmap = BitmapFactory.decodeByteArray(art,0,art.length);
            imageViewOfSong.setImageBitmap(bitmap);
        }catch (Exception e){
            imageViewOfSong.setImageResource(R.drawable.default_image);
        }
    }

//    private void setStaticFieldsOfSong(String... a){
//        textView.setText(songs.get(position).toString().replace(".mp3",""));
//        totalTime.setText(null);
//    }

    private void playSong(int position){
        updateCustomAdapter(position);
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        }catch (Exception ignored){

        }
        textView.setText(songs.get(position).getName().replace(".mp3",""));
        Uri uri2 = Uri.parse(songs.get(position).toString());
        mediaPlayer = MediaPlayer.create(PlaySong.this,uri2);
        int duration;
        try {
            mediaPlayer.start();
            duration = mediaPlayer.getDuration();
            if(duration<=0){
                throw new Exception();
            }
        }catch (Exception e){
            Toast.makeText(PlaySong.this, "Format is not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setAlbumImage();
        try {
            if (updateSeekBar.isAlive())
                updateSeekBar.interrupt();
        } catch (Exception ignored){

        }
        manageSeekBar();
        totalTime.setText(createTime(duration));
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

    public String createTime(int duration){
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