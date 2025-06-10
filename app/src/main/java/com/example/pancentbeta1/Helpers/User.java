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



}
