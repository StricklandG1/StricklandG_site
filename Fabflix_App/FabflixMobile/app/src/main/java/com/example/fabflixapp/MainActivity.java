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


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void LoginRequest(View view)
    {
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
      
        //String url = "https://13.59.50.236:8443/CS_122B_Fablix_Project_API_Version/api/login";
        String url = "http://10.0.2.2:8080/CS_122B_Fablix_Project_API_Version/api/login";
        final StringRequest loginRequest = new StringRequest(
                Request.Method.POST,
                url,

                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        Log.d("login.success", response);
                        try
                        {
                            JSONObject jsonObj = new JSONObject(response);
                            String status = jsonObj.get("status").toString();

                            if (status.equals("success"))
                            {
                                ((TextView) findViewById(R.id.http_response)).setText(jsonObj.get("message").toString());
                                // go to movielist page
                                startActivity(new Intent(MainActivity.this, Top20Activity.class));
                            }
                            else
                            {
                                ((TextView) findViewById(R.id.http_response)).setText(jsonObj.get("message").toString());
                            }
                        }
                        catch(Throwable t)
                        {
                            Log.e("My App", "Could not parse malformed JSON: " + response);
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.d("login.error", error.toString());
                    }
                }
         ) {
            @Override
            protected Map<String, String> getParams() {
                // Post request form data
                final Map<String, String> params = new HashMap<String, String>();
                params.put("username", ((EditText)findViewById(R.id.username)).getText().toString());
                params.put("password", ((EditText)findViewById(R.id.password)).getText().toString());

                return params;
            }
        };
        queue.add(loginRequest);
    }
}
