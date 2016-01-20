package com.example.ramana.moviedb;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

public class MovieDetail extends AppCompatActivity {

    private static final String DETAIL_FRAGMENT_TAG = "movie_detail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentManager manager = getSupportFragmentManager();
        Fragment detail_frag = manager.findFragmentByTag(DETAIL_FRAGMENT_TAG);

        if(detail_frag == null || savedInstanceState ==null){
//            Toast.makeText(this,"first time",Toast.LENGTH_SHORT).show();
            manager.beginTransaction()
                    .add(R.id.fragment_detail_comntainer,
                            new MovieDetailFragment(),
                            "movie_detail")
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_movie_detail_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


}
