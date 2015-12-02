package com.g3.findmii;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Address;
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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
import messageUtils.Message;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double longit = 0, latid = 0;
    ProgressDialog progressDialog;
    ProgressDialog serverDialog;
    boolean gotPosition;
    LocationManager locationManager;
    String URL = "http://52.33.174.180:8080/Servlet/Servlet";
    ArrayList<ArrayList<Double>> weightedLatLng;
    ArrayList<ArrayList<String>> listOfHouses;
    Message message;
    ArrayList<LatLng> latlngs;
    ArrayList<Double> weights, averagePrice;
    ArrayList<WeightedLatLng> hmapData;
    HeatmapTileProvider mHeatMapProvider;
    TileOverlay mHeatMapTileOverlay;
    final static Float MAX_ZOOM = 16.0f;
    boolean isServerResponded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Waiting for location");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        serverDialog = new ProgressDialog(this);
        serverDialog.setMessage("Getting houses within 3 miles");
        serverDialog.setCancelable(false);
        serverDialog.setCanceledOnTouchOutside(false);

        IntentFilter mStatusIntentFilter = new IntentFilter(ContactServerTask.PARAM_OUT);
        Receiver broadcastReciever = new Receiver(this, this);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReciever, mStatusIntentFilter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationListener());


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.location_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentLocation();
            }
        });

        FloatingActionButton housefab = (FloatingActionButton) findViewById(R.id.house_button);
        housefab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isServerResponded) {
                    houseList();
                } else {
                    Log.i("HEAT_MAP", "Cannot access server!");
                    Snackbar.make(findViewById(android.R.id.content), "Sorry, server is not reachable!", Snackbar.LENGTH_LONG)
                            .setActionTextColor(Color.RED)
                            .show();
                }            }
        });

        gotPosition = false;
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            try {
                String query = intent.getStringExtra(SearchManager.QUERY);
                searchForAddress(query);
            } catch (Exception e) {

            }
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
        mMap.getUiSettings().setZoomGesturesEnabled(false);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                setLongAndLat(location);
                if (!gotPosition) {
                    LatLng pos = new LatLng(latid, longit);
                    gotPosition = true;
                    progressDialog.dismiss();
                    mMap.clear();
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(13));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                    mMap.addMarker(new MarkerOptions().position(pos).title("You are here")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));//                    contactServer(longit,latid);
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

// Register the listener with the Location Manager to receive location updates
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } catch (SecurityException e) {
            Log.w(e.getMessage(), "THERES A PROBLEM");
        }
    }

    public void setLongAndLat(Location location) {
        longit = location.getLongitude();
        latid = location.getLatitude();
    }

    public void currentLocation() {
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
        } else {
            if (gotPosition) {
                //progressDialog.show();
                gotPosition = false;
            }
        }
    }

    public void contactServer(double longit, double latid) {
        serverDialog.show();
        Intent i = new Intent(getActivity(), ContactServerTask.class);
        i.putExtra("longit", String.valueOf(longit));
        i.putExtra("latid", String.valueOf(latid));
        i.putExtra("URL", URL);
        getActivity().startService(i);

    }

    public void houseList() {
        Intent i = new Intent(getActivity(), HouseList.class);
        ArrayList<String> values = new ArrayList<>();
        int counter = 0;
        for (ArrayList<String> current : listOfHouses) {
            String tmp = current.get(1) + " " + current.get(2) + " " + current.get(3)
                    + "\n" + current.get(4) + "\n" + current.get(5) + "\n" + current.get(6) + "\n"
                    + "Price Sold: £" + current.get(7) + "\n" + "Distance: ~" + current.get(9) + " Miles";
            values.add(tmp);
            counter++;
            if (counter > 1000) {
                break;
            }
        }
        String[] smpValues = values.toArray(new String[values.size()]);
        i.putExtra("houselist", smpValues);
        getActivity().startActivity(i);
    }


    /*

     Sets the weighted data (latitude and longitude and average price from the server)
     Converts received lat, long into LatLng type
    */
    public void setDataFromServer(ArrayList<ArrayList<Double>> wLatLng) {
        //use weightedMessage variable to generate the heat map
        latlngs = new ArrayList<>();
        weights = new ArrayList<>();
        averagePrice = new ArrayList<>();
        for (int i = 0; i < wLatLng.size(); i++) {
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
        for (int i = 0; i < dataLatLng.size(); i++) {
            WeightedLatLng wlatlng = new WeightedLatLng(dataLatLng.get(i), latLngWeight.get(i));
            hmapData.add(wlatlng);
        }
    }

    private void addHeatMap() {
        mMap.clear();
        mHeatMapProvider = new HeatmapTileProvider.Builder().weightedData(hmapData).build();
        mHeatMapProvider.setRadius(100);
        mHeatMapTileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mHeatMapProvider));
        mHeatMapTileOverlay.clearTileCache();
        addMarkers(latlngs, averagePrice, mMap);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latid, longit), MAX_ZOOM));
    }

    private void addMarkers(ArrayList<LatLng> dataLatLng, ArrayList<Double> avgPrice, GoogleMap mMap) {

        for (int i = 0; i < dataLatLng.size(); i++) {
            Marker mMapMarker = mMap.addMarker(new MarkerOptions().position(dataLatLng.get(i)).title("Average price: £" + avgPrice.get(i).toString()));
        }
    }

    /**
     * Searches for an inputted query using geocoder.
     *
     * @param query The search term inputted by the user.
     */
    public void searchForAddress(String query) {
        try {

            Double[] coord = new SearchTask().execute(getString(R.string.browser_key), query).get();
            LatLng pos = new LatLng(coord[0], coord[1]);
            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.zoomTo(13));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
            mMap.addMarker(new MarkerOptions().position(pos).title("Searched Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
////            contactServer(target.getLongitude(), target.getLatitude());

        } catch (Exception e) {
            Snackbar.make(findViewById(android.R.id.content), "Sorry, we can't find the location you're looking for... Please try again", Snackbar.LENGTH_LONG)
                    .setActionTextColor(Color.RED)
                    .show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search_item).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.map_type) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Select Map Type...")
                    .setItems(R.array.view_list, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            } else if (which == 1) {
                                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

                            } else if (which == 2) {
                                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

                            } else if (which == 3) {
                                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

                            } else if (which == 4) {
                                if (isServerResponded) {
                                    addHeatMap();
                                } else {
                                    Log.i("HEAT_MAP", "Cannot access server!");
                                    Snackbar.make(findViewById(android.R.id.content), "Sorry, server is not reachable!", Snackbar.LENGTH_LONG)
                                            .setActionTextColor(Color.RED)
                                            .show();
                                }
                            }
                        }
                    });
            Dialog mapType = builder.create();
            mapType.show();
        }
        return super.onOptionsItemSelected(item);
    }
}

class Receiver extends BroadcastReceiver {

    //here is where we recieve info from server

    Context context;
    MapsActivity mapsActivity;

    public Receiver(Context context, MapsActivity maps) {
        this.context = context;
        this.mapsActivity = maps;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getStringExtra("STATUS").equals("OK")) {
            byte[] container = intent.getByteArrayExtra("container");
            ByteArrayInputStream in = new ByteArrayInputStream(container);
            try {
                ObjectInputStream is = new ObjectInputStream(in);
                mapsActivity.message = (Message) is.readObject();
                if (mapsActivity.message.getSizeOfWeighted() > 0) {
                    mapsActivity.weightedLatLng = mapsActivity.message.getHouse();
                    mapsActivity.listOfHouses = mapsActivity.message.getHouses();
                    mapsActivity.serverDialog.hide();

                    //set heatmap data
                    mapsActivity.setDataFromServer(mapsActivity.weightedLatLng);
                    mapsActivity.setHeatmapData(mapsActivity.latlngs, mapsActivity.weights);
                    mapsActivity.isServerResponded = true;
                    Snackbar.make(mapsActivity.findViewById(android.R.id.content), "Generated Heatmap", Snackbar.LENGTH_LONG)
                            .setActionTextColor(Color.RED)
                            .show();
                    Log.w("WE GOT OKAY", "ITS ALL GOOD");
                    mapsActivity.serverDialog.hide();
                } else {
                    Snackbar.make(mapsActivity.findViewById(android.R.id.content), "Server is not reachable", Snackbar.LENGTH_LONG)
                            .setActionTextColor(Color.RED)
                            .show();
                    Log.i("EMPTY_RESPONSE", "Server returned empty data, most likely, database issue");
                }
            } catch (Exception e) {
                mapsActivity.serverDialog.hide();
                Snackbar.make(mapsActivity.findViewById(android.R.id.content), "Unable to generate Heatmap", Snackbar.LENGTH_LONG)
                        .setActionTextColor(Color.RED)
                        .show();
                e.printStackTrace();
                Log.w("WE GOT BAD", "ITS ALL NOT GOOD");
            }
        } else if (intent.getStringExtra("STATUS").equals("ERROR")) {
            mapsActivity.serverDialog.hide();
            Snackbar.make(mapsActivity.findViewById(android.R.id.content), "Unable to contact server", Snackbar.LENGTH_LONG)
                    .setActionTextColor(Color.RED)
                    .show();
        }
    }
}
