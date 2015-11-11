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
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import messageUtils.Message;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double longit = 0, latid = 0;
    ProgressDialog progressDialog;
    ProgressDialog serverDialog;
    boolean gotPosition;
    LocationManager locationManager;
    Spinner mapType;
    Button posButton;
    Button houseButton;
    String URL = "http://52.24.80.127:8080/Servlet/Servlet";
    boolean onFirstRun;
    ArrayList<ArrayList<Double>> weightedLatLng;
    ArrayList<ArrayList<String>> listOfHouses;
    Message message;
    ArrayList<LatLng> latlngs;
    ArrayList<Double> weights, averagePrice;
    ArrayList<WeightedLatLng> hmapData;
    HeatmapTileProvider mHeatMapProvider;
    TileOverlay mHeatMapTileOverlay;
    final static Float MAX_ZOOM = 13.5f;
    boolean isServerResponded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Waiting for location");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        serverDialog = new ProgressDialog(this);
        serverDialog.setMessage("Getting houses within 1 mile");
        serverDialog.setCancelable(false);
        serverDialog.setCanceledOnTouchOutside(false);
        onFirstRun = true;
        IntentFilter mStatusIntentFilter = new IntentFilter(ContactServerTask.PARAM_OUT);
        Receiver broadcastReciever = new Receiver(this, this);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReciever, mStatusIntentFilter);

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapType = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.view_list,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapType.setAdapter(adapter);
        mapFragment.getMapAsync(this);

        houseButton = (Button) findViewById(R.id.housebutton);
        houseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                houseList();
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
                }else if (parent.getItemAtPosition(position).equals("Heat Map")) {
                    if(isServerResponded) {
                        addHeatMap();
                    }else {
                        Log.i("HEAT_MAP", "Cannot access server!");
                        Snackbar.make(findViewById(android.R.id.content), "Sorry, server is not reachable!", Snackbar.LENGTH_LONG)
                                .setActionTextColor(Color.RED)
                                .show();
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        gotPosition = false;
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
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                setLongAndLat(location);
                if(!gotPosition) {
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
                        contactServer(longit,latid);
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
                progressDialog.show();
                gotPosition = false;
            }
        }
    }

    public void contactServer(double longit, double latid){
        serverDialog.show();
        Intent i = new Intent(getActivity(), ContactServerTask.class);
        i.putExtra("longit", String.valueOf(longit));
        i.putExtra("latid", String.valueOf(latid));
        i.putExtra("URL", URL);
        getActivity().startService(i);

    }

    public void setOtherLocation(double lat, double lng){

        LatLng pos = new LatLng(latid, longit);
        gotPosition = true;
        progressDialog.setMessage("Waiting for location");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(16));
        mMap.addMarker(new MarkerOptions().position(pos).title("Here you are"));
        //here is where we post info to the server
        contactServer(lng, lat);
        progressDialog.hide();
    }

    public void houseList(){
        Intent i = new Intent(getActivity(), HouseList.class);
        ArrayList<String> values = new ArrayList<>();
        for(ArrayList<String> current : listOfHouses){
            String tmp = current.get(1) + " " + current.get(2) + " " + current.get(3)
                    + "\n" + current.get(4) + "\n" + current.get(5) + "\n" + current.get(6) + "\n"
                    + "Price Sold: £" + current.get(7) + "\n" + "Distance: ~" + current.get(9) + " Miles";
            values.add(tmp);
        }
        String[] smpValues = values.toArray(new String[values.size()]);
        i.putExtra("houselist",smpValues);
        getActivity().startActivity(i);
    }


    /*

     Sets the weighted data (latitude and longitude and average price from the server)
     Converts received lat, long into LatLng type
    */
    public void setDataFromServer(ArrayList<ArrayList<Double>> wLatLng){
        //use weightedMessage variable to generate the heat map
        latlngs = new ArrayList<>();
        weights = new ArrayList<>();
        averagePrice = new ArrayList<>();
        for(int i=0;i<wLatLng.size();i++){
            latlngs.add(new LatLng(wLatLng.get(i).get(0), wLatLng.get(i).get(1)));
            weights.add(wLatLng.get(i).get(2));
            averagePrice.add(wLatLng.get(i).get(3));
        }
       /* latlngs.add(new LatLng(50.8677065292, -0.0881093842587));
        latlngs.add(new LatLng(latid, longit));
        weights.add(1.0); weights.add(2.0);*/
    }

    /*
        receives arraylist of latitude & longitude, weight
        converts to WeightedLatLng type for the heatmap to display.
     */
    public void setHeatmapData(ArrayList<LatLng> dataLatLng, ArrayList<Double> latLngWeight) {

        hmapData = new ArrayList<WeightedLatLng>();
        //averagePrice = new ArrayList<>();
        for(int i=0; i<dataLatLng.size(); i++){
            WeightedLatLng wlatlng = new WeightedLatLng(dataLatLng.get(i),latLngWeight.get(i));
            hmapData.add(wlatlng);
        }
    }

    private void addHeatMap()
    {
        mMap.clear();
        mHeatMapProvider = new HeatmapTileProvider.Builder().weightedData(hmapData).build();
        mHeatMapProvider.setRadius(100);
        mHeatMapTileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mHeatMapProvider));
        mHeatMapTileOverlay.clearTileCache();
        addMarkers(latlngs, averagePrice, mMap);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latid,longit), MAX_ZOOM));
    }

    private void addMarkers(ArrayList<LatLng> dataLatLng, ArrayList<Double> avgPrice, GoogleMap mMap){
        if(dataLatLng.size() != avgPrice.size()){
            //create an exception here!
            Log.i("HEATMAP_DATA","LatLng size must match weight size");
        }
        for(int i=0; i<dataLatLng.size(); i++){
            Marker mMapMarker = mMap.addMarker(new MarkerOptions().position(dataLatLng.get(i)).title("Average price: £" + avgPrice.get(i).toString()));
        }
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
                mapsActivity.message = (Message) is.readObject();
                mapsActivity.weightedLatLng = mapsActivity.message.getHouse();
                mapsActivity.listOfHouses = mapsActivity.message.getHouses();
                mapsActivity.serverDialog.hide();

                //set heatmap data
                mapsActivity.setDataFromServer(mapsActivity.weightedLatLng);
                mapsActivity.setHeatmapData(mapsActivity.latlngs, mapsActivity.weights);
                Log.w("WE GOT OKAY", "ITS ALL GOOD");
                mapsActivity.isServerResponded = true;
                Snackbar.make(mapsActivity.findViewById(android.R.id.content), "Generated Heatmap", Snackbar.LENGTH_LONG)
                        .setActionTextColor(Color.RED)
                        .show();
                Log.w("WE GOT OKAY", "ITS ALL GOOD");

            }
            catch (Exception e){
                mapsActivity.serverDialog.hide();
                Snackbar.make(mapsActivity.findViewById(android.R.id.content), "Unable to generate Heatmap", Snackbar.LENGTH_LONG)
                        .setActionTextColor(Color.RED)
                        .show();
                e.printStackTrace();
                Log.w("WE GOT BAD", "ITS ALL NOT GOOD");
            }
        }
        else if(intent.getStringExtra("STATUS").equals("ERROR")) {
            Snackbar.make(mapsActivity.findViewById(android.R.id.content), "Unable to contact server", Snackbar.LENGTH_LONG)
                    .setActionTextColor(Color.RED)
                    .show();
        }
    }
}
