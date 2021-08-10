package com.nabeel130.bingo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.nabeel130.bingo.DbController.DbHandler;

import java.io.File;
import java.util.ArrayList;

public class FavoriteSongs extends AppCompatActivity {
    ListView listView;
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        listView =(ListView) findViewById(R.id.listViewOfSong);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        ArrayList<File> mySong =(ArrayList) bundle.getParcelableArrayList("mySongs");
        DbHandler dbHandler = new DbHandler(getApplicationContext());
        ArrayList<String> favSong =(ArrayList<String>) dbHandler.getAllSongs();
        if(favSong.size() > 0) {
            ArrayList<File> finalList = new ArrayList<>();
            for (int i = 0; i < mySong.size(); i++) {
                if (favSong.contains(Integer.toString(mySong.get(i).hashCode()))) {
                    finalList.add(mySong.get(i));
                }
            }
            String[] items = new String[finalList.size()];
            for (int i = 0; i < finalList.size(); i++) {
                items[i] = finalList.get(i).getName();
            }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, items);
                if(!arrayAdapter.isEmpty())
                    listView.setAdapter(arrayAdapter);
        }
    }

}
