package com.example.ramana.moviedb;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
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

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailFragment extends Fragment {

    public static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    private ShareActionProvider mShareActionProvider;
    private Boolean mIsFavourite = false;

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_detail, menu);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.getItem(0));

        if(DetailsHolder.trailers.getChildCount()>0)
            mShareActionProvider.setShareIntent(sharetrailer("Adfadf afd af adfefdf adf"));
//        else
//            Log.v(LOG_TAG," no trailers yet");
    }

    public Intent sharetrailer(String shareUrl){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,shareUrl);

        return shareIntent;
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



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        DetailsHolder.title = (TextView)rootView.findViewById(R.id.text_movie_title);
        DetailsHolder.poster = (ImageView)rootView.findViewById(R.id.movie_poster);
        DetailsHolder.release_date = (TextView)rootView.findViewById(R.id.release_date);
        DetailsHolder.rating = (TextView)rootView.findViewById(R.id.text_rating);
        DetailsHolder.overView = (TextView)rootView.findViewById(R.id.movie_overview);
        DetailsHolder.trailers = (LinearLayout)rootView.findViewById(R.id.layout_trailers);
        DetailsHolder.reviews = (LinearLayout)rootView.findViewById(R.id.layout_reviews);


        final ImageView poster = (ImageView) rootView.findViewById(R.id.movie_poster);

        Bundle extras = getActivity().getIntent().getExtras();
        final Long id = extras.getLong("movie_id");
        String image_path = extras.getString("image_path");
        Picasso.with(getActivity())
                .load(image_path)
                .placeholder(R.drawable.loading)
                .fit()
                .into(poster);
        // Fetch movie details like rating overview etc.,.
        new FetchMovieDetails(getActivity(), rootView).execute(id);

        CheckBox checkBox = (CheckBox)rootView.findViewById(R.id.fav_movie_checkbox);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean is_fav = preferences.getBoolean(Long.toString(id),false);
        if(is_fav)
            checkBox.setChecked(is_fav);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(buttonView.isChecked()){
                        Log.v("fragm", "Checked");
                        addMovietoFavourites(id);
                    }
                    else{
                        Log.v("fragm", "Un Checked");
                        int no_of_mov_entry_deleted = getActivity().getContentResolver()
                                .delete(MovieEntry.CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build(),
                                        null,null);
                        if(no_of_mov_entry_deleted != 0)
                            Toast.makeText(getActivity(),"movie has removed from favourites",Toast.LENGTH_SHORT).show();
                            Log.v(LOG_TAG,"movie has removed from favourites");
                        // removing entry from preference manager
                            PreferenceManager.getDefaultSharedPreferences(getActivity())
                                    .edit().remove(Long.toString(id)).apply();
                    }
            }
        });

        return rootView;
    }

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

    public class AddToFavourites extends AsyncTask<ContentValues,Void,String>{

        private Context mContext;
        public AddToFavourites(Context context){
            this.mContext = context;
        }
        @Override
        protected String doInBackground(ContentValues... params) {

//            Log.v(LOG_TAG, params[0].getAsString(MovieEntry.COLUMN_MOVIE_NAME));
//            Log.v(LOG_TAG, params[1].getAsString(MovieDetails.COLUMN_MOVIE_RATING));
            Uri retUri = getActivity().getContentResolver().insert(MovieEntry.CONTENT_URI, params[0]);
            Long row_id = MovieEntry.parseidfromuri(retUri);
            Log.v(LOG_TAG, "Entry row has id: " + row_id);
            if(row_id == -1){
                return null;
            }

            Cursor cursor = getActivity().getContentResolver()
                    .query(MovieEntry.CONTENT_URI,
                            new String[]{MovieEntry.COLUMN_MOVIE_ID},null,null,null);
            Log.v(LOG_TAG,"No of rows in entry table are:" + cursor.getCount());

            retUri = getActivity().getContentResolver().insert(MovieDetails.CONTENT_URI, params[1]);
            row_id = MovieEntry.parseidfromuri(retUri);
            Log.v(LOG_TAG,"Details row has id: " + row_id);
            if(row_id == -1){
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            String movie_id = Long.toString(getActivity().getIntent().getLongExtra("movie_id", 0));

            // Content values for trailers
            int count = DetailsHolder.trailers.getChildCount();
            Log.v(LOG_TAG,"there are "+ count + " trailers");
            final ContentValues[] trailers = new ContentValues[count];
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
                    Log.v(LOG_TAG,"No of trailers inserted : " + no_of_rows);
                }
            }).start();

            Cursor trailersCursor = getActivity().getContentResolver()
                    .query(MovieTrailers.CONTENT_URI,null,null,null,null);
            Log.v(LOG_TAG,"No of rows in trailers table are : "+trailersCursor.getCount());
            trailersCursor.close();
            //.....................................................//
            // COntent Values for reviews
            count = DetailsHolder.reviews.getChildCount();
            Log.v(LOG_TAG,"There are "+count+" reviews for this movie");
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
            Cursor reviewCursor = getActivity().getContentResolver()
                    .query(MovieReviews.CONTENT_URI, null, null, null, null);
            Log.v(LOG_TAG, "Total no of rows in reviews table are: "
                    + reviewCursor.getCount());
            reviewCursor.close();

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            preferences.edit().putBoolean(movie_id,true).apply();
            if(preferences.contains(movie_id) && preferences.getBoolean(movie_id,false)){
                Toast.makeText(getActivity(),"Movie has added to favourites",Toast.LENGTH_SHORT).show();
                Log.v(LOG_TAG,"Movie added to Fav");
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

    }

    public class FetchMovieDetails extends AsyncTask<Long, Void, String[]> {

            private Context mContext;
            private View mrootView;

            LinearLayout trailers;

            public FetchMovieDetails(Context ctx, View root) {
                this.mContext = ctx;
                this.mrootView = root;
            }

            @Override
            protected String[] doInBackground(Long... params) {

                final String BASE_URL = "http://api.themoviedb.org/3/movie";
                final String API_KEY = "api_key";

                String[] resultString = new String[3];
                HttpURLConnection httpURLConnection = null;
                URL url;
                BufferedReader reader = null;
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
                } // End of try---catch---finally block

                return resultString;
            }//End of DoInBackgoung Method

            @Override
            protected void onPostExecute(String[] result) {
                try {
                    ParseMovieDetailsFromJsonString(result[0]);
                    parseJsonForTrailers(result[1]);
                    parseReviewsFromJson(result[2]);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.v(LOG_TAG, "Error While Parsing Json Strings");
                }

            }

            ///////////////////////////////////////////////////
            ////////////////  Helper Method  /////////////////
            /////////////////////////////////////////////////
            public void ParseMovieDetailsFromJsonString(String inpString)
                    throws JSONException {

                Log.v(LOG_TAG,"details are"+inpString);

                final String IMAGE_URI = "http://image.tmdb.org/t/p/w500";

                JSONObject root = new JSONObject(inpString);
                String overView = root.getString("overview");
                String movie_title = root.getString("original_title");
                String release_year = root.getString("release_date").split("-")[0];
                String rating = root.getString("vote_average") + "/" + Integer.toString(10);
                String poster_path = root.getString("poster_path");

//                ((TextView) mrootView.findViewById(R.id.text_movie_title)).setText(title);
//                LinearLayout descript_layout = (LinearLayout) mrootView.findViewById(R.id.layout_movie_info);
//                ((TextView) descript_layout.findViewById(R.id.release_date)).setText(release_year);
//                ((TextView) descript_layout.findViewById(R.id.text_rating)).setText(rating);
//                ((TextView) mrootView.findViewById(R.id.movie_overview)).setText(overView);

                DetailsHolder.title.setText(movie_title);
                DetailsHolder.release_date.setText(release_year);
                DetailsHolder.rating.setText(rating);
                DetailsHolder.overView.setText(overView);
            }

             // Helper method to fetch trailer url from Json String
             private void parseJsonForTrailers(String json) throws JSONException {

                 Log.v(LOG_TAG,"trailers :"+json);

            JSONObject root = new JSONObject(json);
            JSONArray results = root.getJSONArray("results");
            View trailer;
            String key;
            String name;
            LinearLayout trailers = DetailsHolder.trailers;

            for (int i = 0; i < results.length(); i++) {
                //Fetch youtube link and attach to the parent

                key = results.getJSONObject(i).getString("key");

                name = results.getJSONObject(i).getString("name");

                Log.v(LOG_TAG,"name:"+ name);

                trailer = LayoutInflater.from(mContext)
                        .inflate(R.layout.list_item_trailer, trailers,false);
                ((TextView)trailer.findViewById(R.id.list_item_trailer_number)).setText(name);
                trailer.setTag(key);
                trailer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://www.youtube.com/watch?v=" + v.getTag()));
                        if (i.resolveActivity(mContext.getPackageManager()) != null) {
                            startActivity(i);
                        }
                    }
                });

                trailers.addView(trailer);
            }
                 String shareUrl = (String) DetailsHolder.trailers.getChildAt(0).getTag();
                 if(mShareActionProvider != null)
                     mShareActionProvider.setShareIntent(sharetrailer("http://www.youtube.com/watch?v=" + shareUrl));
        }

            // Parse reviews from json
            public void parseReviewsFromJson(String response) throws JSONException{

                Log.v(LOG_TAG,"reviews :"+ response);

                JSONObject obj = new JSONObject(response);
                JSONArray results = obj.getJSONArray("results");

                LinearLayout layout_reviews = DetailsHolder.reviews;

                String content;
                String author;
                for(int i=0;i<results.length();i++){
                    JSONObject reviewObject = results.getJSONObject(i);
                    author = reviewObject.getString("author");
                    content = reviewObject.getString("content");

                    View review = LayoutInflater.from(mContext)
                            .inflate(R.layout.list_item_movie_review,layout_reviews,false);
                    ((TextView)review.findViewById(R.id.list_item_movie_review_author)).setText(author);
                    ((TextView)review.findViewById(R.id.list_item_movie_review_content)).setText(content);
                    layout_reviews.addView(review);

                }
                if(results.length() == 0){
                    TextView tv = new TextView(mContext);
                    tv.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    tv.setText("No reviews for this movie");

                    layout_reviews.addView(tv);
                }

            }// Parse Reviews from json

    } // End of FetchMovieDetailsTask

}




