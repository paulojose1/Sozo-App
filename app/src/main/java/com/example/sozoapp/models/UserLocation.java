package com.example.sozoapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class UserLocation implements Parcelable
{


    private CreateUser user;
    private GeoPoint geo_point;
    private @ServerTimestamp
    Date timeStamp;




    public UserLocation( CreateUser user, GeoPoint geo_point, Date timeStamp) {

        this.user = user;
        this.geo_point = geo_point;
        this.timeStamp = timeStamp;
    }

    public UserLocation() {

    }


    public CreateUser getUser() {
        return user;
    }

    public void setUser(CreateUser user) {
        this.user = user;
    }


    public GeoPoint getGeo_point() {
        return geo_point;
    }

    public void setGeo_point(GeoPoint geo_point) {
        this.geo_point = geo_point;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "UserLocation{" +
                "user=" + user +
                ", geo_point=" + geo_point +
                ", timeStamp=" + timeStamp +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
