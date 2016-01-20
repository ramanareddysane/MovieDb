package com.example.ramana.moviedb;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.ramana.moviedb.data.MovieContract.MovieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MovieListFragment extends Fragment {

    final String LOG_TAG = MovieListFragment.class.getSimpleName();
    MovieAdapter movie_adapter;
    String order_by;
    private Boolean mFavourite = false;

    ProgressDialog progressDialog;

    List<MovieImage> images = new ArrayList<MovieImage>();

    public MovieListFragment() {
        MovieAdapter.setPage_nmber(1);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        progressDialog = new ProgressDialog(getActivity());
        order_by = getString(R.string.popularity);
        setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.image_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.most_popular:
                // Refresh list with popularity wise
                refresh_list(getString(R.string.popularity));
                return true;
            case R.id.high_rated:
                // Refresh list with highest movie rated first
                refresh_list(getString(R.string.rating));
                return true;
            case R.id.fav:
                Toast.makeText(getActivity(),"favourite",Toast.LENGTH_SHORT).show();
                mFavourite=true;
                new LoadFavourites().execute(mFavourite);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public class LoadFavourites extends AsyncTask<Boolean,Void,List<MovieImage>>{

        @Override
        protected void onPreExecute() {
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Getting movies from your favourite colection");
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected List<MovieImage> doInBackground(Boolean... params) {

            final String[] projection = new String[]{
                    MovieEntry.COLUMN_MOVIE_ID,
                    MovieEntry.COLUMN_MOVIE_IMAGE,
                    MovieEntry.COLUMN_MOVIE_NAME
            };
            List<MovieImage> MovieImageObjects;

            Cursor movies = getActivity().getContentResolver()
                    .query(MovieEntry.CONTENT_URI,
                            projection,null,null,null);
            if(movies == null)
                return null;
            MovieImageObjects = new ArrayList<MovieImage>(movies.getCount());
            byte[] bytes;
            Bitmap b;
            int movie_id;
            if(movies.moveToFirst()){
                do{
                    movie_id = Integer.parseInt(movies.getString(0));
                    bytes = movies.getBlob(1);
                    b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    MovieImageObjects.add(new MovieImage(b,movie_id));
                }while (movies.moveToNext());
            }
            return MovieImageObjects;
        }

        @Override
        protected void onPostExecute(List<MovieImage> list) {
            if(progressDialog != null){
                progressDialog.setMessage("Done ");
                progressDialog.hide();
            }
            if(list == null) {
                return;
            }
            movie_adapter.clear();
            Iterator<MovieImage> itr = list.iterator();
            while(itr.hasNext())
                movie_adapter.add(itr.next());
            super.onPostExecute(list);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main,container,false);

        if(savedInstanceState == null){
            for(int i=0;i<4;i++)
                images.add(new MovieImage(Integer.toString(R.drawable.loading),0));
        }
        movie_adapter = new MovieAdapter(getActivity(), images,new FetchMyDataCompleteListener());
        GridView movie_grid = (GridView) rootView.findViewById(R.id.movies_gridview);
        movie_grid.setAdapter(movie_adapter);
        movie_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getActivity(),"id is "+Long.toString(id),Toast.LENGTH_SHORT).show();
                MovieImage imageObject = (MovieImage) parent.getItemAtPosition(position);

                Intent intent = new Intent(getActivity(), MovieDetail.class);
                intent.putExtra("movie_id", id);
                intent.putExtra("image_path", imageObject.getImage_path());
                startActivity(intent);
            }
        });

        if(savedInstanceState == null){
            refresh_list(order_by);
        }

        return rootView;
    }


    public void refresh_list(String sort_order){
        movie_adapter.clear();

        for(MovieImage obj  : images )
            movie_adapter.add(new MovieImage(Integer.toString(R.drawable.loading),0));

        new FetchImageTask(getActivity(),new FetchMyDataCompleteListener())
                .execute(sort_order, Integer.toString(1));
    }


    public class FetchMyDataCompleteListener implements AsyncTaskCompleteListener{

        @Override
        public void onTaskComplete(String result) {
            Log.v(LOG_TAG, result);
            List<MovieImage> list = new ArrayList<MovieImage>();
            try {
                if(MovieAdapter.getPage_nmber() == 1)
                    movie_adapter.clear();
                parseJsonString(result, list);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onImagesComplete(int page_number) {
            new FetchImageTask(getActivity(),new FetchMyDataCompleteListener())
                    .execute(getString(R.string.popularity), Integer.toString(page_number));
        }
    }


    public void parseJsonString(String inpString,List<MovieImage> list) throws JSONException {
        String base_url = "http://image.tmdb.org/t/p/w500/";
        String jsonString = inpString;
        JSONObject root = new JSONObject(inpString);
        JSONArray result = root.getJSONArray("results");
        for(int i=0; i< result.length();i++){
            JSONObject movie = result.getJSONObject(i);
            String movie_image_path = movie.getString("poster_path");
            String image_url = base_url + movie_image_path;
            //Log.v(LOG_TAG,image_url);
            movie_adapter.add(new MovieImage(image_url, movie.getInt("id")));
            //list.add(new MovieImage(image_url));
        }
    }

}
