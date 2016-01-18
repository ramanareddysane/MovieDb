package com.example.ramana.moviedb;

import android.graphics.Bitmap;

/**
 * Created by ramana on 20/12/15.
 */
public class MovieImage {
    private  String image_path;
    private int movie_id;
    private Bitmap bitmap;

    public MovieImage(String  path, int id){
        this.image_path = path;
        this.movie_id = id;
        this.bitmap = null;
    }

    public MovieImage(Bitmap bitmap,int id){
        this.bitmap = bitmap;
        this.movie_id = id;
        this.image_path = null;
    }

    public String getImage_path(){
        return  this.image_path;
    }

    public int getMovie_id(){
        return this.movie_id;
    }

    public Bitmap getBitmap(){ return this.bitmap; }

}
