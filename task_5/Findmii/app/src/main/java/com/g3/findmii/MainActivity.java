package com.g3.findmii;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.app.AlertDialog;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<ArrayList<String>> favList;
    LocationManager lm;
    boolean gpsEnabled;
    boolean networkEnabled;
    Menu favMenu;
    SubMenu favSubMenu;
    NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gpsEnabled = false;
        networkEnabled = false;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button fab = (Button) findViewById(R.id.start_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                        !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Location Settings Are Disabled");
                    builder.setMessage("Please enable your location within your phone's settings");
                    builder.setPositiveButton("Settings...", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                }
            }
        });

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        loadFavMenu();
        navigationView.setNavigationItemSelectedListener(new NavigationListener(getActivity()));

    }
    public void loadFavMenu(){
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu favMenu = navigationView.getMenu();
        showFavs(getActivity(), favMenu);
        MenuItem mi = favMenu.getItem(favMenu.size() - 1);
        mi.setTitle(mi.getTitle());
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
        //getMenuInflater().inflate(R.menu.activity_main_drawer, favMenu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
        }
        return true;

    }
    @Override
    protected void onRestart() {
        super.onRestart();
    }

    public void showFavs(Context c, Menu menu) {
        MapsActivity favs = new MapsActivity();
        MenuItem menuItem = menu.findItem(R.id.favs);
        if (!favs.isFavourite(c, null,0)) {
            //Toast.makeText(MainActivity.this, "You have not added any Favourites", Toast.LENGTH_LONG).show();
            menuItem.getSubMenu().add(Menu.NONE, -1, Menu.NONE, "No Favourites");
        } else {
            displayFavouriteList(favs, c, menu);
        }
    }

    public void displayFavouriteList(MapsActivity favs, Context c, Menu menu) {
        favList = new ArrayList<>();
        favList = favs.getFavourites(c);
        MenuItem menuItem = menu.findItem(R.id.favs);
        for (ArrayList<String> current : favList) {
            StringBuilder tmp = new StringBuilder();
            tmp.append(current.get(4));
            //tmp.append(", ");
            tmp.append(current.get(3));
            menuItem.getSubMenu();
            menuItem.getSubMenu().add(Menu.NONE, Integer.parseInt(current.get(0)), Menu.NONE, tmp);
        }
    }


    public Activity getActivity() {
        return this;
    }
}