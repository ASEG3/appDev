package com.g3.findmii;

import android.app.ListActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class HouseList extends ListActivity {

//    Message houseList;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_house_list);
//
//        byte [] houseByte = getIntent().getExtras().getByteArray("houselist");
//        try {
//            houseList = generateHouseList(houseByte);
//        }
//        catch (Exception e){
//            Log.v("TRIED TO RECOVER OBJECT",e.getMessage());
//        }
//        createListView(houseList);
//
//    }
//
//    public Message generateHouseList(byte[] houseByte) throws Exception{
//        ByteArrayInputStream in = new ByteArrayInputStream(houseByte);
//        ObjectInputStream is = new ObjectInputStream(in);
//        return (Message) is.readObject();
//    }
//
//    public void createListView(Message houseList){
//        ArrayList<String> values = new ArrayList<>();
//        for(ArrayList<String> current : houseList.getHouses()){
//            String tmp = current.get(1) + " " + current.get(2) + " " + current.get(3)
//                    + "\n" + current.get(4) + "\n" + current.get(5) + "\n" + current.get(6)
//                    + "Price Sold: £" + current.get(7) + "\n" + "Distance: ~" + current.get(8) + " Miles";
//            values.add(tmp);
//
//        }
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1, values);
//        setListAdapter(adapter);
//    }


}
