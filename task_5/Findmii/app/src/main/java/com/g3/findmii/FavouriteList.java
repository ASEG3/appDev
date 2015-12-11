package com.g3.findmii;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by tim on 30/11/15.
 */
public class FavouriteList extends ListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favourites_list);

        String[] favValues = getIntent().getStringArrayExtra("favouritelist");
        ArrayList<String> values = new ArrayList<>(Arrays.asList(favValues));
        createListView(values);
    }


    public void createListView(ArrayList<String> favList){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, favList);
        setListAdapter(adapter);
    }
}
