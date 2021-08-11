package com.nabeel130.bingo;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.nabeel130.bingo.DbController.DbHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private ListView listViewOfSong;
    private static String[] items;
    private static ArrayList<File> mySongs;
    public static ArrayList<String> favSongList;
//    private static final boolean isSortedByName = false;
    private CustomAdapter ca;

    @Override
    protected void onStart() {
        super.onStart();
        ca.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listViewOfSong = findViewById(R.id.listViewOfSongs);
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        //fetching all the songs
                        mySongs = fetchSong(Environment.getExternalStorageDirectory());

                        //fetching favourite songs
                        DbHandler dbHandler = new DbHandler(getApplicationContext());
                        favSongList = (ArrayList<String>) dbHandler.getAllSongs();

                        //array of songs name
                        items = new String[mySongs.size()];
                        defaultSort();

                        ca = new CustomAdapter();
                        listViewOfSong.setAdapter(ca);

                        //sorting functions
                        Button sortingBtn = findViewById(R.id.sorting);
                        sortingBtn.setOnClickListener(v -> {
                            if(sortingBtn.getText().equals(getString(R.string.SortByName))) {
                                sortByName();
                                sortingBtn.setText(R.string.defaultSort);
                                ca.notifyDataSetChanged();
                            }
                            else{
                                defaultSort();
                                ca.notifyDataSetChanged();
                                sortingBtn.setText(R.string.SortByName);
                            }
                        });

                        if(sortingBtn.getText().equals(getString(R.string.defaultSort)))
                            sortingBtn.performClick();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();

        //Favorite Song Icon
        Button favSong = findViewById(R.id.favSongIcon);
        favSong.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this,FavoriteSongs.class)
                    .putExtra("mySongs", mySongs);
            startActivity(intent);
        });
    }

    //redirecting to playSong Activity
    public void openPlaySongActivity(int position){
        Intent intent = new Intent(MainActivity.this,PlaySong.class);
        intent.putExtra("songList",mySongs);
        intent.putExtra("position",position);
        startActivity(intent);
    }

    //function to refresh the activity
    public void refreshActivity(){
        finish();
        startActivity(getIntent());
    }

    public void sortByName(){
        Arrays.sort(items);
    }

    public void defaultSort(){
        for(int i=0; i<mySongs.size(); i++){
            items[i] = mySongs.get(i).getName().replace(".mp3","");
        }
    }

    //fetching song from external storage
    public ArrayList<File> fetchSong(File file){
        ArrayList<File> list = new ArrayList<>();
        File[] songs = file.listFiles();
        if(songs != null){
            for(File myFile: songs){
                if(myFile.isDirectory() && !myFile.isHidden() && !myFile.getName().equals("call_rec"))
                    list.addAll(fetchSong(myFile));
                else{
                    if(myFile.getName().endsWith(".mp3") && !myFile.getName().startsWith("."))
                        list.add(myFile);
                }
            }
        }
        return list;
    }


    class CustomAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
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

            if(favSongList.contains(Integer.toString(mySongs.get(i).hashCode()))){
                toggleButton.setChecked(true);
            }
            toggleButton.setOnClickListener(v -> {
                if(toggleButton.isChecked()){
                    DbHandler db = new DbHandler(MainActivity.this);
                    int hCode = mySongs.get(i).hashCode();
                    db.addSongs(hCode);
                    favSongList.add(Integer.toString(hCode));
                }
                else{
                    DbHandler db = new DbHandler(MainActivity.this);
                    if(db.removeSong(mySongs.get(i).hashCode())){
                        favSongList.remove(Integer.toString(mySongs.get(i).hashCode()));
                        Log.d("dbQuery", "1 song removed");
                    }

                }
                Log.d("dbQuery", "f: "+favSongList.toString());
            });
            return myView;
        }
    }

}