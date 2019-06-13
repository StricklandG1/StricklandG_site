package com.example.fabflixapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.content.Intent;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

public class SingleMovie extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_movie);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();

        String title = bundle.getString("movie_title");
        String year = bundle.getString("movie_year");
        String director = bundle.getString("movie_director");
        String genres = bundle.getString("movie_genres");
        String stars = bundle.getString("movie_stars");

        ((TextView) findViewById(R.id.Title)).setText(title);
        ((TextView) findViewById(R.id.Year)).setText(year);
        ((TextView) findViewById(R.id.Director)).setText(director);
        ((TextView) findViewById(R.id.Genres)).setText(genres);
        ((TextView) findViewById(R.id.Stars)).setText(stars);
    }

}
