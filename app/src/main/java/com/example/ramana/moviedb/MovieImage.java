package com.example.ramana.moviedb;

/**
 * Created by ramana on 20/12/15.
 */
public class MovieImage {
    private  String image_path;
    private int movie_id;

    public MovieImage(String  path, int id){
        this.image_path = path;
        this.movie_id = id;
    }

    public String getImage_path(){
        return  this.image_path;
    }

    public int getMovie_id(){
        return this.movie_id;
    }

}
