package com.g3.findmii;

import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;

import messageUtils.Message;

public class HouseList extends ListActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_list);

        String[] smpValues = getIntent().getStringArrayExtra("houselist");
        ArrayList<String> values = new ArrayList<>(Arrays.asList(smpValues));
        createListView(values);
    }


    public void createListView(ArrayList<String> houseList){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, houseList);
        setListAdapter(adapter);
    }


}
