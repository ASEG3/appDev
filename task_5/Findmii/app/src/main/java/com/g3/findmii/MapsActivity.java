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
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
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
    String URL = "http://52.10.109.23:8080/Servlet/Servlet";
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
    boolean showColourBar = false;
    TextView leastExpensive;
    TextView mostExpensive;
    TextView colorBar;
    boolean comparison;
    String propertyType = "N/A";
    int selectedPropertyType = 0;
    String desiredYear = "N/A";

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
                    LatLng pos = new LatLng(latid, longit);
                    gotPosition = true;
                    progressDialog.dismiss();
                    mMap.clear();
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                    contactServer(longit, latid, "N/A", desiredYear, propertyType);
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

    public void contactServer(double longit, double latid, String budget, String year, String propertyType) {
        serverDialog.show();
        Intent i = new Intent(getActivity(), ContactServerTask.class);
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
        getActivity().startService(i);
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

    public void addHeatMap() {
        mMap.clear();
        mHeatMapProvider = new HeatmapTileProvider.Builder().weightedData(hmapData).build();
        mHeatMapProvider.setRadius(20);
        mHeatMapTileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mHeatMapProvider));
        mHeatMapTileOverlay.clearTileCache();
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.addMarker(new MarkerOptions().position(new LatLng(latid, longit)).title("You are here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
    }

    private void addMarkers(ArrayList<LatLng> dataLatLng, ArrayList<Double> avgPrice, GoogleMap mMap) {

        for (int i = 0; i < dataLatLng.size(); i++) {
            Marker mMapMarker = mMap.addMarker(new MarkerOptions().position(dataLatLng.get(i)).title("Average price: £" + avgPrice.get(i).toString()));
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
            LatLng pos = new LatLng(latid, longit);
            String name = coord[2];
            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
            userSearch.hide();
            contactServer(pos.longitude, pos.latitude, "N/A", desiredYear, propertyType);

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
            }
            //searches for houses values up to a certain price based on searchview's query
            else {
                searchBudget = true;
                contactServer(longit, latid, intent.getStringExtra(SearchManager.QUERY), desiredYear, propertyType);
            }
        }
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
                    contactServer(longit, latid, "N/A", desiredYear, propertyType);
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
                    contactServer(longit, latid, "N/A", desiredYear, propertyType);
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
