package com.example.ramana.moviedb;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;


public class MovieAdapter extends ArrayAdapter<MovieImage> {

    private static int page_nmber = 1;
    private AsyncTaskCompleteListener listener;
    private static Boolean offline = false;

    //Constructor for MovieAdapter
    public MovieAdapter(Context context, List<MovieImage> objects,AsyncTaskCompleteListener listener) {
        super(context, 0 , objects);
        this.listener = listener;
    }


    public static void setPage_nmber(int page_nmber) {
        MovieAdapter.page_nmber = page_nmber;
    }

    public static int getPage_nmber() {
        return page_nmber;
    }

    final String LOG_TAG = MovieAdapter.class.getSimpleName();

    @Override
    public long getItemId(int position) {
        return getItem(position).getMovie_id();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MovieImage movieImage = getItem(position);
        if(movieImage.getImage_path() == null)
            offline = true;
        else
            offline = false;

        if(!offline && getCount() == (position + 5)){
            this.page_nmber ++ ;
            Log.v(LOG_TAG, " last two images" + getPage_nmber());
            listener.onImagesComplete(getPage_nmber());
        }

        View rootView = LayoutInflater.from(getContext())
                        .inflate(R.layout.list_item_movie, parent, false);

        ImageView imageView = (ImageView) rootView.findViewById(R.id.list_item_movie_image);
        imageView.setImageResource(R.drawable.loading);

        if(offline){
            Bitmap bitmap = movieImage.getBitmap();
            imageView.setImageBitmap(bitmap);
        }
        else
            Picasso.with(getContext()).load(movieImage.getImage_path())
                    .placeholder(R.drawable.loading)
//                    .fit()
                    .into(imageView);

        return rootView;
    }
}
