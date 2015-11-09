package com.g3.findmii;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Message implements Serializable {

    public static final long serialVersionUID = 1L;
    public HashMap<String, ArrayList<String>> houses;

    public ArrayList<ArrayList<String>> newHouses;

    public Message() {
        houses = new HashMap<String, ArrayList<String>>();
        newHouses = new ArrayList<ArrayList<String>>();
    }

    public void addHouseEntry(String houseID, ArrayList<String> houseInformation) {
        houses.put(houseID, houseInformation);
    }

    public ArrayList<String> getHouseInformation(String houseID) {
        return houses.get(houseID);

    }

    public void addHouseEntryNew(ArrayList<String> houseEntry) {
        newHouses.add(houseEntry);
    }

    public ArrayList<String> getHouseInfo(int i) {
        return newHouses.get(i);
    }

    public int getSize() {
        return newHouses.size();
    }

    public ArrayList<ArrayList<String>> getHouses() {
        return newHouses;
    }

}