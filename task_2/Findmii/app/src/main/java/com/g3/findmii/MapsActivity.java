package com.g3.findmii;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double longit, latid;
    ProgressDialog progressDialog;
    boolean gotPosition;
    LocationManager locationManager;
    Spinner mapType;
    Button posButton;
    TextView longLat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(this);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapType = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.view_list,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapType.setAdapter(adapter);

        longLat = (TextView) findViewById(R.id.long_lat);

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
                if(parent.getItemAtPosition(position).equals("Road Map")){
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
                else if(parent.getItemAtPosition(position).equals("Satellite")){
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                }
                else if(parent.getItemAtPosition(position).equals("Hybrid")){
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                }
                else if(parent.getItemAtPosition(position).equals("Terrain")){
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
                if(!gotPosition) {
                    setLongAndLat(location);
                    Log.w(latid + "+" + longit, "___POS___");
                    LatLng pos = new LatLng(latid, longit);
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(pos).title("Here you are"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
                    gotPosition = true;
                    progressDialog.dismiss();
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

        gotPosition = false;
        if(longit == 0 && latid == 0)
        {
            progressDialog.setMessage("Waiting for location");
            progressDialog.show();
        }
    }

    public void setLongAndLat(Location location) {
        longit = location.getLongitude();
        latid = location.getLatitude();
        String tmp = longit + ", " + latid;
        longLat.setText(tmp);
    }

    public void currentLocation(){
        if(gotPosition){
            progressDialog.setMessage("Waiting for location");
            progressDialog.show();
            gotPosition = false;
        }
    }
}
