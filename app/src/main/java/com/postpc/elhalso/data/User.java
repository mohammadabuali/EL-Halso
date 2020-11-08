package com.postpc.elhalso.data;

import java.util.ArrayList;

public class User {
    private String id;
    private String name;
    private String email;
    private String businessID;
    private ArrayList<String> favorites;
    private double radius;
    private boolean firstLogin;
    public User() { }

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public User(String id, String name, String email, String businessID, ArrayList<String> favorites, double radius, boolean firstLogin) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.businessID = businessID;
        this.favorites = favorites;
        this.radius = radius;
        this.firstLogin = firstLogin;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getBusinessID() {
        return businessID;
    }

    public ArrayList<String> getFavorites() {
        return favorites == null ? new ArrayList<>() : favorites;
    }

    public double getRadius() {
        return radius;
    }

    public boolean isFirstLogin() {
        return firstLogin;
    }

    public void addFavoriteBusiness(Business business){
        favorites = getFavorites();
        if(!favorites.contains(business.getId()))
            favorites.add(business.getId());
    }

    public void removeFavoriteBusiness(Business business){
        favorites = getFavorites();
        favorites.remove(business.getId());
    }

    public void setBusinessID(String businessID) {
        this.businessID = businessID;
    }

    public void setFavorites(ArrayList<String> favorites) {
        this.favorites = favorites;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setFirstLogin(boolean firstLogin) {
        this.firstLogin = firstLogin;
    }
}
