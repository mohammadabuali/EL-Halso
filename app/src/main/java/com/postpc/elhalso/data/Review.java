package com.postpc.elhalso.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Review implements Parcelable , Comparable<Review> {
    private String businessID;
    private String  userID;
    private String userName;
    private String time;
    private float rating;
    private String text;

    public Review() {}

    public Review(String businessID, String userID, String userName, String time, float rating, String text) {
        this.businessID = businessID;
        this.userID = userID;
        this.userName = userName;
        this.rating = rating;
        this.text = text;
        this.time = time;
    }

    protected Review(Parcel in) {
        businessID = in.readString();
        userID = in.readString();
        userName = in.readString();
        time = in.readString();
        rating = in.readFloat();
        text = in.readString();
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    public String getBusinessID() {
        return businessID;
    }

    public String getText() {
        return text;
    }

    public float getRating() {
        return rating;
    }

    public String getTime() {
        return time;
    }

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(businessID);
        dest.writeString(userID);
        dest.writeString(userName);
        dest.writeString(time);
        dest.writeFloat(rating);
        dest.writeString(text);
    }

    @Override
    public int compareTo(Review other) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy;HH:mm:ss");
        try {
            Date d1 = format.parse(getTime());
            Date d2 = format.parse(other.getTime());
            return d1.compareTo(d2)*-1;
        } catch (ParseException e) {
            Log.e("Business", e.toString());
        }
        return 0;
    }
}
