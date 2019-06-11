package com.example.sozoapp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Groups implements Parcelable {

    private String title;
    private String groupId;


    public Groups(String title, String groupId) {
        this.title = title;
        this.groupId = groupId;
    }

    public Groups() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        return "Groups{" +
                "title='" + title + '\'' +
                ", groupId='" + groupId + '\'' +
                '}';
    }

    protected Groups(Parcel in) {
        title = in.readString();
        groupId = in.readString();
    }

    public static final Creator<Groups> CREATOR = new Creator<Groups>() {
        @Override
        public Groups createFromParcel(Parcel in) {
            return new Groups(in);
        }

        @Override
        public Groups[] newArray(int size) {
            return new Groups[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(groupId);
    }
}
