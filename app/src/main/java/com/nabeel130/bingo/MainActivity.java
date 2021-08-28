package com.nabeel130.bingo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import java.util.Collections;


public class MainActivity extends AppCompatActivity {

    private ListView listViewOfSong;
    private static String[] items;
    public static ArrayList<File> mySongs;
    private static ArrayList<File> mySongsCopy;
    public static ArrayList<String> favSongList;
    public static ArrayList<File> list;
    public static CustomAdapter ca;
    public static int clickedOnIndex= -1;
    public static int hashOfCurrentSong = -1;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewOfSong = findViewById(R.id.listViewOfSongs);
        listViewOfSong.setDivider(null);
        listViewOfSong.setDividerHeight(2);

        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        new Thread(){
                            @Override
                        public void run(){
                                RelativeLayout rl_start = findViewById(R.id.showOnStartLayout);
                                runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    rl_start.setBackground(getDrawable(R.color.design_default_color_primary_variant));
                                    TextView tempText = findViewById(R.id.txtViewOfApp);
                                    tempText.setText(R.string.app_name);
                                    ImageView tempImg = findViewById(R.id.imageViewOfApp);
                                    tempImg.setImageResource(R.drawable.app_icon_);
                                }
                                });

                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ViewGroup parent = (ViewGroup) rl_start.getParent();
                                if (parent != null)
                                    parent.removeView(rl_start);
                                FetchSongs fetchSongs = new FetchSongs();
                                fetchSongs.execute();
                                Toolbar toolbar = findViewById(R.id.customToolB);
                                toolbar.setTitleTextColor(getResources().getColor(R.color.white));
                                toolbar.setTitle(R.string.app_name);
                                setSupportActionBar(toolbar);
                            }

                        });
                    }
                }.start();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        finish();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.my_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }


    public void sortByDate(){
        Collections.sort(mySongsCopy,(o1,o2)-> {
            long d = o2.lastModified() - o1.lastModified();
            if(d == 0)return 0;
            return d>0?1:-1;
        });
        defaultSort(mySongsCopy);
    }

    //this method sort playlist
    public void handleSorting(String text){
        if(text.equals(getString(R.string.SortByName))) {
            sortByName();
            list = mySongsCopy;
        }
        else if(text.equals(getString(R.string.defaultSort))){
            defaultSort(mySongs);
            list = mySongs;
        }else if(text.equals(getString(R.string.sortByDate))){
            sortByDate();
            list = mySongsCopy;
        }
        PlaySong playSong = new PlaySong();
        playSong.setList(list,clickedOnIndex);
        ca.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem){
        int id = menuItem.getItemId();

        if(id == R.id.favoriteSongBtn){
            Intent intent = new Intent(MainActivity.this,FavoriteSongs.class);
            startActivity(intent);
        }else if(id == R.id.sortByNameBtn){
            handleSorting(menuItem.getTitle().toString());
        }else if(id == R.id.defaultSortBtn){
            handleSorting(menuItem.getTitle().toString());
        }else if(id == R.id.sortByDateBtn){
            handleSorting(menuItem.getTitle().toString());
        }

        return super.onOptionsItemSelected(menuItem);
    }

    public void updateUI(){
        items = new String[mySongs.size()];
        defaultSort(mySongs);
        list = mySongs;

        ca = new CustomAdapter();
        listViewOfSong.setAdapter(ca);

    }

    //redirecting to playSong Activity
    public void openPlaySongActivity(int position, ArrayList<File> list){
        Intent intent = new Intent(MainActivity.this,PlaySong.class);
        intent.putExtra("songList",list);
        intent.putExtra("position",position);
        intent.putExtra("className", getString(R.string.main_activity));
        startActivity(intent);
    }

    public void sortByName(){
        Collections.sort(mySongsCopy, (o1,o2)->o1.getName().compareTo(o2.getName()));
        defaultSort(mySongsCopy);
    }

    public void defaultSort(ArrayList<File> song){
        for(int i=0; i<song.size(); i++){
            if(song.get(i).hashCode() == hashOfCurrentSong)
                clickedOnIndex = i;
            items[i] = song.get(i).getName().replace(".mp3","");
        }
    }

    //fetching song from external storage
    public static ArrayList<File> fetchSong(File file){
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

    private void setTextIfNoSong(){
        TextView txtView = findViewById(R.id.txtShowIfNoSong1);
        txtView.setText(getString(R.string.noSongs2));
        txtView.setTextColor(Color.WHITE);
    }

    private class FetchSongs extends AsyncTask<Void, Void, String>{

        Dialog dialog = new Dialog(MainActivity.this);
        @Override
        protected String doInBackground(Void... voids) {
            mySongs = fetchSong(Environment.getExternalStorageDirectory());
            mySongsCopy = new ArrayList<>();
            mySongsCopy.addAll(mySongs);

            //fetching favourite songs
            DbHandler dbHandler = new DbHandler(MainActivity.this);
            favSongList = (ArrayList<String>) dbHandler.getAllSongs();
            if(mySongs.isEmpty()){
                setTextIfNoSong();
                return "noSong";
            }
            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            if(result == null) return;
            if(result.equals("noSongs")){
                dialog.dismiss();
                return;
            }
            dialog.dismiss();
            updateUI();
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        protected void onPreExecute() {

            dialog.setCancelable(false);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_dialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        }
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
            textSong.setText(items[i]);

            //assigning magenta colour on item which is being clicked
            if(clickedOnIndex == i) {
                textSong.setSelected(true);
                textSong.setTextColor(Color.MAGENTA);
                hashOfCurrentSong = list.get(i).hashCode();
            }

            textSong.setOnClickListener(v -> openPlaySongActivity(i, list));
            ToggleButton toggleButton = myView.findViewById(R.id.imgSong);

            if(favSongList.contains(Integer.toString(list.get(i).hashCode()))){
                toggleButton.setChecked(true);
            }
            toggleButton.setOnClickListener(v -> {
                if(toggleButton.isChecked()){
                    DbHandler db = new DbHandler(MainActivity.this);
                    int hCode = list.get(i).hashCode();
                    db.addSongs(hCode);
                    favSongList.add(Integer.toString(hCode));
                }
                else{
                    DbHandler db = new DbHandler(MainActivity.this);
                    if(db.removeSong(list.get(i).hashCode())){
                        favSongList.remove(Integer.toString(list.get(i).hashCode()));
                    }
                }
            });
            return myView;
        }
    }

}