package com.example.ramana.moviedb.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ramana on 9/1/16.
 */
public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.example.ramana.moviedb";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_ENTRY = "entry";
    public static final String PATH_DETAILS = "details";
    public static final String PATH_TRAILERS = "trailers";
    public static final String PATH_REVIEWS = "reviews";

    /* Inner class that defines the table contents of the movie table */
    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ENTRY).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY +"/" + PATH_ENTRY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ENTRY;


        //Table name for movies
        public static final String TABLE_NAME = "movies";

        // contains id from movie db api in numerical format
        public static final String COLUMN_MOVIE_ID = "movie_id";

        // holds movie name in String format
        public static final String COLUMN_MOVIE_NAME = "movie_name";

        //holds movie image in BLOB ogject
        public static final String COLUMN_MOVIE_IMAGE = "movie_image";

        public static long parseidfromuri(Uri uri){
            return Long.parseLong(uri.getLastPathSegment());
        }
    }

    /* Inner class that defines the table contents of the details table */
    public static final class MovieDetails implements BaseColumns{

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_DETAILS).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE +"/"+ CONTENT_AUTHORITY + "/" + PATH_DETAILS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DETAILS;

        //table name for movie details
        public static final String TABLE_NAME = "details";

        //holds movie id as a foreign key from movies table
        public static final String COLUMN_MOVIE_ID = "movie_id";

        //holds release date as a string
        public static final String COLUMN_MOVIE_RELEASEDATE = "release_date";

        //holds movie overview as a string
        public static final String COLUMN_MOVIE_RATING = "rating";

        //holds movie rating as a string
        public static final String COLUMN_MOVIE_OVERVIEW = "overview";

    }

    /* Inner class that defines the table contents of the trailers table */
    public static final class MovieTrailers implements BaseColumns{

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILERS).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILERS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.ANY_CURSOR_ITEM_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILERS;

        //table name for movietrailers
        public static final String TABLE_NAME = "trailers";

        //holds movie id a sa foriegn key from movies table
        public static final String COLUMN_MOVIE_ID = "movie_id";

        //holds trailer name as a string
        public static final String COLUMN_TRAILER_NAME = "trailer_name";

        //holds trailer youtube key url as a string
        public static final String COLUMN_TRAILER_KEYURL = "trailer_key_url";

    }

    /* Inner class that defines the table contents of the reviews table */
    public static class MovieReviews implements BaseColumns{

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEWS).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;

        // table name for movie revires
        public static final String TABLE_NAME = "reviews";

        //holds movie id as a foriegn key
        public static final String COLUMN_MOVIE_ID = "movie_id";

        // HOLDS author name as a string
        public static final String COLUMN_AUTHOR = "author";

        //holds review for a perticular movie
        public static final String COLUMN_REVIEW_CONTENT = "review_content";
    }

}
