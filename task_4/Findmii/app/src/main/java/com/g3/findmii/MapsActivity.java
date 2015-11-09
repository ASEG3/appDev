package com.g3.findmii;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double longit = 0, latid = 0;
    ProgressDialog progressDialog;
    boolean gotPosition;
    LocationManager locationManager;
    Spinner mapType;
    Button posButton;
    Button houseButton;
    TextView longLat;
    HashMap<String, ArrayList<String>> networkAttempts;
    String URL = "http://52.11.103.82:8080/Servlet/Servlet";
    boolean setTimer;
    boolean onFirstRun;
    ContainerObject container;
    WeightedMessage weightedMessage;
    com.g3.findmii.Message message;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTimer = false;
        onFirstRun = true;
        networkAttempts = new HashMap<>();
        IntentFilter mStatusIntentFilter = new IntentFilter(ContactServerTask.PARAM_OUT);
        Receiver broadcastReciever = new Receiver(this,this);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReciever, mStatusIntentFilter);
        progressDialog = new ProgressDialog(this);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapType = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.view_list,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapType.setAdapter(adapter);
        mapFragment.getMapAsync(this);

        longLat = (TextView) findViewById(R.id.long_lat);

        houseButton = (Button) findViewById(R.id.housebutton);
        houseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HouseList.class);
//                byte[] byteHouse =
//                startActivity(intent);
            }
        });

        posButton = (Button) findViewById(R.id.location_button);
        posButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLocation();
            }
        });
        mapType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).equals("Road Map")) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                } else if (parent.getItemAtPosition(position).equals("Satellite")) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else if (parent.getItemAtPosition(position).equals("Hybrid")) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                } else if (parent.getItemAtPosition(position).equals("Terrain")) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        gotPosition = false;
        if(longit == 0 && latid == 0)
        {
            progressDialog.setMessage("Waiting for location");
            progressDialog.show();
        }
    }

    public MapsActivity getActivity() {
        return this;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.getUiSettings().setCompassEnabled(true);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                setLongAndLat(location);
                if(!gotPosition && latid != 0 && longit != 0) {
                    LatLng pos = new LatLng(latid, longit);
                    gotPosition = true;
                    progressDialog.dismiss();
                    if(onFirstRun){
                        onFirstRun = false;
                        currentLocation();
                    }
                    if(!onFirstRun) {
                        mMap.clear();
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(16));
                        mMap.addMarker(new MarkerOptions().position(pos).title("Here you are"));
                        //here is where we post info to the server
                        contactServer();
                    }
                }
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

// Register the listener with the Location Manager to receive location updates
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        catch (SecurityException e){
            Log.w(e.getMessage(),"THERES A PROBLEM");
        }
    }

    public void setLongAndLat(Location location) {
        longit = location.getLongitude();
        latid = location.getLatitude();
        String tmp = longit + ", " + latid;
        longLat.setText(tmp);
    }

    public void currentLocation(){
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("GPS is not enabled, please enable it")
                    .setPositiveButton("Settings...", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(i);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            builder.create();
            builder.show();
        }
        else{
            if (gotPosition) {
                progressDialog.setMessage("Waiting for location");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                gotPosition = false;
            }
        }
    }

    public void contactServer(){
        Intent i = new Intent(getActivity(), ContactServerTask.class);
        i.putExtra("longit", String.valueOf(longit));
        i.putExtra("latid", String.valueOf(latid));
        i.putExtra("URL", URL);
        getActivity().startService(i);
    }

    public void heatmap(){
        //use weightedmessage varible to generate the heat map
        ArrayList<ArrayList<Double>> weightedLongLat = weightedMessage.getHouse();
    }

}

class Receiver extends BroadcastReceiver {

    //here is where we recieve info from server

    Context context;
    MapsActivity mapsActivity;

    public Receiver (Context context, MapsActivity maps){
        this.context = context;
        this.mapsActivity = maps;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getStringExtra("STATUS").equals("OK")) {
            byte[] container = intent.getByteArrayExtra("container");
            ByteArrayInputStream in = new ByteArrayInputStream(container);
            try {
                ObjectInputStream is = new ObjectInputStream(in);
                mapsActivity.container = (ContainerObject) is.readObject();
                mapsActivity.message = mapsActivity.container.returnMessage();
                mapsActivity.weightedMessage = mapsActivity.container.returnWeightedMessage();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else if(intent.getStringExtra("STATUS").equals("ERROR")) {
            Snackbar.make(mapsActivity.findViewById(android.R.id.content), "Unable to contact server", Snackbar.LENGTH_LONG)
                    .setActionTextColor(Color.RED)
                    .show();
        }
    }
}
