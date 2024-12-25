package com.example.pancentbeta1.Helpers;

import static com.example.pancentbeta1.Helpers.FBHelper.refauth;

import java.util.ArrayList;

public class User {
    private String username;
    private String email;
    private String uid;
    private ArrayList<Calibration> pastcals;

    public User(String username, String email){
        this.username = username;
        this.email = email;
        this.uid = refauth.getCurrentUser().getUid();
    }

    public String getUid() {
        return uid;
    }
    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public ArrayList<Calibration> getPastcals() {
        return pastcals;
    }
    public void addCalibration(Calibration c){
        pastcals.add(c);
    }


}
