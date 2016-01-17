package com.example.ramana.moviedb;

/**
 * Created by ramana on 21/12/15.
 */
public interface AsyncTaskCompleteListener{

  public void onTaskComplete(String result);

  public void onImagesComplete(int page_number);
}
