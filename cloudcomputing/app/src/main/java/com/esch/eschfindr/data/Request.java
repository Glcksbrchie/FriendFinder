package com.esch.eschfindr.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Request implements Parcelable {

    public String name;
    public String uid;
    public String id;

    public Request(String name, String uid, String id) {
        this.name = name;
        this.uid = uid;
        this.id = id;
    }

    protected Request(Parcel in) {
        name = in.readString();
        uid = in.readString();
    }

    public static final Creator<Request> CREATOR = new Creator<Request>() {
        @Override
        public Request createFromParcel(Parcel in) {
            return new Request(in);
        }

        @Override
        public Request[] newArray(int size) {
            return new Request[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(uid);
    }
}
