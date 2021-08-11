package com.nabeel130.bingo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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
    private String[] items;

//    @Override
//    public void onBackPressed(){
//        super.onBackPressed();
//    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.fav_songs);
        ListView listView = findViewById(R.id.listViewOfFavSong);

        //intent activity
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        ArrayList<File> mySong =(ArrayList) bundle.getParcelableArrayList("mySongs");

        //accessing table from db
//        DbHandler dbHandler = new DbHandler(getApplicationContext());
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
            items = new String[finalList.size()];
            for (int i = 0; i < finalList.size(); i++) {
                items[i] = finalList.get(i).getName().replace(".mp3","");
            }
            CustomAdapter customAdapter = new CustomAdapter();
            listView.setAdapter(customAdapter);
        }


    }

    public void openPlaySongActivity(int position){
        Intent intent = new Intent(FavoriteSongs.this,PlaySong.class);
        intent.putExtra("songList",finalList);
        intent.putExtra("position",position);
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

            toggleButton.setChecked(true);

            toggleButton.setOnClickListener(v -> {
                if (!toggleButton.isChecked()) {
                    DbHandler db = new DbHandler(FavoriteSongs.this);
                    if(db.removeSong(finalList.get(i).hashCode())){
//                        Log.d("dbQuery", MainActivity.favSongList.toString()+" index "+ i);
                        MainActivity.favSongList.remove(i);
                        finalList.remove(i);
                    }
                }
                finish();
                startActivity(getIntent());
            });
            return myView;
        }
    }
}
