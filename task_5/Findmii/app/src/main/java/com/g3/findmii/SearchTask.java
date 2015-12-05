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
public class SearchTask extends AsyncTask<String, Void, String[]> {


    @Override
    protected String[] doInBackground(String... params) {
        String mapsKey = params[0];
        String query = params[1];
        return loadJSON(query, mapsKey);
    }

    public String[] loadJSON(String query, String mapsKey){
        //String[] coord = null;
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
            String lat = Double.toString(jsonObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
            String lng = Double.toString(jsonObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
            String name = jsonObject.getJSONArray("results").getJSONObject(0).getString("formatted_address");
            String[] coord = new String[]{lat,lng,name};
            return coord;
        }
        catch (Exception e){
            Log.e("SEARCH_TASK",e.getMessage());
        }
        return null;
    }
}