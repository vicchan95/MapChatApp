package edu.temple.mapchatapp;

import android.os.Parcel;
import android.os.Parcelable;

public class Partners implements Comparable<Partners>, Parcelable {
    private String username;
    private double latitude, longitude, distToUser;

    public Partners(String username, double latitude, double longitude, double distToUser){
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distToUser = distToUser;
    }

    protected Partners(Parcel in) {
        username = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        distToUser = in.readDouble();
    }

    public static final Creator<Partners> CREATOR = new Creator<Partners>() {
        @Override
        public Partners createFromParcel(Parcel in) {
            return new Partners(in);
        }

        @Override
        public Partners[] newArray(int size) {
            return new Partners[size];
        }
    };

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getDistToUser() {
        return distToUser;
    }

    public void setDistToUser(double distToUser) {
        this.distToUser = distToUser;
    }

    @Override
    public int compareTo(Partners o) {
        if(getDistToUser() > o.getDistToUser())
            return 1;
        else if (getDistToUser() == o.getDistToUser())
            return 0;
        else
            return -1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(distToUser);
    }
}
