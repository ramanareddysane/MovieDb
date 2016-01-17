package com.example.ramana.moviedb;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.ramana.moviedb.data.MovieContract.MovieReviews;
import com.example.ramana.moviedb.data.MovieContract.MovieTrailers;
import com.example.ramana.moviedb.data.MovieContract.MovieDetails;
import com.example.ramana.moviedb.data.MovieContract.MovieEntry;
import com.example.ramana.moviedb.data.MovieDBHelper;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * Created by ramana on 9/1/16.
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {

        // deleting the database for clean testing
        mContext.deleteDatabase(MovieDBHelper.DATABASE_NAME);
        SQLiteDatabase db = new MovieDBHelper(mContext).getWritableDatabase();

        assertEquals(true,db.isOpen());

        db.close();
    }

    /*
    * Tests to check whether insertion is performed
    * correctly or not in all tables
    */
    public void testInsertDb() {
        /*
        *
        * Insert values into movie entry database and
        * test the returned values by quering them
        *
        */
        // get Bitmap from drawable resourse
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.play);

        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, arrayOutputStream);
        byte[] byte_array = arrayOutputStream.toByteArray();

        ContentValues values = new ContentValues();
        values.put(MovieEntry.COLUMN_MOVIE_ID, "126514");
        values.put(MovieEntry.COLUMN_MOVIE_NAME, "ramana");
        values.put(MovieEntry.COLUMN_MOVIE_IMAGE, byte_array);

        MovieDBHelper helper = new MovieDBHelper(mContext);
        SQLiteDatabase database = helper.getWritableDatabase();

        long row_id = database.insert(MovieEntry.TABLE_NAME, null, values);

        // Verify that we got a row back
        assertTrue(row_id != -1);
        Log.v(LOG_TAG, "created id :" + row_id);

        // pull data from cursor to make sure the data is inserted correctly.
        String[] columns = {
                MovieEntry.COLUMN_MOVIE_ID,
                MovieEntry.COLUMN_MOVIE_NAME,
                MovieEntry.COLUMN_MOVIE_IMAGE
        };

        Cursor cursor = database.query(
                MovieEntry.TABLE_NAME,
                columns, //string array of columns we want to retrieve from database.
                null,   //columns for where clause
                null,   //arguments  for where clause
                null,   //columns for group by
                null,   // columns to filter by row groups
                null    //sort oder
        );

        String id = null;
        String name = null;
        Bitmap b = null;
        byte[] array = new byte[0];
        if (cursor.moveToFirst()) {
            id = cursor.getString(0);
            name = cursor.getString(1);
            array = cursor.getBlob(2);
            b = BitmapFactory.decodeByteArray(array, 0, array.length);
        }
        // Test whether data inserted are correct or not
        assertEquals("126514", id);
        assertEquals("ramana", name);
        assertTrue(Arrays.equals(array,byte_array));


        /*
        *
        * Insert values into DETAILS and check the
        * inserted values whether they are correct or not
        *
        * */

        ContentValues values1 = new ContentValues();
        values1.put(MovieDetails.COLUMN_MOVIE_ID,"126514");
        values1.put(MovieDetails.COLUMN_MOVIE_RELEASEDATE,"19-11-2015");
        values1.put(MovieDetails.COLUMN_MOVIE_RATING,"8.3/10");
        values1.put(MovieDetails.COLUMN_MOVIE_OVERVIEW, "OVER VIEW");

        row_id = database.insert(MovieDetails.TABLE_NAME, null, values1);
        assertTrue( row_id != -1);

        String[] columns1 = {
                MovieDetails.COLUMN_MOVIE_ID,
                MovieDetails.COLUMN_MOVIE_RELEASEDATE,
                MovieDetails.COLUMN_MOVIE_RATING,
                MovieDetails.COLUMN_MOVIE_OVERVIEW,
        };

        Cursor cursor1 = database.query(MovieDetails.TABLE_NAME,
                columns1, null, null,null,null,null);
        if(cursor1.moveToFirst()){
            assertEquals("126514",cursor1.getString(0));
            assertEquals("19-11-2015",cursor1.getString(1));
            assertEquals("8.3/10",cursor1.getString(2));
            assertEquals("OVER VIEW",cursor1.getString(3));
        }

        /*
        * Insert values into  TRAILERS and check the
        * inserted values whether they are correct or not
        *
        * */

        String[] columns2 = {
                MovieTrailers.COLUMN_MOVIE_ID,
                MovieTrailers.COLUMN_TRAILER_NAME,
                MovieTrailers.COLUMN_TRAILER_KEYURL
        };

        ContentValues values2 = new ContentValues();
        values2.put(MovieTrailers.COLUMN_MOVIE_ID,"126514");
        values2.put(MovieTrailers.COLUMN_TRAILER_NAME,"trailer");
        values2.put(MovieTrailers.COLUMN_TRAILER_KEYURL,"rtsdJSifjjsjY-sfg");

        row_id = database.insert(MovieTrailers.TABLE_NAME,null,values2);
        assertTrue(row_id != -1);

        Cursor cursor2 = database.query(MovieTrailers.TABLE_NAME,
                columns2,null,null,null,null,null);

        if(cursor2.moveToFirst()){
            assertEquals("126514",cursor2.getString(0));
            assertEquals("trailer",cursor2.getString(1));
            assertEquals("rtsdJSifjjsjY-sfg",cursor2.getString(2));
        }

         /*
        * Insert values into  REVIEWS and check the
        * inserted values whether they are correct or not
        *
        * */
        String[] columns3 = {
                MovieReviews.COLUMN_MOVIE_ID,
                MovieReviews.COLUMN_AUTHOR,
                MovieReviews.COLUMN_REVIEW_CONTENT
        };

        ContentValues values3 = new ContentValues();
        values3.put(MovieReviews.COLUMN_MOVIE_ID,"126514");
        values3.put(MovieReviews.COLUMN_AUTHOR,"ramana");
        values3.put(MovieReviews.COLUMN_REVIEW_CONTENT,"So good");

        row_id = database.insert(MovieReviews.TABLE_NAME,null,values3);
        assertTrue(row_id != -1);

        Cursor cursor3 = database.query(MovieReviews.TABLE_NAME,
                columns3,null,null,null,null,null);

        if(cursor3.moveToFirst()){
            assertEquals("126514",cursor3.getString(0));
            assertEquals("ramana",cursor3.getString(1));
            assertEquals("So good",cursor3.getString(2));
        }
    }

    public void testdelete(){

        SQLiteDatabase database = new MovieDBHelper(mContext).getWritableDatabase();


        database.delete(MovieEntry.TABLE_NAME,null,null);

    }


}