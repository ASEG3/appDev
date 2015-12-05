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
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
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
import android.support.v4.view.MenuItemCompat;
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
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.cache.Resource;
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
    String URL = "http://52.32.13.98:8080/Servlet/Servlet";
    ArrayList<ArrayList<Double>> weightedLatLng;
    ArrayList<ArrayList<String>> listOfHouses;
    Message message;
    ArrayList<LatLng> latlngs;
    ArrayList<Double> weights, averagePrice;
    ArrayList<WeightedLatLng> hmapData;
    HeatmapTileProvider mHeatMapProvider;
    TileOverlay mHeatMapTileOverlay;
    final static Float zoom = 13.0f;
    boolean isServerResponded = false;
    Menu menu;
    SearchManager searchManager;
    SearchView locationSearch;
    SearchView budgetSearch;
    MenuItem locationItem;
    MenuItem budgetItem;
    boolean searchAddress;
    boolean searchBudget;
    ArrayList<Marker> hMapMarkers;
    ArrayList<ArrayList<String>> favs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Waiting for location");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

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
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.nav_fav) {
                    MainActivity activityMain = new MainActivity();
                    activityMain.showFavs();
                }
                return true;
            }
        });

        gotPosition = false;
        handleIntent(getIntent());
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
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                    mMap.addMarker(new MarkerOptions().position(pos).title("You are here")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    contactServer(longit, latid, "0");
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
                progressDialog.show();
                gotPosition = false;
            }
        }
    }

    public void contactServer(double longit, double latid, String budget) {
        serverDialog.show();
        Intent i = new Intent(getActivity(), ContactServerTask.class);
        i.putExtra("longit", String.valueOf(longit));
        i.putExtra("latid", String.valueOf(latid));
        i.putExtra("URL", URL);
        i.putExtra("budget", searchBudget);
        i.putExtra("budget_value", budget);
        searchBudget = false;
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

    public void addHeatMap() {
        mMap.clear();
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mHeatMapProvider = new HeatmapTileProvider.Builder().weightedData(hmapData).build();
        mHeatMapProvider.setRadius(20);
        mHeatMapTileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mHeatMapProvider));
        mHeatMapTileOverlay.clearTileCache();
        hMapMarkers = new ArrayList<>();
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition pos) {
                if (pos.zoom > 16.0f) {
                    addHeatmapMarkers(latlngs, averagePrice, mMap);
                } else {
                    removeHeatMapMarkers();
                   // mMap.addMarker(new MarkerOptions().position(new LatLng(latid, longit)).title("Here you are")
                     //       .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                }
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                showFavouriteDialog(marker);
                return false; // shows default as well
            }
        });
    }
    /** places the points on the heatmap
     * @params dataLatLng, avgPrice, mMap: info from server
     **/
    private void addHeatmapMarkers(ArrayList<LatLng> dataLatLng, ArrayList<Double> avgPrice, GoogleMap mMap) {

        for (int i = 0; i < dataLatLng.size(); i++) {
            Marker mMapMarker = mMap.addMarker(new MarkerOptions().position(dataLatLng.get(i)).title("Average price: £" + avgPrice.get(i).toString()));
            hMapMarkers.add(mMapMarker);
        }
    }

    /** removes marker points from heat map
     *
     */
    private void removeHeatMapMarkers(){
        if(hMapMarkers.size()>0){
            for(int i=0;i< hMapMarkers.size();i++){
                hMapMarkers.get(i).remove();
            }
        }

    }

    /**
     * Searches for an inputted query using the JSON API provided by Google Places. The query is from the associated searchview.
     *
     * @param query The search term inputted by the user.
     */
    public void searchForAddress(String query) {
        try {
            ProgressDialog userSearch = new ProgressDialog(getApplicationContext());
            userSearch.setMessage("Finding...");
            String[] coord = new SearchTask().execute(getString(R.string.browser_key), query).get();
            latid = Double.parseDouble(coord[0]);
            longit = Double.parseDouble(coord[1]);
            LatLng pos = new LatLng(Double.parseDouble(coord[0]), Double.parseDouble(coord[1]));
            String name = coord[2];
            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
            mMap.addMarker(new MarkerOptions().position(pos).title("Searched Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            userSearch.hide();
            contactServer(pos.longitude, pos.latitude, "0");

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

    /**
     * Initialises the searchview, inserting it into the actionbar.
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        this.menu = menu;
        this.searchManager = searchManager;

        locationItem = menu.findItem(R.id.location_search);
        budgetItem = menu.findItem(R.id.budget_search);

        locationSearch =
                (SearchView) locationItem.getActionView();
        budgetSearch =
                (SearchView) budgetItem.getActionView();

        locationSearch.setIconifiedByDefault(true);

        budgetSearch.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        locationSearch.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        locationSearch.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {


            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                getMenu().findItem(R.id.add_place).setVisible(true);
                getMenu().findItem(R.id.search_item).setVisible(true);
            }
        });

        budgetSearch.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                getMenu().findItem(R.id.add_place).setVisible(true);
                getMenu().findItem(R.id.search_item).setVisible(true);
            }
        });
        return true;
    }

    private Menu getMenu(){
        return menu;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            //searches for address based on searchview's query
            if(searchAddress) {
                try {
                    String query = intent.getStringExtra(SearchManager.QUERY);
                    searchForAddress(query);
                } catch (Exception e) {

                }
            }
            //searches for houses values up to a certain price based on searchview's query
            else{
                searchBudget = true;
                contactServer(longit,latid, intent.getStringExtra(SearchManager.QUERY));
            }
        }
    }

    /**
     * Performs a desired action based on whatever item is selected from the actionbar
     * @param item
     * @return
     */
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
            return true;
        }

        if(id == R.id.location_search){
            searchAddress = true;
            getMenu().findItem(R.id.add_place).setVisible(false);
            getMenu().findItem(R.id.search_item).setVisible(false);
            return true;
        }

        if(id == R.id.budget_search){
            searchAddress = false;
            getMenu().findItem(R.id.add_place).setVisible(false);
            getMenu().findItem(R.id.search_item).setVisible(false);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showFavouriteDialog(Marker makr){
        final Marker marker = makr;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                getActivity());

        alertDialog.setTitle("Favourites");

        alertDialog
                .setMessage("Add to Favourite location?");

        alertDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Double lat = marker.getPosition().latitude;
                        Double longit = marker.getPosition().longitude;
                        String postcode = getPostCode(marker.getPosition().latitude, marker.getPosition().longitude);

                        //Double.toString(lat)+","+Double.toString(longit)
                        try {
                            //SearchTask crd = new SearchTask();
                            //crd.execute()
                            String[] coord = new SearchTask().execute(getResources().getString(R.string.browser_key), postcode).get();
                            String address = coord[0];

                            String me ="me";//Random
                            int i=2*2;
                            // save to favourites
                           /* if (isFavourite(getActivity(), postcode)) {
                                Toast.makeText(getActivity(), "Already a Favourite!", Toast.LENGTH_LONG).show();
                            } else {
                                if (addToFavourites(lat, longit, postcode,address,
                                        marker.getTitle(), "createdat")) {
                                    Toast.makeText(getActivity(), "Added to favourites", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getActivity(), "Sorry, could not add to favourites", Toast.LENGTH_LONG).show();
                                }
                            }*/
                        }catch (Exception e){
                            Log.e("ADDR",e.getMessage());
                            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG).show();
                        }

                        //;
                    }
                });

        alertDialog.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        if(getFavourites(getActivity()).size()>0){
                            Toast.makeText(getActivity(),String.valueOf(favs.get(1).size()),Toast.LENGTH_LONG).show();
                        }
                    }
                });
        alertDialog.show();
    }
    public boolean addToFavourites(Double latitude, Double longitude, String postcode, String address, String avgprice, String createdate){
        try {
            FavouriteReaderDbHelper mDbHelper = new FavouriteReaderDbHelper(getApplicationContext());
            // Gets the data repository in write mode
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_LATITUDE, latitude);
            values.put(FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_LONGITUDE, longitude);
            values.put(FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_POSTCODE, postcode);
            values.put(FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_ADDRESS, address);
            values.put(FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_AVG_PRICE, avgprice);
            //values.put(FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_CREATED_AT, createdate);

            // Insert the new row, returning the primary key value of the new row
            long newRowId;
            newRowId = db.insert(
                    FavouriteDBSchema.FavouriteSchema.TABLE_NAME,
                    FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_POSTCODE,
                    values);
            if (newRowId > 0.0) {
                return true;
            } else {
                return false;
            }
        }catch (SQLiteException e){
            Log.i("SQLITE", e.getMessage());
        }
        return false;
    }
    void setFavourites(Context c) {
        try {
            FavouriteReaderDbHelper mDbHelper = new FavouriteReaderDbHelper(c);
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            String query = "SELECT * FROM " + FavouriteDBSchema.FavouriteSchema.TABLE_NAME;
            Cursor results = db.rawQuery(query, null);
            favs = new ArrayList<>();

            if (results.moveToFirst()) {
                while (results.isAfterLast() == false) {
                    ArrayList<String> fav = new ArrayList<>();
                    fav.add(results.getString(results.getColumnIndexOrThrow(FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_LATITUDE)));
                    fav.add(results.getString(results.getColumnIndexOrThrow(FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_LONGITUDE)));
                    fav.add(results.getString(results.getColumnIndexOrThrow(FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_POSTCODE)));
                    fav.add(results.getString(results.getColumnIndexOrThrow(FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_ADDRESS)));
                    fav.add(results.getString(results.getColumnIndexOrThrow(FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_AVG_PRICE)));
                    favs.add(fav);
                    results.moveToNext();
                }
            }
        }catch(SQLiteException e){
            Log.i("SQLITE", e.getMessage());
        }
    }


    boolean isFavourite(Context c, String postcode){
        try {
            FavouriteReaderDbHelper mDbHelper = new FavouriteReaderDbHelper(c);
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            String query;
            if (postcode != null) {
                query = "SELECT * FROM " + FavouriteDBSchema.FavouriteSchema.TABLE_NAME + " WHERE " +
                        FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_POSTCODE + " = '" + postcode + "'";
            } else {
                query = "SELECT * FROM " + FavouriteDBSchema.FavouriteSchema.TABLE_NAME;
            }
            Cursor results = db.rawQuery(query, null);

            if (results.getCount() <= 0) {
                results.close();
                return false;
            }
            results.close();
            return true;
        }catch(SQLiteException e){
            Log.i("SQLITE", e.getMessage());
            return false;
        }

    }
    int getNumberOfFavourites(){
        return favs.size();
    }
    ArrayList<ArrayList<String>> getFavourites(Context c){
        setFavourites(c);
        return favs;
    }
    //return the address from latitude and longitude
    public String getPostCode(double latitude, double longitude) {
        List<Address> addresses=null;
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext());
            addresses  = geocoder.getFromLocation(latitude, longitude, 1);
            return addresses.get(0).getPostalCode();
        } catch (IOException e) {
            Log.e("Geocoder", e.getMessage());
        }
        return null;
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
                    mapsActivity.addHeatMap();
                    mapsActivity.serverDialog.hide();
                } else {
                    mapsActivity.serverDialog.hide();
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
class FavouriteReaderDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "myfavs.db";


    final String TEXT_TYPE = " TEXT";
    final String COMMA_SEP = ",";
    final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FavouriteDBSchema.FavouriteSchema.TABLE_NAME + " (" +
                    FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_LATITUDE+ TEXT_TYPE + COMMA_SEP +
                    FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_LONGITUDE+ TEXT_TYPE + COMMA_SEP +
                    FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_POSTCODE + TEXT_TYPE + " PRIMARY KEY" + COMMA_SEP +
                    FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_ADDRESS + TEXT_TYPE + COMMA_SEP +
                    FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_AVG_PRICE + TEXT_TYPE + COMMA_SEP +
                    FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_CREATED_AT + TEXT_TYPE +
                    " )";

    public FavouriteReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        //db.execSQL(SQL_DELETE_ENTRIES);
        // onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}