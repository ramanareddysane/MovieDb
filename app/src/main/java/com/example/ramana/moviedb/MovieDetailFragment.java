package com.example.ramana.moviedb;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ramana.moviedb.data.MovieContract.MovieDetails;
import com.example.ramana.moviedb.data.MovieContract.MovieEntry;
import com.example.ramana.moviedb.data.MovieContract.MovieReviews;
import com.example.ramana.moviedb.data.MovieContract.MovieTrailers;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MovieDetailFragment extends Fragment {

    public static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    public static ShareActionProvider mShareActionProvider;
    public static MovieTotalDetails mTotal_movie_details;
    public FetchMovieDetails fetchMovieDetails = null;

    public MovieDetailFragment() {

    }

    public static class DetailsHolder{
        static TextView title;
        static ImageView poster;
        static TextView release_date;
        static TextView rating;
        static TextView overView;
        static LinearLayout trailers;
        static LinearLayout reviews;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Updating details incase it has no Image in parcellable object
        ContentValues updateDetails = mTotal_movie_details.getDetails();
        Bitmap b = ((BitmapDrawable)DetailsHolder.poster.getDrawable()).getBitmap();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] bytes = bos.toByteArray();
        updateDetails.put(MovieEntry.COLUMN_MOVIE_IMAGE,bytes);
        mTotal_movie_details.setDetails(updateDetails);

        outState.putParcelable("movie_info", mTotal_movie_details);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        setViewHolder(rootView);

        Bundle extras = getActivity().getIntent().getExtras();
        final Long id = extras.getLong("movie_id");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean is_fav = preferences.getBoolean(Long.toString(id), false);

        if(savedInstanceState == null){
            mTotal_movie_details = new MovieTotalDetails();
            if(!is_fav) {
                if(!fetchMovieFromDatabase(Long.toString(id)));
                Toast.makeText(getActivity(),"Somthing gone wrong in " +
                        "fetching movie details from database",Toast.LENGTH_SHORT).show();
            }else {
                String image_path = getActivity().getIntent().getExtras().getString("image_path");
                Picasso.with(getActivity())
                        .load(image_path)
                        .placeholder(R.drawable.loading)
                        .fit()
                        .into(DetailsHolder.poster);
                fetchMovieDetails =
                        (FetchMovieDetails) new FetchMovieDetails(getActivity(),rootView).execute(id);
            }
        }else {
            // Get back from parcelleble object
            mTotal_movie_details = savedInstanceState.getParcelable("movie_info");
            setMovieDetails(mTotal_movie_details.getDetails());
            setMovieTrailers(mTotal_movie_details.getTrailers());
            setMovieReviews(mTotal_movie_details.getReviews());
        }

        // Set the state of check box if it is favourite
        CheckBox checkBox = (CheckBox)rootView.findViewById(R.id.fav_movie_checkbox);
        if(is_fav)
            checkBox.setChecked(is_fav);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    addMovietoFavourites(id);
                } else {
                    int no_of_movie_entries_deleted = getActivity().getContentResolver()
                            .delete(MovieEntry.CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build(),
                                    null, null);
                    if (no_of_movie_entries_deleted != 0)
                        Toast.makeText(getActivity(), "movie has removed from favourites", Toast.LENGTH_SHORT).show();
                    // removing entry from preference manager
                    PreferenceManager.getDefaultSharedPreferences(getActivity())
                            .edit().remove(Long.toString(id)).apply();
                }
            }
        }); // End of Check box implementation

        return rootView;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_share:
                Log.v(LOG_TAG,"share Clicked");
                break;
            default:{
                Log.v(LOG_TAG," Nothing  Clicked");
                return super.onOptionsItemSelected(item);
            }
        }
        return super.onOptionsItemSelected(item);
    }

        // Method to initialise Movies View Holder
    public void setViewHolder(View rootView){
       DetailsHolder.title = (TextView)rootView.findViewById(R.id.text_movie_title);
       DetailsHolder.poster = (ImageView)rootView.findViewById(R.id.movie_poster);
       DetailsHolder.release_date = (TextView)rootView.findViewById(R.id.release_date);
       DetailsHolder.rating = (TextView)rootView.findViewById(R.id.text_rating);
       DetailsHolder.overView = (TextView)rootView.findViewById(R.id.movie_overview);
       DetailsHolder.trailers = (LinearLayout)rootView.findViewById(R.id.layout_trailers);
       DetailsHolder.reviews = (LinearLayout)rootView.findViewById(R.id.layout_reviews);
   }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_detail, menu);
        mShareActionProvider = new ShareActionProvider(getActivity());
        MenuItemCompat.setActionProvider(menu.getItem(0), mShareActionProvider);
    }

        //Method to Create intent for ShareActionProvider
    public Intent sharetrailer(String shareUrl){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);

        return shareIntent;
    }

        // Method to Fetch Entire Movie Details from Database.
    public Boolean fetchMovieFromDatabase(String movie_id){
        //Set movie details, but not trailers and reviews
        String[] projectionsforDetails = {
                MovieEntry.TABLE_NAME+"."+MovieEntry.COLUMN_MOVIE_ID,
                MovieEntry.COLUMN_MOVIE_NAME,
                MovieEntry.COLUMN_MOVIE_IMAGE,
                MovieDetails.COLUMN_MOVIE_RELEASEDATE,
                MovieDetails.COLUMN_MOVIE_RATING,
                MovieDetails.COLUMN_MOVIE_OVERVIEW
        };
        Cursor detailCursor = getActivity().getContentResolver()
                .query(MovieDetails.CONTENT_URI.buildUpon().appendPath(movie_id).build(),
                        projectionsforDetails, null, null, null);
        if( detailCursor!=null && detailCursor.moveToFirst()){
            ContentValues details = new ContentValues();
            // Set movie title
            DetailsHolder.title.setText(detailCursor.getString(1));
            details.put(MovieEntry.COLUMN_MOVIE_NAME,detailCursor.getString(1));
             //Set movie poster image
            byte[] bytes = detailCursor.getBlob(2);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            DetailsHolder.poster.setImageBitmap(bitmap);
            details.put(MovieEntry.COLUMN_MOVIE_IMAGE, detailCursor.getBlob(2));
            // Set release year
            DetailsHolder.release_date.setText(detailCursor.getString(3));
            details.put(MovieDetails.COLUMN_MOVIE_RELEASEDATE, detailCursor.getString(3));
            //Set rating
            DetailsHolder.rating.setText(detailCursor.getString(4));
            details.put(MovieDetails.COLUMN_MOVIE_RATING, detailCursor.getString(4));
            // Set over view
            DetailsHolder.overView.setText(detailCursor.getString(5));
            details.put(MovieDetails.COLUMN_MOVIE_OVERVIEW, detailCursor.getString(5));
                // Send the values to Parcellable  object.
            mTotal_movie_details.setDetails(details);
           }

        if(detailCursor != null)
            detailCursor.close();
        else
            return false;

        // Set Trailers for the movie if any
        String[] trailerColumns = {
                MovieTrailers.COLUMN_TRAILER_NAME,
                MovieTrailers.COLUMN_TRAILER_KEYURL
        };
        Cursor trailerCursor = getActivity().getContentResolver()
                .query(MovieTrailers.CONTENT_URI.buildUpon().appendPath(movie_id).build(),
                        trailerColumns,
                        MovieTrailers.COLUMN_MOVIE_ID + " = ? ",
                        new String[]{movie_id},
                        null);
        String key;
        String name;
        if(trailerCursor!=null && trailerCursor.moveToFirst()){
            Map trailers = new HashMap();
            do{
                Log.v(LOG_TAG, "Trailer name is: " + trailerCursor.getString(0));
                name = trailerCursor.getString(0);
                key = trailerCursor.getString(1);
                trailers.put(key, name);
            }while (trailerCursor.moveToNext());
             // Set trailers to UI if any
            setMovieTrailers(trailers);
        } // End of adding all trailers
        else
            DetailsHolder.trailers.addView(getEmptyTextView(getString(R.string.no_trailers)));

        if (trailerCursor != null)
            trailerCursor.close();
        else
            return false;

        // Set reviews about movie if any
        String[] reviewColumns = {
                MovieReviews.COLUMN_AUTHOR,
                MovieReviews.COLUMN_REVIEW_CONTENT
        };
        Cursor reviewsCursor = getActivity().getContentResolver()
                .query(MovieReviews.CONTENT_URI.buildUpon().appendPath(movie_id).build(),
                        reviewColumns,
                        MovieEntry.COLUMN_MOVIE_ID+" = ? ",
                        new String[]{movie_id},
                        null);
        String author;
        String content;
        Map reviews = new HashMap();
        if(reviewsCursor.moveToFirst()){
            do{
                author = reviewsCursor.getString(0);
                content = reviewsCursor.getString(1);
                reviews.put(author,content);

            }while (reviewsCursor.moveToNext());
             // Set reviews to UI
            setMovieReviews(reviews);
        }else {
            DetailsHolder.reviews.addView(getEmptyTextView(getString(R.string.no_reviews)));
        }
        if (reviewsCursor != null)
            reviewsCursor.close();
        else
            return false;
        // Return true if nothing un ususal happens
        return true;
    }

        // Method to add movie to favourites
    public void addMovietoFavourites(Long id){
        Bitmap posterBitmap = ((BitmapDrawable) DetailsHolder.poster .getDrawable())
                .getBitmap();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        posterBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] bytes = bos.toByteArray();

         //Content values for movie Entry
        ContentValues entry = new ContentValues();
        entry.put(MovieEntry.COLUMN_MOVIE_ID, id);
        entry.put(MovieEntry.COLUMN_MOVIE_NAME, (String) DetailsHolder.title.getText());
        entry.put(MovieEntry.COLUMN_MOVIE_IMAGE, bytes);

         // Content values for movie details.
        ContentValues details = new ContentValues();
        details.put(MovieDetails.COLUMN_MOVIE_ID,id);
        details.put(MovieDetails.COLUMN_MOVIE_ID,id);
        details.put(MovieDetails.COLUMN_MOVIE_RELEASEDATE,
                (String)DetailsHolder.release_date.getText());
        details.put(MovieDetails.COLUMN_MOVIE_RATING,
                (String) DetailsHolder.rating.getText());
        details.put(MovieDetails.COLUMN_MOVIE_OVERVIEW,
                (String) DetailsHolder.overView.getText());

        ContentValues[] values = {entry, details};
        new AddToFavourites(getActivity()).execute(values);
    }

        // Async Task for adding movies to Database as favourites
    public class AddToFavourites extends AsyncTask<ContentValues,Void,String>{

        private Context mContext;
        public AddToFavourites(Context context){
            this.mContext = context;
        }

        @Override
        protected String doInBackground(ContentValues... params) {
              // Add Movie Entry to the database
            Uri retUri = getActivity().getContentResolver().insert(MovieEntry.CONTENT_URI, params[0]);
            Long row_id = MovieEntry.parseidfromuri(retUri);
            if(row_id == -1){
                return null;
            }
                // Add movie Details to the database
            retUri = getActivity().getContentResolver().insert(MovieDetails.CONTENT_URI, params[1]);
            row_id = MovieEntry.parseidfromuri(retUri);
            if(row_id == -1){
                return null;
            }
            return null;
        } // End of Do In background

        @Override
        protected void onPostExecute(String result) {
            String movie_id = Long.toString(getActivity().getIntent().getLongExtra("movie_id", 0));

            /*
             *  Get Content values from trailers.
             *  First get no of child for trailers layout
             *  to determine whether if any trailers are present or not
             */
            int count = DetailsHolder.trailers.getChildCount();

             final ContentValues[] trailers = new ContentValues[count];
            if(count == 1 && (DetailsHolder.trailers.getChildAt(0) instanceof TextView)){
                    // Child is empty text view
                Toast.makeText(getActivity(),"No trailers",Toast.LENGTH_SHORT).show();
                Log.v(LOG_TAG,"No trailers for this movie");
            }else {
                    // Child has trailers and fetch them
                for(int i=0;i<count;i++){
                    View trailerView = DetailsHolder.trailers.getChildAt(i);
                    String name = (String) ((TextView)trailerView
                            .findViewById(R.id.list_item_trailer_number)).getText();
                    ContentValues trailer = new ContentValues();
                    trailer.put(MovieTrailers.COLUMN_MOVIE_ID, movie_id);
                    trailer.put(MovieTrailers.COLUMN_TRAILER_NAME, name);
                    trailer.put(MovieTrailers.COLUMN_TRAILER_KEYURL, (String) trailerView.getTag());

                    trailers[i] = trailer;
                }
                ////....Thread for storing trailers info in database.
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int no_of_rows = getActivity().getContentResolver()
                                .bulkInsert(MovieTrailers.CONTENT_URI,trailers);

                    }
                }).start();
            }

            /*
             *  Get Content values from Reviews if any.
             *  First get no of child for Reviews layout
             *  to determine whether if any trailers are present or not
             */
            count = DetailsHolder.reviews.getChildCount();
            if(count == 1 && (DetailsHolder.reviews.getChildAt(0) instanceof TextView)){
                    // Child is empty text view
//                Log.v(LOG_TAG,"No reviews for this movie");
            }else {
                    // We have to fetch reviews from layout
                final ContentValues[] reviews = new ContentValues[count];
                for(int i=0;i<count;i++){
                    ContentValues review = new ContentValues();
                    View reviewView = DetailsHolder.reviews.getChildAt(i);
                    String author = (String) ((TextView)reviewView
                            .findViewById(R.id.list_item_movie_review_author)).getText();
                    String review_content = (String)((TextView)reviewView
                            .findViewById(R.id.list_item_movie_review_content)).getText();
                    review.put(MovieReviews.COLUMN_MOVIE_ID, movie_id);
                    review.put(MovieReviews.COLUMN_AUTHOR, author);
                    review.put(MovieReviews.COLUMN_REVIEW_CONTENT, review_content);
                    reviews[i] = review;
                }
                // Thread for storing reviews in database
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int no_of_reviews = getActivity().getContentResolver()
                                .bulkInsert(MovieReviews.CONTENT_URI,reviews);
                        Log.v(LOG_TAG,"No of reviews inserted: "+ no_of_reviews);
                    }
                }).start();
            }

                // Enter the movie to Shared Preferences favourites list
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            preferences.edit().putBoolean(movie_id,true).apply();
            if(preferences.contains(movie_id) && preferences.getBoolean(movie_id,false)){
                Toast.makeText(getActivity(),"Movie has added to favourites",Toast.LENGTH_SHORT).show();
                Log.v(LOG_TAG,"Movie added to Fav");
            }
        } // End of onPostExecute
    }

        // Async Task for Fetch Movies from theMovieDb API
    public class FetchMovieDetails extends AsyncTask<Long, Void, String[]> {
        private Context mContext;
        private HttpURLConnection httpURLConnection = null;
        private BufferedReader reader = null;
        private View mrootView;

        public FetchMovieDetails(Context ctx,View rootView) {
                this.mContext = ctx;
                this.mrootView = rootView;
        }

        @Override
        protected String[] doInBackground(Long... params) {

            final String BASE_URL = "http://api.themoviedb.org/3/movie";
            final String API_KEY = "api_key";

            String[] resultString = new String[3];
            URL url;
            Uri build_uri;

            try {
                ///// code to fetch movie details
                build_uri = Uri.parse(BASE_URL)
                        .buildUpon()
                        .appendPath(Long.toString(params[0]))
                        .appendQueryParameter(API_KEY, BuildConfig.API_KEY)
                        .build();
                url = new URL(build_uri.toString());
                Log.v(LOG_TAG, url.toString());

                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                StringBuffer buffer = new StringBuffer();

                if(isCancelled()){
                    Log.v(LOG_TAG,"Cancelled before fetching details");
                    return null;
                }
                InputStream inputStream = httpURLConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0)
                    return null;
                resultString[0] = buffer.toString();


                ///// code to fetch movie trailers
                 build_uri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath(Long.toString(params[0]))
                        .appendPath("videos")
                        .appendQueryParameter(API_KEY, BuildConfig.API_KEY)
                        .build();
                url = new URL(build_uri.toString());
                Log.v(LOG_TAG, url.toString());

                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                if(isCancelled()){
                    Log.v(LOG_TAG,"Cancelled before fetching trailers");
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

                buffer = new StringBuffer();
                if (reader == null)
                    return null;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "/n");
                }
                resultString[1] = buffer.toString();


                ////////// Fetch movie reviews
                Uri build = Uri.parse(BASE_URL).buildUpon()
                        .appendPath(Long.toString(params[0]))
                        .appendPath("reviews")
                        .appendQueryParameter("api_key",BuildConfig.API_KEY)
                        .build();
                url = new URL(build.toString());

                httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                if(isCancelled()){
                    Log.v(LOG_TAG,"Cancelled before fetching reviews");
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                if(reader == null)
                    return null;
               buffer = new StringBuffer();

                while ((line = reader.readLine())!= null)
                    buffer.append(line);

                resultString[2] = buffer.toString();

            } catch (java.io.IOException e) {
                e.printStackTrace();
            } finally {
                cleanUpWork();
            } // End of try---catch---finally block

            return resultString;
        }//End of DoInBackgoung Method

        @Override
        protected void onCancelled() {
            cleanUpWork();
            super.onCancelled();
        }

        private void cleanUpWork(){
            if (httpURLConnection != null)
                httpURLConnection.disconnect();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.v(LOG_TAG, "Error while closing a stram");
                }
            }
        }

        @Override
        protected void onPostExecute(String[] result) {
            try {
                String image_path = getActivity().getIntent().getExtras().getString("image_path");
                if(image_path != null){
                    Picasso.with(mContext)
                            .load(image_path)
                            .fit()
                            .into(DetailsHolder.poster);
                }
                setMovieDetails(ParseMovieDetailsFromJsonString(result[0]));
                setMovieTrailers(parseJsonForTrailers(result[1]));;
                setMovieReviews(parseReviewsFromJson(result[2]));;
            } catch (JSONException e) {
                e.printStackTrace();
                Log.v(LOG_TAG, "Error While Parsing Json Strings");
            }

        }

        //..............................................//
        ////////////////  Helper Method  /////////////////
        private ContentValues ParseMovieDetailsFromJsonString(String inpString)
                throws JSONException {

            Log.v(LOG_TAG,"details are"+inpString);
            ContentValues details = new ContentValues();
            if(inpString == null)
                return null;

            JSONObject root = new JSONObject(inpString);
            String overView = root.getString("overview");
            String movie_title = root.getString("original_title");
            String release_year = root.getString("release_date").split("-")[0];
            String rating = root.getString("vote_average") + "/" + Integer.toString(10);
            String path = root.getString("poster_path");

             //Set the appropriate details to Content values.
            details.put(MovieEntry.COLUMN_MOVIE_NAME, movie_title);
            Log.v(LOG_TAG, "TITLE: " + movie_title);
            details.put(MovieDetails.COLUMN_MOVIE_RELEASEDATE, release_year);

            details.put(MovieDetails.COLUMN_MOVIE_RATING, rating);
            details.put(MovieDetails.COLUMN_MOVIE_OVERVIEW, overView);

//                ImageView im = (ImageView) mrootView.findViewById(R.id.movie_poster);
//                Bitmap b = ((BitmapDrawable) im.getDrawable()).getBitmap();
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                b.compress(Bitmap.CompressFormat.PNG, 100, bos);
//                byte[] bytes = bos.toByteArray();
//                details.put(MovieEntry.COLUMN_MOVIE_IMAGE,bytes);

            // Send values to Parcellable object before returning values
            mTotal_movie_details.setDetails(details);
            return details;
        }

             // Helper method to fetch trailer url from Json String
        private HashMap parseJsonForTrailers(String json) throws JSONException {

            JSONObject root = new JSONObject(json);
            JSONArray results = root.getJSONArray("results");
            String key;
            String name;
            if(results.length() == 0){
                DetailsHolder.trailers.addView(getEmptyTextView("No reviews for this movie"));
                return null;
            }
            HashMap movie_trailers = new HashMap(results.length());

            for (int i = 0; i < results.length(); i++) {
                    //Fetch youtube link and attach to the parent
                key = results.getJSONObject(i).getString("key");
                name = results.getJSONObject(i).getString("name");
                movie_trailers.put(key,name);
            }
            return movie_trailers;
        }

             // Parse reviews from json
        private HashMap parseReviewsFromJson(String response) throws JSONException{
            JSONObject obj = new JSONObject(response);
            JSONArray results = obj.getJSONArray("results");

            LinearLayout layout_reviews = DetailsHolder.reviews;

            String content;
            String author;
            if(results.length() == 0){
                layout_reviews.addView(getEmptyTextView("No reviews for this movie"));
                return null;
            }
            HashMap reviews_hashMap = new HashMap();
            for(int i=0;i<results.length();i++){
                JSONObject reviewObject = results.getJSONObject(i);
                author = reviewObject.getString("author");
                content = reviewObject.getString("content");

                reviews_hashMap.put(author,content);
            }
            return reviews_hashMap;
        }// Parse Reviews from json

    } // End of FetchMovieDetailsTask

        // Method to set Details to respective views in layout.
    public void setMovieDetails(ContentValues details){
        if(details != null){
                // that means the movie is not in favourites.
                // So we need to fetch the information from MovieDb API.
            DetailsHolder.title.setText(details.getAsString(MovieEntry.COLUMN_MOVIE_NAME));
                // Extract the byte array and convert into Bitmap
            byte[] bytes = details.getAsByteArray(MovieEntry.COLUMN_MOVIE_IMAGE);
            if(bytes != null) {
                Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                DetailsHolder.poster.setImageBitmap(b);
            }
            DetailsHolder.release_date.setText(details.getAsString(MovieDetails.COLUMN_MOVIE_RELEASEDATE));
            DetailsHolder.rating.setText(details.getAsString(MovieDetails.COLUMN_MOVIE_RATING));
            DetailsHolder.overView.setText(details.getAsString(MovieDetails.COLUMN_MOVIE_OVERVIEW));
        }
        else
            Log.v(LOG_TAG,"No details for this movie");

    } // End of setMovie Details method

        // Method to set trailers to respective views.
    public void setMovieTrailers(Map trailers){
        if(trailers == null || trailers.size()==0){
           return;
        }
        // Send trailers to parcellable class
        mTotal_movie_details.setTrailers(trailers);

        LinearLayout trailer;
        String key;
        String name;

        Iterator i = trailers.entrySet().iterator();
        while (i.hasNext()){
            Map.Entry me = (Map.Entry) i.next();
            key = (String) me.getKey();
            name = (String) me.getValue();

            trailer = (LinearLayout) LayoutInflater.from(getActivity())
                    .inflate(R.layout.list_item_trailer, DetailsHolder.trailers, false);
            ((TextView)trailer.findViewById(R.id.list_item_trailer_number)).setText(name);
            trailer.setTag(key);
            trailer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://www.youtube.com/watch?v=" + v.getTag()));
                    if (i.resolveActivity(getActivity().getPackageManager()) != null)
                        startActivity(i);
                }
            });

            DetailsHolder.trailers.addView(trailer);
        }

        if(DetailsHolder.trailers.getChildCount() > 0){
            String shareUrl = (String) DetailsHolder.trailers.getChildAt(0).getTag();
            if(mShareActionProvider != null)
                mShareActionProvider.setShareIntent(sharetrailer("http://www.youtube.com/watch?v=" + shareUrl));
        }
    } //End of setMovie Trailers method

        // Methid to set reviews to respective views.
    public void setMovieReviews(Map reviews){
        if(reviews == null || reviews.size()==0)
            return;
        String author;
        String content;

        // Send trailers to parcellable class
        mTotal_movie_details.setReviews(reviews);

        Set reviewSet = reviews.entrySet();
        Iterator i = reviewSet.iterator();

        while (i.hasNext()){
            Map.Entry me = (Map.Entry) i.next();
            author = (String) me.getKey();
            content = (String) me.getValue();
            View review = LayoutInflater.from(getActivity())
                    .inflate(R.layout.list_item_movie_review,DetailsHolder.reviews,false);
            ((TextView)review.findViewById(R.id.list_item_movie_review_author)).setText(author);
            ((TextView)review.findViewById(R.id.list_item_movie_review_content)).setText(content);
            DetailsHolder.reviews.addView(review);
        }
    }

        //Method for create and return empty text view
    TextView getEmptyTextView(String text){
        TextView tv = new TextView(getActivity());
        tv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setText(text);
        return tv;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if( fetchMovieDetails != null){
            fetchMovieDetails.cancel(true);
            fetchMovieDetails = null;
//            Toast.makeText(getActivity(),"Cancelled",Toast.LENGTH_SHORT).show();
        }
//        Toast.makeText(getActivity(),"Detached",Toast.LENGTH_SHORT).show();
    }

}
