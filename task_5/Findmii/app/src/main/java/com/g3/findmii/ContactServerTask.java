package com.g3.findmii;

import android.app.IntentService;
import android.content.Intent;
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
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("latitude", intent.getStringExtra("longit")));
        nameValuePairs.add(new BasicNameValuePair("longitude", intent.getStringExtra("latid")));

        // Execute HTTP Post Request
        try {
            HttpGet httpget = new HttpGet(String.valueOf(intent.getStringExtra("URL") + "?" + URLEncodedUtils.format(nameValuePairs, "utf-8")));
            HttpResponse response = httpclient.execute(httpget);
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
