package com.g3.findmii;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

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
 * Created by matthewweller on 05/12/15.
 */
public class ServerTask extends AsyncTask<String, Void, byte[]> {
    @Override
    protected byte[] doInBackground(String... params) {

        String URL = params[0];

        Intent localIntent;
        HttpClient httpclient = HttpClientBuilder.create().build();
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("latitude", params[1]));
        nameValuePairs.add(new BasicNameValuePair("longitude", params[2]));
        if(!params[2].equals("N/A"))
        {
            nameValuePairs.add(new BasicNameValuePair("budget", params[3]));
        }
        if(!params[3].equals("N/A")){
            nameValuePairs.add(new BasicNameValuePair("specificYear", params[4]));
        }
        if(!params[4].equals("N/A")){
            nameValuePairs.add(new BasicNameValuePair(params[5], "something"));
        }
        // Execute HTTP Post Request
        try {
            HttpGet httpget = new HttpGet(String.valueOf(URL + "?" + URLEncodedUtils.format(nameValuePairs, "utf-8")));
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            byte [] container = EntityUtils.toByteArray(entity);
            Log.v("NETWORK SUCCESS: ====> ", "Count: ");
            return container;
        }
        catch (Exception e) {
            Log.v("NETWORK ERROR: =====>", "Count: ");
            return new byte[0];
        }
    }
}
