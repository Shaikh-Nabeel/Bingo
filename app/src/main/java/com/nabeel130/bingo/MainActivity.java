package com.nabeel130.bingo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


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
    private int posOfDeletedItem;

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
                            if(getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Permission")
                                        .setMessage("This Application need particular permissions to start.\nGive permission from App setting.")
                                        .setPositiveButton("OK", (dialog, which) -> finish())
                                        .show();
                                return;
                            }
                        }
                        if(!report.areAllPermissionsGranted()){
                            if(getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                                Toast.makeText(MainActivity.this, "App need particular permissions to start", Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }
                        }

                    new Thread(){
                        @Override
                        public void run(){
                                RelativeLayout rl_start = findViewById(R.id.showOnStartLayout);
                                runOnUiThread(() -> {
                                    rl_start.setBackground(getDrawable(R.color.purple_700));
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
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mySongs = (ArrayList<File>) fetchAudiosForV29plus();
//            }else{
//                mySongs = fetchSong(Environment.getExternalStorageDirectory());
//            }
            mySongsCopy = new ArrayList<>();
            mySongsCopy.addAll(mySongs);

            //fetching favourite songs
            DbHandler dbHandler = new DbHandler(MainActivity.this);
            favSongList = (ArrayList<String>) dbHandler.getAllSongs();
            if(mySongs.isEmpty()){
                return "noSong";
            }
            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            if(result == null) return;
            if(result.equals("noSongs")){
                dialog.dismiss();
                setTextIfNoSong();
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
    private void removeFile(int i) throws IntentSender.SendIntentException {

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

    @SuppressLint("Recycle")
    private List<File> fetchAudiosForV29plus(){
        //project is what we want to select
        String[] projection = {MediaStore.Audio.Media.DATA};

        List<File> listV29plus = new ArrayList<>();
        Uri uri;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            uri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        }else{
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        ContentResolver contentResolver = getContentResolver();

        Cursor c = contentResolver.query(uri,projection,null,null,null);
        if(c != null){
            while(c.moveToNext()){
                  int columnIndex  = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                String fileName = c.getString(columnIndex);
                if(fileName.endsWith(".mp3") && !fileName.startsWith("."))
                    listV29plus.add(new File(c.getString(columnIndex)));
            }
        }
        return listV29plus;
    }

    //launcher for asking permission to user to delete particular file and return result
    private final ActivityResultLauncher<IntentSenderRequest> launcher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(this, "deleted", Toast.LENGTH_SHORT).show();
                    try {
                        removeFile(posOfDeletedItem);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(this, "Couldn't delete", Toast.LENGTH_SHORT).show();
                }
            });

    private void deleteFileV29plus(File file) throws IntentSender.SendIntentException {

        //projection is what we want in result,In this case we want ID of the audio file
        String[] projection = {MediaStore.Audio.Media._ID};
        //selection is 'where' clause like in database (where Media.data =?)
        String selection = MediaStore.Audio.Media.DATA+"=?";
        //selection Arguments is on what basis should contentResolver fetch result
        //this execute when selection string contains "?", this ? specify that resolver should look for selectionArgs
        String[] selectionArgs = new String[]{file.getAbsolutePath()};

        //this is from where you want to run the query,in this case it is external storage(scoped storage).
        Uri queryUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        //content resolver will run the query on the device and return Cursor object
        @SuppressLint("Recycle") Cursor c = contentResolver.query(queryUri,projection,selection,selectionArgs,null);

        if(c != null){
            if(c.moveToFirst()){
                //getting id of the file we passed in selectionArguments
                long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                //making delete Uri, withAppendedId tells that, delete the file where id = id you passed in it
                Uri deleteUri = ContentUris.withAppendedId(queryUri,id);
                try {
                    //content resolver will run delete query
                    contentResolver.delete(deleteUri, null, null);
                    //if version if < android 10 then file will be deleted
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                        removeFile(posOfDeletedItem);
                    }
                    //but if version >= android 10 then it will throw security exception
                }catch(SecurityException s){

                    PendingIntent pendingIntent = null;
                    /*
                    In android 11 we will call delete request through content resolver
                    This feature is only available in > android 11, even IDE will request you to add @RequiredApi(R)
                    */
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                        ArrayList<Uri> listOfUri= new ArrayList<>();
                        listOfUri.add(deleteUri);
                        pendingIntent = MediaStore.createDeleteRequest(contentResolver,listOfUri);
                    }
                    /*
                    but in android 10 security exception can be cast as RecoverableSecurityException
                    and we can get actionIntent to ask user permission
                    */
                    else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if(s instanceof RecoverableSecurityException) {
                            pendingIntent = ((RecoverableSecurityException) s).getUserAction().getActionIntent();
                        }
                        else{
                            return;
                        }
                    }

                    //we will get IntentSender and build IntentSenderRequest then launch the request through launder
                    //launcher is basically prompt which ask user to deny or accept and get result from intent
                    if(pendingIntent != null){
                        IntentSender intentSender = pendingIntent.getIntentSender();
                        IntentSenderRequest request = new IntentSenderRequest.Builder(intentSender).build();
                        launcher.launch(request);
                    }
                }

            }else{
                Toast.makeText(MainActivity.this,"Couldn't delete", Toast.LENGTH_SHORT).show();
            }
            c.close();
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
                popupMenu.setOnMenuItemClickListener(item -> {
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
//                            boolean isDeleted = file.delete();
                            try {
                                posOfDeletedItem = i;
                                deleteFileV29plus(file);
                                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                                    Toast.makeText(MainActivity.this,"File delted",Toast.LENGTH_SHORT).show();
                                }
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                    return true;
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