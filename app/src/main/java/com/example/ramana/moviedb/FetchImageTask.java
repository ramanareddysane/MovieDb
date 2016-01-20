package com.example.ramana.moviedb;

////////////////////////////////////////////////////////////////////
///////////////////Fetch Images from Movie Database/////////////////
////////////////////////////////////////////////////////////////////

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchImageTask extends AsyncTask<String, Void, String> {

    private ProgressDialog progressDialog;
    private final String LOG_TAG = MovieListFragment.class.getSimpleName();
    private AsyncTaskCompleteListener listener;
    private Context context;

    public FetchImageTask(Context ctx,AsyncTaskCompleteListener listener){
        this.context = ctx;
        this.listener = listener;
        progressDialog = new ProgressDialog(ctx);
    }

    @Override
    protected void onPreExecute() {
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Getting movies from internet");
        progressDialog.show();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        String resultString = null;
        BufferedReader reader = null;

        final String BASE_URL = "https://api.themoviedb.org/3/discover/movie";
        //final String PATH = "discover/movie";
        final String API_KEY = "api_key";
        final String VALUE_API_KEY = BuildConfig.API_KEY;
        final String SORT_BY = "sort_by";
        final String VALUE_SORT_BY = params[0];
        final String PAGE_NO = "page";
        final String VALUE_PAGE_NO = params[1];


        Uri buildUri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(SORT_BY,VALUE_SORT_BY)
                .appendQueryParameter(API_KEY,VALUE_API_KEY)
                .appendQueryParameter(PAGE_NO,VALUE_PAGE_NO)
                .build();
        Log.v(LOG_TAG,buildUri.toString());

        try {
            URL url = new URL(buildUri.toString());
            urlConnection = ( HttpURLConnection )url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuffer buffer = new StringBuffer();

            while((line = reader.readLine())!=null){
                buffer.append(line + "\n");
            }

            if(buffer.length() != 0){
                resultString = buffer.toString();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Error while Connecting to url");
        }finally {
            // We have to close both urlconnection ie.,HttpUrlConnection Object and reader object
            if(urlConnection != null)
                urlConnection.disconnect();
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error while cosing stram object");
                }
            }
        } // End of finally block

        return resultString ;
    } // End of Do In Backlground

    @Override
    protected void onPostExecute(String result) {
        if(progressDialog != null) {
            progressDialog.setMessage("Done loading movies");
            progressDialog.hide();
        }
        if (result != null){
            listener.onTaskComplete(result);
        }
    }

}// End of Fetch Image Task

