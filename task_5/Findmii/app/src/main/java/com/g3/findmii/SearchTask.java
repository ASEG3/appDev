package com.g3.findmii;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.utils.URLEncodedUtils;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.util.EntityUtils;

/**
 * Created by matthewweller on 01/12/15.
 */
public class SearchTask extends AsyncTask<String, Void, Double[]> {


    @Override
    protected Double[] doInBackground(String... params) {
        String mapsKey = params[0];
        String query = params[1];
        return loadJSON(query, mapsKey);
    }

    public Double[] loadJSON(String query, String mapsKey){
        Double[] coord = {Double.MIN_VALUE};
        try {
            Log.v("<---",query);
            HttpClient httpclient = HttpClientBuilder.create().build();
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("query",query));
            nameValuePairs.add(new BasicNameValuePair("key", mapsKey));
            HttpGet httpget = new HttpGet("https://maps.googleapis.com/maps/api/place/textsearch/json?" + URLEncodedUtils.format(nameValuePairs, "UTF-8"));
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            JSONObject jsonObject = new JSONObject(EntityUtils.toString(entity));
            double lat = jsonObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
            double lng = jsonObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
            coord = new Double[]{lat,lng};
        }
        catch (Exception e){

        }
        return coord;
    }
}
