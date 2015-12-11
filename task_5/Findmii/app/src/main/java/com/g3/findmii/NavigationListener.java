package com.g3.findmii;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Created by matthewweller on 02/12/15.
 */
public class NavigationListener extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    Context c;
    public NavigationListener(Context context){
        c=context;
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        final int id = item.getItemId();
        final MenuItem mnuItem = item;
        if(id>0) {
            String options[] = new String[]{"Remove from favourites","View on Map"};
            final AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setTitle("Select an Action");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // the user clicked on options[which]
                    MapsActivity mapsActivity = new MapsActivity();
                    if (which == 0) {
                        if (mapsActivity.removeFromFavMenu(c,builder,id, mnuItem)) {
                            if (mapsActivity.removeFromFavourite(c, id)) {
                                Toast.makeText(c, "Removed from Favourite", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        //contact server
                        String coord[] = new String[]{};
                        coord = mapsActivity.getFavouriteLatLng(c, id);
                        //LatLng pos = new LatLng(Double.parseDouble(coord[0]),Double.parseDouble(coord[1]));
                        //mapsActivity.contactServer(Double.parseDouble(coord[0]),Double.parseDouble(coord[1]),"N/A","N/A","N/A",getActivity());
                        Intent i = new Intent(c, MapsActivity.class);
                        //startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                        i.setAction(Intent.ACTION_SEND);
                        i.putExtra("Lat", Double.parseDouble(coord[0]));
                        i.putExtra("Lng", Double.parseDouble(coord[1]));
                        c.startActivity(i);
                    }
                }
            });
            builder.show();
        }

        return true;
    }

}
