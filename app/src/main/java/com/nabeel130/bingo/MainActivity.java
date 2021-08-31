package com.nabeel130.bingo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.nabeel130.bingo.DbController.DbHandler;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;


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
    public boolean isGrantedManageStoragePermission = false;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewOfSong = findViewById(R.id.listViewOfSongs);
        listViewOfSong.setDivider(null);
        listViewOfSong.setDividerHeight(2);



        Dexter.withContext(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if(report.isAnyPermissionPermanentlyDenied()){
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Permission")
                                    .setMessage("This Application need particular permissions to start.\nGive permission from App setting.")
                                    .setPositiveButton("OK", (dialog, which) -> finish())
                                    .show();
                            return;
                        }
                        if(!report.areAllPermissionsGranted()){
                            Toast.makeText(MainActivity.this, "App need particular permissions to start", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        new Thread(){
                            @Override
                        public void run(){
                                RelativeLayout rl_start = findViewById(R.id.showOnStartLayout);
                                runOnUiThread(() -> {
                                    rl_start.setBackground(getDrawable(R.color.design_default_color_primary_variant));
                                    TextView tempText = findViewById(R.id.txtViewOfApp);
                                    tempText.setText(R.string.app_name);
                                    ImageView tempImg = findViewById(R.id.imageViewOfApp);
                                    tempImg.setImageResource(R.drawable.app_icon_);
                                });

                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(() -> {
                            ViewGroup parent = (ViewGroup) rl_start.getParent();
                            if (parent != null)
                                parent.removeView(rl_start);
                            FetchSongs fetchSongs = new FetchSongs();
                            fetchSongs.execute();
                            Toolbar toolbar = findViewById(R.id.customToolB);
                            toolbar.setTitleTextColor(getResources().getColor(R.color.white));
                            toolbar.setTitle(R.string.app_name);
                            setSupportActionBar(toolbar);
                        });
                    }
                }.start();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).onSameThread()
                .check();
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R){
            Dexter.withContext(this)
                    .withPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                            isGrantedManageStoragePermission = true;
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                            permissionToken.continuePermissionRequest();
                        }
                    }).onSameThread()
            .check();
        }
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
    public void openPlaySongActivity(int position){
        Intent intent = new Intent(MainActivity.this,PlaySong.class);
        intent.putExtra("position",position);
        intent.putExtra("className", getString(R.string.main_activity));
        startActivity(intent);
    }

    public void sortByName(){
        Collections.sort(mySongsCopy, (o1,o2)->o1.getName().compareTo(o2.getName()));
        defaultSort(mySongsCopy);
    }

    public void defaultSort(ArrayList<File> song){
        items = new String[song.size()];
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

    @SuppressLint("DefaultLocale")
    private String getFileSize(File file){
        String MB = " MB";
        String KB = " KB";
        double kb =(double) file.length()/1024;
        double mb = kb/1024;

        if(mb>1) {
            return String.format("%.2f", mb) + MB;
        }
        return String.format("%.2f", kb) + KB;
    }

    public void showAlertDialog(String title,String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(title)
                .setMessage(message)
                .show();
    }
    //delete current song from 'list', 'mySongs', 'mySongsCopy'
    private void removeFile(File file,int i, boolean isDeleted) throws IntentSender.SendIntentException {
        if(isDeleted){
            Toast.makeText(MainActivity.this, "File deleted", Toast.LENGTH_SHORT).show();
        }else{
            deleteFileV29plus(file);
        }
        try {
            String currFileHash = Integer.toString(list.get(i).hashCode());
            list.remove(i);
            defaultSort(list);
            ca.notifyDataSetChanged();
            new Thread(){
                @Override
                public void run(){
                    for(File file: mySongs){
                        if(Integer.toString(file.hashCode()).equals(currFileHash)) {
                            mySongs.remove(file);
                            break;
                        }
                    }
                    for(File file: mySongsCopy){
                        if(Integer.toString(file.hashCode()).equals(currFileHash)){
                            mySongsCopy.remove(file);
                            break;
                        }
                    }
                    if(favSongList.contains(currFileHash)){
                        favSongList.remove(currFileHash);
                        DbHandler dbHandler = new DbHandler(MainActivity.this);
                        dbHandler.removeSong(Integer.parseInt(currFileHash));
                        if(FavoriteSongs.finalList != null){
                            for(File file: FavoriteSongs.finalList){
                                if(Integer.toString(file.hashCode()).equals(currFileHash)){
                                    FavoriteSongs.finalList.remove(file);
                                    break;
                                }
                            }
                        }
                        Log.d("hashCode","1 song removed");
                    }
                }
            }.start();
        }catch(Exception e){
            e.printStackTrace();
        }
        PlaySong ps = new PlaySong();
        ps.setList();
    }

    private void deleteFileV29plus(File file) throws IntentSender.SendIntentException {
        String[] projection = {MediaStore.Audio.Media._ID};
        String selection = MediaStore.Audio.Media.DATA+"=?";
        String[] selectionArgs = new String[]{file.getAbsolutePath()};

        Uri queryUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        @SuppressLint("Recycle") Cursor c = contentResolver.query(queryUri,projection,selection,selectionArgs,null);

        if(c != null){
            if(c.moveToFirst()){
                long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                Uri deleteUri = ContentUris.withAppendedId(queryUri,id);
                try {
                    contentResolver.delete(deleteUri, null, null);
                }catch(SecurityException s){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        RecoverableSecurityException recoverableSecurityException;
                        if(s instanceof RecoverableSecurityException) {
                            recoverableSecurityException = (RecoverableSecurityException) s;
                        }
                        else{
                            return;
                        }
                        IntentSender intentSender = recoverableSecurityException.getUserAction().getActionIntent().getIntentSender();
                        startIntentSenderForResult(intentSender,34,null,0,0,0,null);
                    }
                }

            }else{
                Toast.makeText(MainActivity.this,"Couldn't delete", Toast.LENGTH_SHORT).show();
            }
            c.close();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 344) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(MainActivity.this, "File deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Couldn't delete", Toast.LENGTH_SHORT).show();
            }
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

        @SuppressLint("ViewHolder")
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
            textSong.setOnLongClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this,textSong);
                popupMenu.getMenuInflater().inflate(R.menu.menu_for_list_item,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @SuppressLint({"InflateParams", "ResourceType"})
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if(id == R.id.songDetailsBtn) {

                            File file = list.get(i);
                            @SuppressLint("SimpleDateFormat") DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                            Date date = new Date(file.lastModified());

                            Dialog builder = new Dialog(MainActivity.this);
                            builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            builder.setContentView(R.layout.custom_dialog_details);
                            builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            TextView nameOfSong, sizeOfSong, lastModifiedDate;
                            nameOfSong = builder.findViewById(R.id.songNameForDialog);
                            sizeOfSong = builder.findViewById(R.id.sizeOfTheSong);
                            lastModifiedDate = builder.findViewById(R.id.lastModifiedOfSong);
                            nameOfSong.setText(file.getName());
                            sizeOfSong.setText(getFileSize(file));
                            lastModifiedDate.setText(sdf.format(date));
                            builder.show();

                        }
                        else if(id == R.id.deleteSong){
                            if(clickedOnIndex == i){
                                Toast.makeText(MainActivity.this,"Couldn't delete", Toast.LENGTH_SHORT).show();
                                return true;
                            }
                            File file = new File(String.valueOf(list.get(i)));
                            if(file.exists()){
                                boolean isDeleted = file.delete();
                                try {
                                    removeFile(file,i, isDeleted);
                                } catch (IntentSender.SendIntentException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                        return true;
                    }
                });
                popupMenu.show();
                return true;
            });
            textSong.setOnClickListener(v -> openPlaySongActivity(i));
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