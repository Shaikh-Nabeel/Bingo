package com.nabeel130.bingo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.nabeel130.bingo.DbController.DbHandler;

import java.io.File;
import java.util.ArrayList;

public class FavoriteSongs extends AppCompatActivity {
    private ArrayList<File> finalList;
    private static String[] items;
    public static CustomAdapter customAdapter;
    public static int clickedOnIndex= -1;
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.fav_songs);
        ListView listView = findViewById(R.id.listViewOfFavSong);

        //intent activity
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        ArrayList<File> mySong =(ArrayList) bundle.getParcelableArrayList("mySongs");

        ArrayList<String> favSong = MainActivity.favSongList;

        if(!favSong.isEmpty() &&  !mySong.isEmpty()) {

            //adding songs to list
            finalList = new ArrayList<>();
            for(int i =0; i<favSong.size(); i++){
                for(int j =0; j<mySong.size(); j++){
                    if(Integer.toString(mySong.get(j).hashCode()).equals(favSong.get(i))){
                        finalList.add(mySong.get(j));
                        break;
                    }
                }
            }

            //extracting name of the song
            refreshList();
            customAdapter = new CustomAdapter();
            listView.setAdapter(customAdapter);
        }

    }

    public void refreshList(){
        items = new String[finalList.size()];
        for(int i=0; i<finalList.size(); i++){
            items[i] = finalList.get(i).getName().replace(".mp3","");
        }
    }

    public void openPlaySongActivity(int position){
        Intent intent = new Intent(FavoriteSongs.this,PlaySong.class);
        intent.putExtra("songList",finalList);
        intent.putExtra("position",position);
        intent.putExtra("className", getString(R.string.favorite_song));
        startActivity(intent);
    }

    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
             @SuppressLint({"ViewHolder", "InflateParams"}) View myView = getLayoutInflater().inflate(R.layout.list_item, null);
            TextView textSong = myView.findViewById(R.id.txtView1);
            textSong.setSelected(true);
            textSong.setText(items[i]);
            textSong.setOnClickListener(v -> openPlaySongActivity(i));
            ToggleButton toggleButton = myView.findViewById(R.id.imgSong);
            if(clickedOnIndex == i) {
                textSong.setSelected(true);
                textSong.setTextColor(Color.MAGENTA);
            }
            toggleButton.setChecked(true);

            toggleButton.setOnClickListener(v -> {
                if (!toggleButton.isChecked()) {
                    DbHandler db = new DbHandler(FavoriteSongs.this);
                    if(db.removeSong(finalList.get(i).hashCode())){
                        MainActivity.favSongList.remove(i);
                        finalList.remove(i);
                        refreshList();
                        Log.d("dbQuery", "1 song removed");
                    }
                }
                this.notifyDataSetChanged();
                MainActivity.ca.notifyDataSetChanged();
            });
            return myView;
        }
    }
}
