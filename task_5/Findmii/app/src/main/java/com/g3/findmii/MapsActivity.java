package com.g3.findmii;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
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
import android.provider.Settings;
import android.support.design.internal.NavigationMenuPresenter;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import messageUtils.Message;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double longit = 0, latid = 0;
    ProgressDialog progressDialog;
    ProgressDialog serverDialog;
    boolean gotPosition;
    LocationManager locationManager;
    String URL = "http://52.35.222.167:8080/Servlet/Servlet";
    ArrayList<ArrayList<Double>> weightedLatLng;
    ArrayList<ArrayList<String>> listOfHouses;
    Message message;
    ArrayList<LatLng> latlngs;
    ArrayList<Double> weights, averagePrice;
    ArrayList<WeightedLatLng> hmapData;
    HeatmapTileProvider mHeatMapProvider;
    TileOverlay mHeatMapTileOverlay;
    final static Float zoom = 12.5f;
    boolean isServerResponded = false;
    Menu menu;
    SearchManager searchManager;
    SearchView locationSearch;
    SearchView budgetSearch;
    MenuItem locationItem;
    MenuItem budgetItem;
    boolean searchAddress;
    boolean searchBudget;
    ArrayList<ArrayList<String>> favs;
    boolean showColourBar = false;
    TextView leastExpensive;
    TextView mostExpensive;
    TextView colorBar;
    boolean comparison;
    String propertyType = "N/A";
    int selectedPropertyType = 0;
    String desiredYear = "N/A";
    String budget = "";
    HashMap<LatLng, Double> currentLatLng = new HashMap<>();
    HashMap<LatLng, Marker> hMapMarkers = new HashMap<>();
    final int favaourite_max = 10;
    LatLng favPosisiton;
    boolean favIntent=false;
    NavigationView navigationView;
    Menu favMenu;
    MenuItem menuItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        gotPosition = false;
        comparison = false;

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


        loadFavMenu();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationListener(getActivity()));


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
                }
            }
        });


        leastExpensive = (TextView) findViewById(R.id.least_expensive);
        mostExpensive = (TextView) findViewById(R.id.most_expensive);
        colorBar = (TextView) findViewById(R.id.bar);

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
                if (!gotPosition) {
                    setLongAndLat(location);

                    gotPosition = true;
                    progressDialog.dismiss();
                    mMap.clear();
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
                    if(favIntent){
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(favPosisiton));
                        mMap.addMarker(new MarkerOptions().position(favPosisiton)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    }else{
                        LatLng pos = new LatLng(latid, longit);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                    }
                    contactServer(longit, latid, "N/A", desiredYear, propertyType, getActivity());
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

    public void contactServer(double longit, double latid, String budget, String year, String propertyType, Context c) {
        serverDialog.show();
        Intent i = new Intent(c, ContactServerTask.class);
        i.putExtra("longit", String.valueOf(longit));
        i.putExtra("latid", String.valueOf(latid));
        i.putExtra("URL", URL);
        if (searchBudget) {
            i.putExtra("budget_value", budget);
        } else {
            i.putExtra("budget_value", "N/A");
        }
        if (!year.equals("N/A")) {
            i.putExtra("specific_year", year);
        } else {
            i.putExtra("specific_year", "N/A");
        }
        if (!propertyType.equals("N/A")) {
            if (propertyType.equals("house")) {
                i.putExtra("property_type", "housesOnly");
            } else {
                i.putExtra("property_type", "flatsOnly");
            }
        } else {
            i.putExtra("property_type", "N/A");
        }
        c.startService(i);
        searchBudget = false;
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
        mHeatMapProvider.setRadius(22);
        mHeatMapTileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mHeatMapProvider));
        mHeatMapTileOverlay.clearTileCache();

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition pos) {
                if (pos.zoom > 16.2f) {
                    mHeatMapProvider.setRadius(60);
                    mHeatMapTileOverlay.clearTileCache();
                    LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                    for(LatLng current : latlngs){
                        if(bounds.contains(current)){
                            if(!currentLatLng.containsKey(current)){
                                Marker marker = mMap.addMarker(new MarkerOptions().position(current).title("" + averagePrice.get(latlngs.indexOf(current))));
                                currentLatLng.put(current, averagePrice.get(latlngs.indexOf(current)));
                                hMapMarkers.put(current, marker);
                            }
                        }
                        else
                        {
                            if(currentLatLng.containsKey(current)){
                                Marker marker = hMapMarkers.get(current);
                                hMapMarkers.remove(current);
                                currentLatLng.remove(current);
                                marker.remove();
                            }
                        }
                    }
                }
                else{
                    for(LatLng current : hMapMarkers.keySet()){
                        Marker marker = hMapMarkers.get(current);
                        marker.remove();
                    }
                    currentLatLng.clear();
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
            LatLng pos = new LatLng(latid, longit);
            String name = coord[2];
            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
            userSearch.hide();
            contactServer(pos.longitude, pos.latitude, "N/A", desiredYear, propertyType, getActivity());

        } catch (Exception e) {
            Snackbar.make(findViewById(android.R.id.content), "Sorry, we can't find the location you're looking for... Please try again", Snackbar.LENGTH_LONG)
                    .setActionTextColor(Color.RED)
                    .show();
        }
    }

    public void compareLocations(String queryOne, String queryTwo) {
        try {
            String[] addressOne = new SearchTask().execute(getString(R.string.browser_key), queryOne).get();
            String[] addressTwo = new SearchTask().execute(getString(R.string.browser_key), queryTwo).get();

            byte[] locationOne = new ServerTask().execute(URL, addressOne[0], addressOne[1], "N/A", "N/A", "N/A").get();
            byte[] locationTwo = new ServerTask().execute(URL, addressTwo[0], addressTwo[1], "N/A", "N/A", "N/A").get();

            ByteArrayInputStream inOne = new ByteArrayInputStream(locationOne);
            ByteArrayInputStream inTwo = new ByteArrayInputStream(locationTwo);

            ObjectInputStream isOne = new ObjectInputStream(inOne);
            ObjectInputStream isTwo = new ObjectInputStream(inTwo);

            Message messageOne = (Message) isOne.readObject();
            Message messageTwo = (Message) isTwo.readObject();

            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
            builder.setMessage("Got it");
            builder.create().show();

        } catch (Exception e) {
            Snackbar.make(findViewById(android.R.id.content), "Sorry, we can't find the location(s) you're trying to compare... Please try again", Snackbar.LENGTH_LONG)
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
     *
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

    private Menu getMenu() {
        return menu;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            //searches for address based on searchview's query
            if (searchAddress) {
                try {
                    String query = intent.getStringExtra(SearchManager.QUERY);
                    searchForAddress(query);
                } catch (Exception e) {

                }
            }         //searches for houses values up to a certain price based on searchview's query
            else {
                if(budgetRegexp(intent.getStringExtra(SearchManager.QUERY))) {
                    searchBudget = true;
                    contactServer(longit, latid, budget, desiredYear, propertyType, getApplicationContext());
                }
                else{
                    Snackbar.make(findViewById(android.R.id.content), "Sorry, the value you entered isn't recognised, please try again", Snackbar.LENGTH_LONG)
                            .setActionTextColor(Color.RED)
                            .show();
                }
            }
        }else if(Intent.ACTION_SEND.equals(intent.getAction())){
            //addHeatMap();
            favIntent = true;
            favPosisiton = new LatLng(intent.getDoubleExtra("Lat",latid),intent.getDoubleExtra("Lng",longit));
        }
    }

    public boolean budgetRegexp(String query) {
        if (query.charAt(0) != '£') {
            query = "£" + query;
        }
        if(query.matches("^(([£])?((([0-9]{1,3},)+[0-9]{3})|[0-9]+)?)$")){
            query = query.replaceAll("£|,", "");
            budget = query;
            return true;
        }
        return false;
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (serverDialog != null && serverDialog.isShowing()) {
            serverDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    /**
     * Performs a desired action based on whatever item is selected from the actionbar
     *
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
        if (id == R.id.about_us) {
            FrameLayout inflatedView = (FrameLayout) getLayoutInflater().inflate(R.layout.about_team_layout, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(inflatedView);
            builder.setTitle("About Us");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.create().show();
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

        if (id == R.id.location_search) {
            searchAddress = true;
            getMenu().findItem(R.id.add_place).setVisible(false);
            getMenu().findItem(R.id.search_item).setVisible(false);
            return true;
        }

        if (id == R.id.budget_search) {
            searchAddress = false;
            getMenu().findItem(R.id.add_place).setVisible(false);
            getMenu().findItem(R.id.search_item).setVisible(false);
            return true;
        }

        if (id == R.id.year) {
            LinearLayout inflatedView = (LinearLayout) getLayoutInflater().inflate(R.layout.year_picker_layout, null);

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Select A Year...");
            builder.setCancelable(false);

            final NumberPicker picker = (NumberPicker) inflatedView.findViewById(R.id.picker);
            picker.setMinValue(1995);
            picker.setMaxValue(2015);
            picker.setValue(1998);

            final CheckBox checkBox = (CheckBox) inflatedView.findViewById(R.id.all_years);
            checkBox.setText("Show results for all years");
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Log.v("<---", "changed");
                        desiredYear = "N/A";
                    }
                }
            });

            builder.setView(inflatedView);

            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(!checkBox.isChecked()) {
                        desiredYear = ""+picker.getValue();
                    }
                    contactServer(longit, latid, "N/A", desiredYear, propertyType, getActivity());
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
            return true;
        }

        if (id == R.id.colour_bar_toggle) {
            if (showColourBar) {
                colorBar.setVisibility(View.INVISIBLE);
                mostExpensive.setVisibility(View.INVISIBLE);
                leastExpensive.setVisibility(View.INVISIBLE);
                showColourBar = false;
            } else if (!showColourBar) {
                colorBar.setVisibility(View.VISIBLE);
                mostExpensive.setVisibility(View.VISIBLE);
                leastExpensive.setVisibility(View.VISIBLE);
                showColourBar = true;
            }
            return true;
        }

        if (id == R.id.property_preferences) {
            final CharSequence[] options = {"All Property", "Houses", "Flats"};
            final String[] flags = {"N/A", "house", "flat"};
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final int tmp = selectedPropertyType;
            builder.setCancelable(false);
            builder.setTitle("Select Property Type");
            builder.setSingleChoiceItems(options, selectedPropertyType, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.v("<---",""+which);
                    selectedPropertyType = which;
                }
            });
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    propertyType = flags[selectedPropertyType];
                    contactServer(longit, latid, "N/A", desiredYear, propertyType,getActivity());
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    selectedPropertyType = tmp;
                    dialog.cancel();
                }
            });
            builder.show();
        }

        if(id == R.id.comparison){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            alertDialog.setTitle("Compare Locations...");
            alertDialog.setMessage("Enter two locations in oder to compare them both against each other");


            final EditText textOne = new EditText(getApplicationContext());
            final EditText textTwo = new EditText(getApplicationContext());

            LinearLayout ll=new LinearLayout(this);
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.addView(textOne);
            ll.addView(textTwo);
            alertDialog.setView(ll);

            alertDialog.setPositiveButton("Compare", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
                    compareLocations(textOne.getText().toString(), textTwo.getText().toString());
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            AlertDialog alert = alertDialog.create();
            alert.show();

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
                        try {
                            Double lat = marker.getPosition().latitude;
                            Double longit = marker.getPosition().longitude;
                            String postcode = getPostCode(marker.getPosition().latitude, marker.getPosition().longitude);
                            String address = getAddress(lat,longit)+", ";
                            //String[] coord = new SearchTask().execute(getString(R.string.browser_key), postcode).get();
                            //String address = coord[2];
                            if (getNumberOfFavourites() <= favaourite_max) {
                                if (isFavourite(getActivity(), postcode,0)) {
                                    Toast.makeText(getActivity(), "Already a Favourite!", Toast.LENGTH_LONG).show();
                                } else {
                                    if (addToFavourites(lat, longit, postcode, address,marker.getTitle(), "createdat")) {
                                        Toast.makeText(getActivity(), "Added to favourites", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getActivity(), "Sorry, could not add to favourites", Toast.LENGTH_LONG).show();
                                    }
                                }
                            } else {
                                Toast.makeText(getActivity(), "Sorry, you have execeeded allowed number of favourites", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Log.e("SEARCH_TASK","Error-SearchTask");
                        }

                    }
                });

        alertDialog.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
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
                //loadFavMenu();
                addToFavMenu(address, newRowId);
                return true;
            } else {
                return false;
            }
        }catch (SQLiteException e){
            Log.i("SQLITE", e.getMessage());
            return false;
        }
    }
    public void addToFavMenu(String item, long id){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu favMenu = navigationView.getMenu();
        MenuItem menuItem = favMenu.findItem(R.id.favs);
        menuItem.getSubMenu().add(Menu.NONE, (int) id, Menu.NONE, item);
        getMenuInflater().inflate(R.menu.activity_main_drawer, favMenu);
    }
    public boolean removeFromFavMenu(Context c, AlertDialog.Builder builder,int id, MenuItem mnuItem){
        try {
            mnuItem.setVisible(false);
            return true;
        }catch(Exception e){
            Log.e("REMOVE_TASK",e.getMessage());
            return false;
        }
    }
    public boolean removeFromFavourite(Context c, int id){
        if(isFavourite(c,null,id)) {
            try {
                FavouriteReaderDbHelper mDbHelper = new FavouriteReaderDbHelper(c);
                SQLiteDatabase db = mDbHelper.getReadableDatabase();

                String query = "DELETE FROM " + FavouriteDBSchema.FavouriteSchema.TABLE_NAME
                        + " WHERE " + FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_ID + "=" + id;
                db.execSQL(query);
                return true;
            } catch (SQLiteException e) {
                Log.i("SQLITE", e.getMessage());
                return false;
            }
        }else {

            return false;
        }
    }
    static NavigationMenuPresenter getNavigationMenuPresenter(NavigationView view){
        try {
            Field presenterField = NavigationView.class.getDeclaredField("mPresenter");
            presenterField.setAccessible(true);
            return (NavigationMenuPresenter) presenterField.get(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    void setFavourites(Context c) {
        try {
            FavouriteReaderDbHelper mDbHelper = new FavouriteReaderDbHelper(c);
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            String query = "SELECT * FROM " + FavouriteDBSchema.FavouriteSchema.TABLE_NAME;
            Cursor results = db.rawQuery(query, null);
            favs = new ArrayList<>();

            if (results.moveToFirst()) {
                while (!results.isAfterLast()) {
                    ArrayList<String> fav = new ArrayList<>();
                    fav.add(String.valueOf(results.getInt(results.getColumnIndexOrThrow(FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_ID))));
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
    public String[] getFavouriteLatLng(Context c, int id){
        String[] coord = {Double.toString(Double.MIN_VALUE)};
        try {
            FavouriteReaderDbHelper mDbHelper = new FavouriteReaderDbHelper(c);
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            String query = "SELECT * FROM " + FavouriteDBSchema.FavouriteSchema.TABLE_NAME
                    + " WHERE " + FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_ID + "="+id;
            Cursor results = db.rawQuery(query, null);


            if (results.moveToFirst()) {
                while (!results.isAfterLast()) {
                    coord = new String[]{results.getString(results.getColumnIndexOrThrow(FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_LATITUDE)),
                            results.getString(results.getColumnIndexOrThrow(FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_LONGITUDE))
                    };
                    results.moveToNext();
                }
                return  coord;            }
        }catch(SQLiteException e){
            Log.i("SQLITE", e.getMessage());
        }
        return null;
    }


    boolean isFavourite(Context c, String postcode, int id){
        try {
            FavouriteReaderDbHelper mDbHelper = new FavouriteReaderDbHelper(c);
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            String query;
            if (postcode != null) {
                query = "SELECT * FROM " + FavouriteDBSchema.FavouriteSchema.TABLE_NAME + " WHERE " +
                        FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_POSTCODE + " = '" + postcode + "'";
            }else if(id !=0) {
                query = "SELECT * FROM " + FavouriteDBSchema.FavouriteSchema.TABLE_NAME + " WHERE " +
                        FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_ID + "=" + id;
            }else{
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
        return getFavourites(getActivity()).size();
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
    public String getAddress(double latitude, double longitude) {
        StringBuilder address = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 2);

            if (addresses.size() > 0) {
                address.append(addresses.get(0).getSubThoroughfare());
                address.append(", ");
                address.append(addresses.get(0).getThoroughfare());
                address.append(", ");
                address.append(addresses.get(0).getLocality());
                return address.toString();
            }
        } catch (IOException e) {
            Log.e("Geocoder", e.getMessage());
        }
        return null;
    }
    public void loadFavMenu(){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu favMenu = navigationView.getMenu();
        MainActivity mainActivity = new MainActivity();
        mainActivity.showFavs(getActivity(), favMenu);
        MenuItem mi = favMenu.getItem(favMenu.size() - 1);
        mi.setTitle(mi.getTitle());
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

                    mapsActivity.mostExpensive.setText("£" + mapsActivity.message.getMostExpensive());
                    mapsActivity.leastExpensive.setText("£" + mapsActivity.message.getLeastExpensive());

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
    public static final String DATABASE_NAME = "myfavss.db";


    final String TEXT_TYPE = " TEXT";
    final String ID_TYPE = " INTEGER";
    final String COMMA_SEP = ",";
    final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FavouriteDBSchema.FavouriteSchema.TABLE_NAME + " (" +
                    FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_ID + ID_TYPE + " PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                    FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_LATITUDE+ TEXT_TYPE + COMMA_SEP +
                    FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_LONGITUDE+ TEXT_TYPE + COMMA_SEP +
                    FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_POSTCODE + TEXT_TYPE + " UNIQUE" + COMMA_SEP +
                    FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_ADDRESS + TEXT_TYPE + COMMA_SEP +
                    FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_AVG_PRICE + TEXT_TYPE + COMMA_SEP +
                    FavouriteDBSchema.FavouriteSchema.COLUMN_NAME_CREATED_AT + TEXT_TYPE +
                    " )";
    //final String SQL_DELETE_ENTRY = "";

    public FavouriteReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        //  db.execSQL(SQL_DELETE_ENTRY);
        // onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
