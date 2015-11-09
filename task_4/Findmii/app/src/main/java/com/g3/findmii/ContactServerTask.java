package com.g3.findmii;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.SyncStateContract;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.EntityUtils;

/**
 * Created by matthewweller on 21/10/2015.
 */
public class ContactServerTask extends IntentService{

    static String PARAM_OUT = "com.example.android.threadsample.THISISABROADCAST";
    public ContactServerTask() {
        super("Contact Task");
    }

    @Override
    protected void onHandleIntent(Intent intent) {


        //need to add cached entries
        Intent localIntent;
        HttpClient httpclient = HttpClientBuilder.create().build();
        HttpPost httppost = new HttpPost(String.valueOf(intent.getStringExtra("URL")));
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("latitude", intent.getStringExtra("latid")));
        nameValuePairs.add(new BasicNameValuePair("longitude", intent.getStringExtra("longit")));

            // Execute HTTP Post Request
        try {
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            byte [] container = EntityUtils.toByteArray(entity);
            localIntent= new Intent(PARAM_OUT).putExtra("STATUS", "OK");
            localIntent.putExtra("container", container);
            localIntent.putExtra("UPLOADED","TRUE");
            Log.v("NETWORK SUCCESS: ====> ", "Count: ");
        }
        catch (Exception e) {
            localIntent= new Intent(PARAM_OUT).putExtra("STATUS","ERROR");
            localIntent.putExtra("UPLOADED","FALSE");
            Log.v("NETWORK ERROR: =====>", "Count: ");
        }
        localIntent.setAction(ContactServerTask.PARAM_OUT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
