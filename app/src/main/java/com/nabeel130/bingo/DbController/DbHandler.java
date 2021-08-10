package com.nabeel130.bingo.DbController;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DbHandler extends SQLiteOpenHelper {

    public DbHandler(Context context){
        super(context,Params.DB_NAME,null,Params.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE "+Params.TABLE_NAME+"("
                +Params.KEY_ID+" INTEGER PRIMARY KEY,"+Params.KEY_HASH+" TEXT)";
        Log.d("dbQuery",createTable);
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void addSongs(int hashCode){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Params.KEY_HASH, Integer.toString(hashCode));

        db.insert(Params.TABLE_NAME,null,values);
        Log.d("dbQuery","1 song inserted");
        db.close();

    }

    public boolean removeSong(int hashCode){
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(Params.TABLE_NAME,Params.KEY_HASH+" = "+Integer.toString(hashCode),null)>0;
    }

    public List<String> getAllSongs(){
        List<String> songList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String readDb = "SELECT * FROM "+Params.TABLE_NAME;
        Cursor cursor = db.rawQuery(readDb,null);

        if(cursor.moveToFirst()){
            do{
                String song = cursor.getString(1);
                songList.add(song);
            }while(cursor.moveToNext());
        }
        return songList;
    }
}
