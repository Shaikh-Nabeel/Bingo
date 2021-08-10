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
import android.widget.ImageView;
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

public class MainActivity extends AppCompatActivity {
    private ListView listViewOfSong;
    private String[] items;
    private  ArrayList<File> mySongs;
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
                        mySongs = fetchSong(Environment.getExternalStorageDirectory());
//                        for(int i =0; i<mySongs.size(); i++){
//                            Log.d("hashcode", mySongs.get(i).hashCode()+"");
//                        }
                        items = new String[mySongs.size()];
                        for(int i=0; i<mySongs.size(); i++){
                            items[i] = mySongs.get(i).getName().replace(".mp3","");
                        }
                        CustomAdapter ca = new CustomAdapter();
                        listViewOfSong.setAdapter(ca);

                        listViewOfSong.setOnItemClickListener((parent, view, position, id) -> {
                            Intent intent = new Intent(MainActivity.this,PlaySong.class);
                            String currSong = (String) listViewOfSong.getItemAtPosition(position);
                            intent.putExtra("songList",mySongs);
                            intent.putExtra("currSong",currSong);
                            intent.putExtra("position",position);
                            startActivity(intent);
                        });

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
        ImageView favSong = findViewById(R.id.favSongIcon);
        favSong.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this,FavoriteSongs.class)
                    .putExtra("mySongs", mySongs);
            startActivity(intent);
        });
    }

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

            ToggleButton toggleButton = myView.findViewById(R.id.imgSong);
            toggleButton.setOnClickListener(v -> {
                if(toggleButton.isChecked()){
                    Log.d("hashcode","if block");
                    DbHandler db = new DbHandler(MainActivity.this);
                    db.addSongs(mySongs.get(i).hashCode());

                }
                else{
                    DbHandler db = new DbHandler(MainActivity.this);
                    if(db.removeSong(mySongs.get(i).hashCode())){
                        Log.d("dbQuery", "1 song removed");
                    }
                }
            });
            return myView;
        }
    }

}