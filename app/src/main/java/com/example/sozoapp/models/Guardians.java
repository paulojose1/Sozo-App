package com.example.sozoapp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Guardians implements Parcelable {

    private String guardianName;
    private String guardianId;

    public Guardians() {
    }

    public Guardians(String guardianName, String guardianId) {
        this.guardianName = guardianName;
        this.guardianId = guardianId;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }


    public String getGuardianId() {
        return guardianId;
    }

    public void setGuardianId(String guardianId) {
        this.guardianId = guardianId;
    }

    @Override
    public String toString() {
        return "Guardians{" +
                "guardianName='" + guardianName + '\'' +
                ", guardianId='" + guardianId + '\'' +
                '}';
    }

    protected Guardians(Parcel in) {
    }

    public static final Creator<Guardians> CREATOR = new Creator<Guardians>() {
        @Override
        public Guardians createFromParcel(Parcel in) {
            return new Guardians(in);
        }

        @Override
        public Guardians[] newArray(int size) {
            return new Guardians[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
