package com.example.ramana.moviedb.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import com.example.ramana.moviedb.data.MovieContract.MovieDetails;
import com.example.ramana.moviedb.data.MovieContract.MovieEntry;
import com.example.ramana.moviedb.data.MovieContract.MovieReviews;
import com.example.ramana.moviedb.data.MovieContract.MovieTrailers;

/**
 * Created by ramana on 9/1/16.
 */
public class MovieDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "movie.db";


    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            db.setForeignKeyConstraintsEnabled(true);
        }else
            db.execSQL("PRAGMA foreign_keys=ON;");
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        /*
        * Create movies table that contains movie details such as
        * movie name, moive id from theMovieDbAPI and movie poster image
        * */
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE "+ MovieEntry.TABLE_NAME+" (" +
                MovieEntry._ID +" INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieEntry.COLUMN_MOVIE_ID +" TEXT UNIQUE NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_NAME +" TEXT NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_IMAGE +" BLOB NOT NULL, " +
                "UNIQUE ("+MovieEntry.COLUMN_MOVIE_ID +") ON CONFLICT REPLACE" +
                ");";

        final String SQL_CREATE_MOVIE_DETAILS = "CREATE TABLE "+MovieDetails.TABLE_NAME +"(" +
                MovieDetails._ID + " INTEGER PRIMARY KEY ," +
                MovieDetails.COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
                MovieDetails.COLUMN_MOVIE_RELEASEDATE+" TEXT NOT NULL, " +
                MovieDetails.COLUMN_MOVIE_RATING+" TEXT NOT NULL, " +
                MovieDetails.COLUMN_MOVIE_OVERVIEW+" TEXT NOT NULL, " +
                //set foriegn key as movie id from database
                " FOREIGN KEY (" + MovieDetails.COLUMN_MOVIE_ID +") REFERENCES " +
                MovieEntry.TABLE_NAME+"("+MovieEntry.COLUMN_MOVIE_ID +")  ON DELETE CASCADE " +
                ");";

        final String SQL_CREATE_MOVIE_TRAILERS = "CREATE TABLE "+ MovieTrailers.TABLE_NAME +"(" +
                MovieTrailers._ID+" TEXT PRIMARY KEY, " +
                MovieTrailers.COLUMN_MOVIE_ID+" TEXT NOT NULL, " +
                MovieTrailers.COLUMN_TRAILER_NAME +" TEXT NOT NULL, " +
                MovieTrailers.COLUMN_TRAILER_KEYURL +" TEXT NOT NULL, " +
                // SET FORIEGN KEY AS MOVIE ID
                " FOREIGN KEY ("+ MovieTrailers.COLUMN_MOVIE_ID +") REFERENCES " +
                MovieEntry.TABLE_NAME +"("+ MovieEntry.COLUMN_MOVIE_ID + ") ON DELETE CASCADE, "+
                "UNIQUE ("+MovieTrailers.COLUMN_MOVIE_ID+","
                +MovieTrailers.COLUMN_TRAILER_KEYURL+") ON CONFLICT REPLACE "+
                ");";


        final String SQL_CREATE_MOVIE_REVIEWS = "CREATE TABLE "+ MovieReviews.TABLE_NAME+"(" +
                MovieReviews._ID +" INTEGER PRIMARY KEY, " +
                MovieReviews.COLUMN_MOVIE_ID +" TEXT NOT NULL, " +
                MovieReviews.COLUMN_AUTHOR +" TEXT NOT NULL, " +
                MovieReviews.COLUMN_REVIEW_CONTENT + " TEXT NOT NULL, " +
                " FOREIGN KEY ("+MovieReviews.COLUMN_MOVIE_ID +") REFERENCES " +
                MovieEntry.TABLE_NAME+"("+MovieEntry.COLUMN_MOVIE_ID +") ON DELETE CASCADE, " +
                " UNIQUE("+MovieReviews.COLUMN_MOVIE_ID+"" +
                        ","+MovieReviews.COLUMN_AUTHOR+") ON CONFLICT REPLACE"+
                ");";

        db.execSQL(SQL_CREATE_MOVIES_TABLE);
        db.execSQL(SQL_CREATE_MOVIE_DETAILS);
        db.execSQL(SQL_CREATE_MOVIE_TRAILERS);
        db.execSQL(SQL_CREATE_MOVIE_REVIEWS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS "+ MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ MovieDetails.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieTrailers.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ MovieReviews.TABLE_NAME);

        onCreate(db);
    }
}
