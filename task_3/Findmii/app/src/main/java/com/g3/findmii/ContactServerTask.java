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

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

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
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(String.valueOf(intent.getStringExtra("URL")));
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("MAC", intent.getStringExtra("MAC")));
        nameValuePairs.add(new BasicNameValuePair("ENTRY", intent.getStringExtra("latid")+ "," + intent.getStringExtra("longit")));

            // Execute HTTP Post Request
        try {
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            httpclient.execute(httppost);
            localIntent= new Intent(PARAM_OUT).putExtra("STATUS","OK");
            localIntent.putExtra("UPLOADED","TRUE");
            Log.v("NETWORK SUCCESS: ====> ", "Count: ");
        }
        catch (Exception e) {
            localIntent= new Intent(PARAM_OUT).putExtra("STATUS","ERROR");
            localIntent.putExtra("UPLOADED","FALSE");
            Log.v("NETWORK ERROR: =====>", "Count: ");
        }
        localIntent.setAction(ContactServerTask.PARAM_OUT);
        localIntent.putExtra("PACKETID", intent.getStringExtra("PACKETID"));
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
