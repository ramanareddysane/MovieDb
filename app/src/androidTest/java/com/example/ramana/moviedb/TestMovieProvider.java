package com.example.ramana.moviedb;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.ramana.moviedb.data.MovieContract;
import com.example.ramana.moviedb.data.MovieContract.MovieDetails;
import com.example.ramana.moviedb.data.MovieContract.MovieEntry;
import com.example.ramana.moviedb.data.MovieContract.MovieReviews;
import com.example.ramana.moviedb.data.MovieContract.MovieTrailers;
import com.example.ramana.moviedb.data.MovieDBHelper;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;


/**
 * Created by ramana on 9/1/16.
 */
public class TestMovieProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestMovieProvider.class.getSimpleName();

    public void testDelete(){
        mContext.deleteDatabase(MovieDBHelper.DATABASE_NAME);
    }

    public void testGetType(){
        String type = mContext.getContentResolver().getType(MovieEntry.CONTENT_URI);
        assertEquals(MovieEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testInsertReadProvider(){
        /*
        * Inserting necessary values to Movie Entry table
        * and test them by retrieving them
        */
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.play);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] bytearray = bos.toByteArray();

        ContentValues values = new ContentValues();
        values.put(MovieEntry.COLUMN_MOVIE_ID, "126514");
        values.put(MovieEntry.COLUMN_MOVIE_NAME, "ramana");
        values.put(MovieEntry.COLUMN_MOVIE_IMAGE, bytearray);

        Uri ret_uri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, values);
        Log.v("ramana1", " URI is :" + ret_uri);
        long inserted_id = MovieEntry.parseidfromuri(ret_uri);
        Log.v("ramana1", "inserted row has id :" + inserted_id);



        ContentValues extravalues = new ContentValues();
        extravalues.put(MovieEntry.COLUMN_MOVIE_ID, "26514");
        extravalues.put(MovieEntry.COLUMN_MOVIE_NAME, "ramana");
        extravalues.put(MovieEntry.COLUMN_MOVIE_IMAGE, bytearray);

        ret_uri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, extravalues);
         Log.v("ramana1", " URI is :" + ret_uri);
         Long inserted = MovieEntry.parseidfromuri(ret_uri);
         Log.v("ramana1","inserted  2nd row has id :" + inserted);

        // get the movie id from inserted row
        Cursor cursor = mContext.getContentResolver().query(MovieEntry.CONTENT_URI,
                new String[]{MovieEntry.COLUMN_MOVIE_ID},
                MovieEntry._ID +" = ? ",
                new String[]{Long.toString(inserted_id)},
                null);
        String movie_id = null;
        if(cursor.moveToFirst()){
            movie_id = cursor.getString(0);
            Log.v("ramana1","INserted Second row movie id: " + movie_id);
            assertEquals("126514", movie_id);
        }
        //get the movie id for second row
        Cursor extracursor = mContext.getContentResolver().query(MovieEntry.CONTENT_URI,
                new String[]{MovieEntry.COLUMN_MOVIE_ID},
                MovieEntry._ID +" = ? ",
                new String[]{Long.toString(inserted)},
                null);
        if(extracursor.moveToFirst()){
            Log.v("ramana1","INserted Second row movie id: " + extracursor.getString(0));
            assertEquals("26514",extracursor.getString(0));
        }

        //Insert movie details into details table and test them.
        ContentValues values1 = new ContentValues();
        values1.put(MovieDetails.COLUMN_MOVIE_ID,"126514");
        values1.put(MovieDetails.COLUMN_MOVIE_RELEASEDATE,"19-11-2015");
        values1.put(MovieDetails.COLUMN_MOVIE_RATING, "8.3/10");
        values1.put(MovieDetails.COLUMN_MOVIE_OVERVIEW, "OVER VIEW");

        Uri retUri = mContext.getContentResolver().insert(
                MovieDetails.CONTENT_URI,values1);
        Log.v("ramana1","details inserted row has id: "+retUri.getLastPathSegment());

        String[] columns1 = {
                MovieDetails.TABLE_NAME+"."+MovieDetails.COLUMN_MOVIE_ID,
                MovieDetails.COLUMN_MOVIE_RELEASEDATE,
                MovieDetails.COLUMN_MOVIE_RATING,
                MovieDetails.COLUMN_MOVIE_OVERVIEW,
        };

        Cursor cursor1 =mContext.getContentResolver()
                .query(MovieDetails.CONTENT_URI.buildUpon().appendPath("126514").build(),
                        columns1, null, null, null);
        if(cursor1.moveToFirst()){
            assertEquals("126514",cursor1.getString(0));
            assertEquals("19-11-2015",cursor1.getString(1));
            assertEquals("8.3/10",cursor1.getString(2));
            assertEquals("OVER VIEW",cursor1.getString(3));
        }


        /*
        * retrieve combined data from both
        * movies and entried table and
        * test aquired results
        */

        String[] projectionsforDetails = {
                MovieEntry.TABLE_NAME+"."+MovieEntry.COLUMN_MOVIE_ID,
                MovieEntry.COLUMN_MOVIE_NAME,
                MovieEntry.COLUMN_MOVIE_IMAGE,
                MovieDetails.COLUMN_MOVIE_RELEASEDATE,
                MovieDetails.COLUMN_MOVIE_RATING,
                MovieDetails.COLUMN_MOVIE_OVERVIEW
        };
        Cursor detail_cursor =mContext.getContentResolver()
                .query(MovieDetails.CONTENT_URI.buildUpon().appendPath(movie_id).build(),
                        projectionsforDetails, null, null, null);
        if(detail_cursor.moveToFirst()){
            byte[] byte_array = detail_cursor.getBlob(2);
            Bitmap b = BitmapFactory.decodeByteArray(byte_array,0,byte_array.length);

            Log.v("ramana1",detail_cursor.getString(0));
            Log.v("ramana1", detail_cursor.getString(1));
            Log.v("ramana1", detail_cursor.getString(3));
            Log.v("ramana1", detail_cursor.getString(4));
            Log.v("ramana1", detail_cursor.getString(5));

            assertEquals("126514",detail_cursor.getString(0));
            assertEquals("ramana", detail_cursor.getString(1));
            assertTrue(Arrays.equals(bytearray, byte_array));
            assertEquals("19-11-2015",detail_cursor.getString(3));
            assertEquals("8.3/10",detail_cursor.getString(4));
            assertEquals("OVER VIEW",detail_cursor.getString(5));
        }

        /*
        * Add three trailers into trailers table which contains
        * same movie_id and test them by retrieving
        */
        ContentValues trailer1 = new ContentValues();
        trailer1.put(MovieTrailers.COLUMN_MOVIE_ID, "126514");
        trailer1.put(MovieTrailers.COLUMN_TRAILER_NAME, "trailer");
        trailer1.put(MovieTrailers.COLUMN_TRAILER_KEYURL, "rtsdJSifjjsjY-sfg");

        ContentValues trailer2 = new ContentValues();
        trailer2.put(MovieTrailers.COLUMN_MOVIE_ID, "126514");
        trailer2.put(MovieTrailers.COLUMN_TRAILER_NAME, "trailer");
        trailer2.put(MovieTrailers.COLUMN_TRAILER_KEYURL, "rtsdJSifjjsjY-sfg");

        ContentValues[] values2 = new ContentValues[]{trailer1,trailer2};
//        Uri insertedUri = mContext.getContentResolver().insert(MovieTrailers.CONTENT_URI, trailer1);
        int no_of_rows_inserted = mContext.getContentResolver().bulkInsert(MovieTrailers.CONTENT_URI, values2);
//        String inserted_movie_id = insertedUri.getLastPathSegment();
//        assertTrue(inserted_movie_id != trailer1.getAsString(MovieTrailers.COLUMN_MOVIE_ID));
        assertEquals(values2.length,no_of_rows_inserted);

        String[] columns2 = {
                MovieContract.MovieTrailers.COLUMN_TRAILER_NAME,
                MovieContract.MovieTrailers.COLUMN_TRAILER_KEYURL
        };
        Cursor cursor2 = mContext.getContentResolver()
                .query(MovieTrailers.CONTENT_URI.buildUpon().appendPath("126514").build(),
                        columns2,
                        MovieTrailers.COLUMN_MOVIE_ID+" = ? ",
                        new String[]{"126514"},
                        null);
        if(cursor2.moveToFirst()){
            do {
//                  Log.v("ramana1","trailer name is: "+cursor2.getString(0));
//                  Log.v("ramana1","trailer url is: "+cursor2.getString(1));
                assertEquals("trailer",cursor2.getString(0));
                assertEquals("rtsdJSifjjsjY-sfg",cursor2.getString(1));
            }while (cursor2.moveToNext());

        }

        /*
        * Add three Reviews into trailers table which contains
        * same movie_id and test them by retrieving
        */

        ContentValues review1 = new ContentValues();
        review1.put(MovieReviews.COLUMN_MOVIE_ID, "126514");
        review1.put(MovieReviews.COLUMN_AUTHOR, "ramana");
        review1.put(MovieReviews.COLUMN_REVIEW_CONTENT, "So good");

        ContentValues review2 = new ContentValues();
        review2.put(MovieReviews.COLUMN_MOVIE_ID, "126514");
        review2.put(MovieReviews.COLUMN_AUTHOR, "ramana");
        review2.put(MovieReviews.COLUMN_REVIEW_CONTENT, "So good");


//    Uri review_ret_uri = mContext.getContentResolver().insert(MovieReviews.CONTENT_URI,review1);
//     mContext.getContentResolver().insert(MovieReviews.CONTENT_URI,review1);
//    assertEquals(review1.getAsString(MovieReviews.COLUMN_MOVIE_ID), review_ret_uri.getLastPathSegment());
      int no_rows =  mContext.getContentResolver().bulkInsert(MovieReviews.CONTENT_URI,
               new ContentValues[]{review1,review2});
       assertEquals(2,no_rows);

      String[] columns3 = {
             MovieReviews.COLUMN_AUTHOR,
             MovieReviews.COLUMN_REVIEW_CONTENT
      };
      Cursor cursor3 = mContext.getContentResolver()
              .query(MovieReviews.CONTENT_URI.buildUpon().appendPath(movie_id).build(),
                       columns3,
                       MovieEntry.COLUMN_MOVIE_ID+" = ? ",
                       new String[]{movie_id},
                       null);

      if(cursor3.moveToFirst()){
           do{
              Log.v("ramana1",cursor3.getString(0));
              Log.v("ramana1",cursor3.getString(1));
           }while(cursor3.moveToNext());
      }

    }// End of testInsertReadProvider


    public void testdelete(){

        Log.v("ramana1", " in test delete");

        int rows_effected = mContext.getContentResolver()
                .delete(MovieEntry.CONTENT_URI.buildUpon().appendPath("126514").build(), null, null);
        assertTrue(rows_effected != 0);
        Log.v("ramana1","rows deleted : "+ rows_effected);


        //Check reviews for deleted movie entry;
        String[] columns3 = {
                MovieReviews.COLUMN_AUTHOR,
                MovieReviews.COLUMN_REVIEW_CONTENT
        };
        Cursor cursor3 = mContext.getContentResolver()
                .query(MovieReviews.CONTENT_URI.buildUpon().appendPath("126514").build(),
                        columns3,
                        MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                        new String[]{"126514"},
                        null);

        if(cursor3.moveToFirst()){
            do{
                Log.v("ramana1",cursor3.getString(0));
                Log.v("ramana1",cursor3.getString(1));
            }while(cursor3.moveToNext());
        }else
            Log.v("ramana1","no entry in reviews table");
    } // End of testDelete
}
