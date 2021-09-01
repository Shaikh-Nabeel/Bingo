package com.nabeel130.bingo;

import android.graphics.Bitmap;
import android.net.Uri;

public class Helper {
    String songName;
    long id;
    Uri contentUri;

    Helper(String songName,long id,Uri uri){
        this.songName = songName;
        this.id = id;
        this.contentUri = uri;
    }
}
