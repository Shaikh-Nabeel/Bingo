package com.nabeel130.bingo;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ListView listViewOfSong;
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
                        ArrayList<File> mySongs = fetchSong(Environment.getExternalStorageDirectory());
                        String[] items = new String[mySongs.size()];
                        for(int i=0; i<mySongs.size(); i++){
                            items[i] = mySongs.get(i).getName().replace(".mp3","");
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, items);
                        listViewOfSong.setAdapter(adapter);

                        listViewOfSong.setOnItemClickListener((parent, view, position, id) -> {
                            Intent intent = new Intent(MainActivity.this,PlaySong.class);
                            String currSong = listViewOfSong.getItemAtPosition(position).toString();
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

}