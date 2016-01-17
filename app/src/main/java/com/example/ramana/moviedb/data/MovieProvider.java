package com.example.ramana.moviedb.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.ramana.moviedb.data.MovieContract.MovieDetails;
import com.example.ramana.moviedb.data.MovieContract.MovieEntry;
import com.example.ramana.moviedb.data.MovieContract.MovieReviews;
import com.example.ramana.moviedb.data.MovieContract.MovieTrailers;

/**
 * Created by ramana on 9/1/16.
 */
public class MovieProvider extends ContentProvider {

    public static final int MOVIE_ENTRY = 100;
    public static final int Movie_ENTRY_WITH_ID=101;
    public static final int MOVIE_DETAILS = 200;
    public static final int MOVIE_DETAILS_WITH_ID = 201;
    public static final int MOVIE_TRAILERS = 300;
    public static final int MOVIE_TRAILERS_WITH_ID = 301;
    public static final int MOVIE_REVIEWS = 400;
    public static final int MOVIE_REVIEWS_WITH_ID = 401;


    private static final UriMatcher suriMatcher = buildUriMather();
    public static MovieDBHelper mOpenHelper;

    private static SQLiteQueryBuilder sqLiteQueryBuilder;

    static {
        sqLiteQueryBuilder = new SQLiteQueryBuilder();

        //This is inner join on movie entry and movie details
        //table that retrieves single row containing all info
        //about tables.

        // movies INNER JOIN details ON (movies.movie_id = details.movie_id)
        sqLiteQueryBuilder.setTables(MovieEntry.TABLE_NAME
                + " INNER JOIN " + MovieDetails.TABLE_NAME
                + " ON (" + MovieEntry.TABLE_NAME +"."+ MovieEntry.COLUMN_MOVIE_ID +
                        " = "+ MovieDetails.TABLE_NAME+"."+MovieDetails.COLUMN_MOVIE_ID
                + ")"
        );
    }

    //movies.movie_id =
    private static final String smovieDetailsSelection
            = MovieEntry.TABLE_NAME+"."+MovieDetails.COLUMN_MOVIE_ID +" = ?";

    private static Cursor getMovieDetailsWithMovieId(String movieId,String[] projections){

        return sqLiteQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projections,
                smovieDetailsSelection,
                new String[]{movieId},
                null,null,null);
    }


    private  static UriMatcher buildUriMather(){

        String authority = MovieContract.CONTENT_AUTHORITY;

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(authority, MovieContract.PATH_ENTRY , MOVIE_ENTRY);
        uriMatcher.addURI(authority, MovieContract.PATH_ENTRY + "/*", Movie_ENTRY_WITH_ID);
        uriMatcher.addURI(authority, MovieContract.PATH_DETAILS , MOVIE_DETAILS);
        uriMatcher.addURI(authority, MovieContract.PATH_DETAILS + "/*", MOVIE_DETAILS_WITH_ID);
        uriMatcher.addURI(authority, MovieContract.PATH_TRAILERS, MOVIE_TRAILERS);
        uriMatcher.addURI(authority, MovieContract.PATH_TRAILERS + "/*", MOVIE_TRAILERS_WITH_ID);
        uriMatcher.addURI(authority, MovieContract.PATH_REVIEWS , MOVIE_REVIEWS);
        uriMatcher.addURI(authority, MovieContract.PATH_REVIEWS + "/*", MOVIE_REVIEWS_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (suriMatcher.match(uri)){
            case MOVIE_ENTRY:{
                retCursor = mOpenHelper.getReadableDatabase().query(MovieContract.MovieEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case MOVIE_DETAILS_WITH_ID:{
                String movie_id = uri.getLastPathSegment();
                retCursor = getMovieDetailsWithMovieId(movie_id,projection);
                break;
            }
            case MOVIE_TRAILERS:{
                retCursor = mOpenHelper.getReadableDatabase()
                        .query(MovieTrailers.TABLE_NAME,
                                new String[]{MovieTrailers._ID}
                                ,null,null,null,null,null);
                break;
            }
            case MOVIE_TRAILERS_WITH_ID:{
                String movie_id = uri.getLastPathSegment();
                retCursor = mOpenHelper.getReadableDatabase().query(MovieTrailers.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case MOVIE_REVIEWS:{
                retCursor = mOpenHelper.getReadableDatabase()
                        .query(MovieReviews.TABLE_NAME,
                                new String[]{MovieReviews._ID},null,null,null,null,null);
            }
            case MOVIE_REVIEWS_WITH_ID:{
                String movie_id = uri.getLastPathSegment();
                retCursor = mOpenHelper.getReadableDatabase().query(MovieReviews.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("UnKnown Uri " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(),uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (suriMatcher.match(uri)){
            case MOVIE_ENTRY: {
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            }default:
                throw new UnsupportedOperationException("Unknown URI "+ uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri returi;
        switch (suriMatcher.match(uri)){
            case MOVIE_ENTRY: {
                long rowid  = mOpenHelper.getWritableDatabase().insert(MovieEntry.TABLE_NAME,null,values);
                returi = ContentUris.withAppendedId(uri,rowid);
                break;
            }
            case MOVIE_DETAILS:{
                long rowid = mOpenHelper.getWritableDatabase().insert(MovieDetails.TABLE_NAME,null,values);
                if(rowid != -1)
                returi = uri.buildUpon()
                        .appendPath(Long.toString(rowid)).build();
                else throw new SQLException("Error while inserting values into details");
                break;
            }case MOVIE_TRAILERS:{
                long rowid =  mOpenHelper.getWritableDatabase().insert(MovieTrailers.TABLE_NAME,null,values);
                if(rowid != -1)
                    returi = uri.buildUpon().
                            appendPath(values.getAsString(MovieTrailers.COLUMN_MOVIE_ID)).build();
                else
                    throw new SQLException("Error while inserting values to trailers");
                break;
            }
            case MOVIE_REVIEWS:{
                long rowId = mOpenHelper.getWritableDatabase().insert(MovieReviews.TABLE_NAME,null,values);
                if(rowId != -1)
                    returi = uri.buildUpon()
                            .appendPath(values.getAsString(MovieReviews.COLUMN_MOVIE_ID)).build();
                else
                    throw new SQLException("Error while inserting row into reviews table ");
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown Uri"+ uri);
        }
        return returi;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rows_effected = 0;
        switch (suriMatcher.match(uri)){
            case Movie_ENTRY_WITH_ID:{
                int rows = mOpenHelper.getWritableDatabase().delete(MovieEntry.TABLE_NAME,
                        MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                        new String[]{uri.getLastPathSegment()});
                if(rows != 0)
                    rows_effected ++;
                else
                    throw new SQLiteConstraintException("Error while deleting a movie  with id:"
                            + uri.getLastPathSegment());
                break;
            }
            default:
                throw new UnsupportedOperationException("Unkown uri "+ uri);
        }
        return rows_effected;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        switch (suriMatcher.match(uri)){
            case MOVIE_TRAILERS:{
                int retCount=0;
                SQLiteDatabase db =  mOpenHelper.getWritableDatabase();
                db.beginTransaction();
                try{
                    for(ContentValues value : values){
                        long retid = mOpenHelper.getWritableDatabase().insert(MovieTrailers.TABLE_NAME, null, value);
                        if(retid != -1)
                            retCount++;
                        else
                            throw new SQLException("Error inserting values to trailers");
                    }
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }
                return retCount;
            }
            case MOVIE_REVIEWS:{
                int retCount = 0;
                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                db.beginTransaction();
                try{
                    for(ContentValues value : values){
                        long retid = db.insert(MovieReviews.TABLE_NAME,null,value);
                        if(retid != -1)
                            retCount ++;
                        else throw new SQLException("Error while inserting rows into reviews table");
                    }
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }
                return retCount;
            }
            default:
                return super.bulkInsert(uri, values);
        } // end of switch case
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
