package com.example.aakhmerov.testapplication.is24;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.aakhmerov.testapplication.LocatorActivity;
import com.example.aakhmerov.testapplication.is24.tos.OfferTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by AAkhmerov on 7/19/2014.
 */
public class Is24Service {

    private static final String BASE_URL = "http://rest.immobilienscout24.de/restapi/api/search/v1.0/search/radius";

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private static final String GEO_PARAM = "geocoordinates";
    private static final String RADIUS_VALUE = "5";
    private static final String OUR_SCALE = "SCALE_210x210";

    OkHttpClient client = new OkHttpClient();

    Gson gson = new Gson();


    public String searchRegion (String longitude, String latitude) {
        String result = null;

        String geoValue = latitude + ";"+ longitude + ";" + RADIUS_VALUE;
        geoValue = URLEncoder.encode(geoValue);
        String params = "realestatetype=ApartmentRent&" + GEO_PARAM + "=" + geoValue;

        String request = BASE_URL + "?" + params;
        String json = null;
        try {
            json = get(request);
            result = json;
        } catch (IOException e) {
            Log.e("search error",e.getMessage(),e);
        }
        return result;
    }

    public List <Map> parseOffers (String jsonData) {
        List <OfferTO> parsedOffers = null;
        HashMap result = gson.fromJson(jsonData, HashMap.class);
//      TODO: replace with proper parsing to entities. is there xsd of Immo?
        List <Map> entries = new LinkedList<Map>();
        if (result != null) {
            entries = (List<Map>) ((Map)((List)((Map)result.get("resultlist.resultlist")).get("resultlistEntries")).get(0)).get("resultlistEntry");
        }
        return entries;
    }

    String get(String url) throws IOException {
        Request
                request = new Request.Builder()
                .url(url)
                .addHeader("Accept","application/json; charset=utf-8")
                .get()
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public ImageView constructImage(Map offer, Context locatorActivity) {
        ImageView result = new ImageView(locatorActivity);
        String url = null;
        List<Map> scales = (List) ((Map)((List)((Map)offer.get("titlePicture")).get("urls")).get(0)).get("url");
        for (Map scale : scales) {
            if (OUR_SCALE.equals(scale.get("@scale"))) {
                url = (String) scale.get("@href");
            }
        }
        DownloadImageTask task = new DownloadImageTask();
        task.execute(result,url);
        return result;
    }

    public DownloadImmoDataTask constructDownloadTask() {
        return new DownloadImmoDataTask();
    }

    public View constructTextLine(String title, LocatorActivity locatorActivity) {
        TextView t = new TextView(locatorActivity);
        t.setText( title);
        t.setGravity(Gravity.LEFT);
        t.setMaxLines(4);
//        t.getLayout().set(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        return t;
    }

    /**
     * Download images
     */
    public final class DownloadImageTask extends AsyncTask<Object/* Param */, Boolean /* Progress */, Bitmap /* Result */> {
        ImageView destView;
        @Override
        protected Bitmap doInBackground(Object... params) {
            publishProgress(true);
            destView = (ImageView) params[0];
            URL newurl = null;
            Bitmap mIcon_val = null;
            try {
                newurl = new URL((String) params[1]);
                mIcon_val = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Do the usual httpclient thing to get the result
            return mIcon_val;
        }

        @Override
        protected void onProgressUpdate(Boolean... progress) {
            // line below coupled with
            //    getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
            //    before setContentView
            // will show the wait animation on the top-right corner
//            LocatorActivity.this.setProgressBarIndeterminateVisibility(progress[0]);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            publishProgress(false);
            destView.setImageBitmap(result);
        }
    }

    public final class DownloadImmoDataTask extends AsyncTask<Object/* Param */, Boolean /* Progress */, String /* Result */> {
        private LocatorActivity activity;
        @Override
        protected String doInBackground(Object... params) {
            publishProgress(true);
            activity = (LocatorActivity) params[2];
            // Do the usual httpclient thing to get the result
            return  searchRegion(String.valueOf(params[0]),String.valueOf(params[1]));
        }

        @Override
        protected void onProgressUpdate(Boolean... progress) {
            // line below coupled with
            //    getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
            //    before setContentView
            // will show the wait animation on the top-right corner
//            LocatorActivity.this.setProgressBarIndeterminateVisibility(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            publishProgress(false);
            activity.displayApartments(result);
        }
    }
}
