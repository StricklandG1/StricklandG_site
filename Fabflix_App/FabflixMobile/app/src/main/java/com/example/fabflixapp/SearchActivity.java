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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.DefaultRetryPolicy;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.json.JSONArray;
import android.widget.AdapterView;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    String  id = "";
    String page = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Bundle bundle = getIntent().getExtras();

        id = bundle.getString("query");
        page = bundle.getString("page");
        Log.d("SearchValues", id + page);

        final ListView movieList = (ListView) findViewById(R.id.search_listview);
        final ArrayList<Movie> movies = new ArrayList<>();

        final MovieListViewAdapter  movieAdapter = new MovieListViewAdapter(movies, this);
        movieList.setAdapter(movieAdapter);

        movieList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = movies.get(position);
                Log.d("Clicked movie id: ", movie.getId());
                Intent goToIntent = new Intent(SearchActivity.this, SingleMovie.class);
                goToIntent.putExtra("movie_title", movie.getTitle());
                goToIntent.putExtra("movie_year", movie.getYear());
                goToIntent.putExtra("movie_director", movie.getDirector());
                goToIntent.putExtra("movie_genres", movie.getGenres());
                goToIntent.putExtra("movie_stars", movie.getStars());

                startActivity(goToIntent);
            }
        });
//search=fts&id=test&sort=title&order=ASC&page=0&results=10

        //String url = "https://13.59.50.236:8443/CS_122B_Fablix_Project_API_Version/api/search?search=fts&id=" + id + "&sort=title&order=ASC&page=" + page + "&results=10";
        String url = "http://10.0.2.2:8080/CS_122B_Fablix_Project_API_Version/api/search?search=fts&id=" + id + "&sort=title&order=ASC&page=" + page + "&results=10";

        final RequestQueue queue =  NetworkManager.sharedManager(this).queue;

        final StringRequest getData = new StringRequest(
            Request.Method.GET,
            url,
            new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    try
                    {
                        JSONArray data = new JSONArray(response);
                        int len = data.length();

                        for(int i = 0; i < len; ++i)
                        {
                            JSONObject dataObj = data.optJSONObject(i);
                            String id = dataObj.getString("movie_id");
                            String title = dataObj.getString("movie_title");
                            String year = dataObj.getString("movie_year");
                            String director = dataObj.getString("movie_director");
                            String genres = dataObj.getString("movie_genres");
                            String stars = dataObj.getString("movie_stars");
                            movies.add(new Movie(id, title, year, director, genres, stars));
                        }
                        movieAdapter.notifyDataSetChanged();
                    }
                    catch (Throwable t)
                    {
                        Log.e("SearchError", "Could not parse object");
                    }
                }
            },
            new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    Log.e("FailedResponse", error.toString());
                }
            }
        );
        getData.setRetryPolicy(new DefaultRetryPolicy(50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(getData);
        Log.d("ArrayListSize: ", Integer.toString(movies.size()));
    }

    public void onNext(View view)
    {
        Intent goToIntent = new Intent(SearchActivity.this, SearchActivity.class);
        int pageNum = Integer.parseInt(page);
        ++pageNum;
        page = Integer.toString(pageNum);
        goToIntent.putExtra("query", id);
        goToIntent.putExtra("page", page);
        startActivity(goToIntent);
    }

    public void onPrev(View view)
    {
        int pageNum = Integer.parseInt(page);
        if (pageNum > 0)
        {
            Intent goToIntent = new Intent(SearchActivity.this, SearchActivity.class);
            --pageNum;
            page = Integer.toString(pageNum);
            goToIntent.putExtra("query", id);
            goToIntent.putExtra("page", page);
            startActivity(goToIntent);
        }
    }
}
