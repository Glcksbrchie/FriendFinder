package com.esch.eschfindr.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public class Friend implements Parcelable{

    public String id;
    public String name;
    public String date;
    public int allow;

    public LatLng pos;

    public static final int TRUE = 1;
    public static final int FALSE = 0;

    public Friend(String name, String date, int allow, String id, LatLng location) {
        this.name = name;
        this.date = date;
        this.allow = allow;
        this.id = id;
        this.pos = location;
    }

    protected Friend(Parcel in) {
        name = in.readString();
        date = in.readString();
        allow = in.readInt();
    }

    public static final Creator<Friend> CREATOR = new Creator<Friend>() {
        @Override
        public Friend createFromParcel(Parcel in) {
            return new Friend(in);
        }

        @Override
        public Friend[] newArray(int size) {
            return new Friend[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(date);
        dest.writeInt(allow);
    }

    public void setState(boolean state) {
        if(state) allow = TRUE;
        else allow = FALSE;
    }
}
