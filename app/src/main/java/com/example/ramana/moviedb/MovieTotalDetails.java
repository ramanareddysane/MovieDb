package com.example.ramana.moviedb;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ramana on 19/1/16.
 */

public class MovieTotalDetails implements Parcelable {

    private ContentValues details;
    private Map trailers;
    private Map reviews;

    public MovieTotalDetails(){
        details = new ContentValues();
        trailers = new HashMap();
        reviews = new HashMap();
    }

    public void setTrailers(Map trailers) { this.trailers = trailers;}
    public void setReviews(Map reviews) {this.reviews = reviews;}
    public void setDetails(ContentValues details) {this.details = details;}

    public ContentValues getDetails() { return details;}
    public Map getReviews() {return reviews;}
    public Map getTrailers() {return trailers;}

    protected MovieTotalDetails(Parcel in) {
        in.readValue(ContentValues.class.getClassLoader());
        in.readMap(trailers,String.class.getClassLoader());
        in.readMap(reviews,String.class.getClassLoader());
    }

    public static final Creator<MovieTotalDetails> CREATOR = new Creator<MovieTotalDetails>() {
        @Override
        public MovieTotalDetails createFromParcel(Parcel in) {
            return new MovieTotalDetails(in);
        }

        @Override
        public MovieTotalDetails[] newArray(int size) {
            return new MovieTotalDetails[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(details);
        dest.writeMap(trailers);
        dest.writeMap(reviews);
    }
}
