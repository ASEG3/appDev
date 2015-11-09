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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double longit = 0, latid = 0;
    ProgressDialog progressDialog;
    boolean gotPosition;
    LocationManager locationManager;
    Spinner mapType;
    Button posButton;
    Button serverButton;
    TextView longLat;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    String mac;
    HashMap<String, ArrayList<String>> networkAttempts;
    String URL = "http://ec2-52-10-83-78.us-west-2.compute.amazonaws.com:8080/Servlet/Servlet";
    static int packetCounter = 0;
    boolean setTimer;
    boolean onFirstRun;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTimer = false;
        onFirstRun = true;
        networkAttempts = new HashMap<>();
        IntentFilter mStatusIntentFilter = new IntentFilter(ContactServerTask.PARAM_OUT);
        Receiver broadcastReciever = new Receiver(alarmMgr,alarmIntent,this,this);
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

        serverButton = (Button) findViewById(R.id.serverbutton);
        serverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Input Server URL");

                final EditText input = new EditText(getActivity());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String m_Text = input.getText().toString();
                        URL = m_Text;
                        Snackbar.make(findViewById(android.R.id.content), "Changed the server URL", Snackbar.LENGTH_LONG)
                                .setActionTextColor(Color.RED)
                                .show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
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
                if(!gotPosition) {
                    LatLng pos = new LatLng(latid, longit);
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(pos).title("Here you are"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
                    gotPosition = true;
                    progressDialog.dismiss();
                    if(!onFirstRun) {
                        Snackbar.make(findViewById(android.R.id.content), "Found your location", Snackbar.LENGTH_LONG)
                                .setActionTextColor(Color.RED)
                                .show();
                    }
                }
                if(!setTimer){
                    setTimer = true;
                    setTimerTask();
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
                progressDialog.show();
                gotPosition = false;
            }
        }
    }

//    public void postToServer(double lat, double longit){
//
//        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//        WifiInfo wInfo = wifiManager.getConnectionInfo();
//        String macAddress = wInfo.getMacAddress();
//
//        Intent i = new Intent(this, ContactServerTask.class);
//        i.putExtra("MAC", macAddress);
//        i.putExtra("longit", Double.toString(longit));
//        i.putExtra("latid", Double.toString(lat));
//
//        startService(i);
//    }

    public void setTimerTask(){
        if(onFirstRun) {
            currentLocation();
            onFirstRun = false;
        }
        new Thread(new Runnable() {
            public void run() {
                WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                WifiInfo wInfo = wifiManager.getConnectionInfo();
                mac = wInfo.getMacAddress();
                String packetID = mac +  String.valueOf(packetCounter);

                alarmMgr = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);

                Intent i = new Intent(getActivity(), ContactServerTask.class);

                ArrayList<String> info = new ArrayList<>();
                info.add(mac);
                info.add(String.valueOf(longit));
                info.add(String.valueOf(latid));

                i.putExtra("MAC", mac);
                i.putExtra("longit", String.valueOf(longit));
                i.putExtra("latid", String.valueOf(latid));
                i.putExtra("URL", URL);
                i.putExtra("PACKETID", packetID);

                networkAttempts.put(packetID, info);

                Log.v("LONG", "" + longit);
                Log.v("LAT", "" + latid);

                MapsActivity.packetCounter++;

                getActivity().startService(i);
            }
        }).start();
    }

}

class Receiver extends BroadcastReceiver {

    AlarmManager alarmMgr;
    PendingIntent alarmIntent;
    Context context;
    MapsActivity mapsActivity;

    public Receiver (AlarmManager alarmMgr, PendingIntent alarmIntent, Context context, MapsActivity maps){
        this.alarmMgr = alarmMgr;
        this.alarmIntent = alarmIntent;
        this.context = context;
        this.mapsActivity = maps;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("BROADCAST SAYS", intent.getStringExtra("STATUS"));
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mapsActivity.setTimerTask();
                }
            }, 1000 * 60 * 1);
        if(intent.getStringExtra("STATUS").equals("OK")) {
            String packetID = intent.getStringExtra("PACKETID");
            mapsActivity.networkAttempts.remove(packetID);
            Snackbar.make(mapsActivity.findViewById(android.R.id.content), "Sent your location", Snackbar.LENGTH_LONG)
                    .setActionTextColor(Color.RED)
                    .show();
        }
        else if(intent.getStringExtra("STATUS").equals("ERROR")) {
            Snackbar.make(mapsActivity.findViewById(android.R.id.content), "Cached your location", Snackbar.LENGTH_LONG)
                    .setActionTextColor(Color.RED)
                    .show();
        }
        Log.v("CACHE SIZE:",""+mapsActivity.networkAttempts.size());
    }
}
