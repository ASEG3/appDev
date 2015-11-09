package com.g3.findmii;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by matthewweller on 22/10/2015.
 */
public class LocationInfo implements Parcelable {

    private String MAC;
    private String longit;
    private String latid;
    private String finalEntry;

    public LocationInfo(String MAC, String longit, String latid, String finalEntry){
        this.MAC = MAC;
        this.longit = longit;
        this.latid = latid;
        this.finalEntry = finalEntry;
    }

    public String getFinalEntry() {
        return finalEntry;
    }

    public void setFinalEntry(String finalEntry) {
        this.finalEntry = finalEntry;
    }

    public String getLongit() {
        return longit;
    }

    public void setLongit(String longit) {
        this.longit = longit;
    }

    public String getLatid() {
        return latid;
    }

    public void setLatid(String latid) {
        this.latid = latid;
    }

    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public LocationInfo(Parcel in){
        String[] data = new String[4];

        in.readStringArray(data);
        this.MAC = data[0];
        this.longit = data[1];
        this.latid = data[2];
        this.finalEntry = data[3];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.MAC,
                this.longit,
                this.latid,
                this.finalEntry});
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public LocationInfo createFromParcel(Parcel in) {
            return new LocationInfo(in);
        }

        public LocationInfo[] newArray(int size) {
            return new LocationInfo[size];
        }
    };
}
