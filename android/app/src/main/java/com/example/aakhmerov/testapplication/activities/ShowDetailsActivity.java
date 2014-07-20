package com.example.aakhmerov.testapplication.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.aakhmerov.testapplication.R;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class ShowDetailsActivity extends Activity {

    // Handles to UI widgets
    private TextView mLatLng;
    private Gson gson = new Gson();
    private HashMap offer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_details);

        Intent intent = getIntent();
        String message = intent.getStringExtra(LocatorActivity.COORDS_MESSAGE);
        mLatLng = (TextView) findViewById(R.id.lat_lng);
        mLatLng.setText(message);

        this.offer = gson.fromJson(intent.getStringExtra(LocatorActivity.OFFER_MESSAGE),HashMap.class);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.show_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
