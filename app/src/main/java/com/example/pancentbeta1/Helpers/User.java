package com.example.pancentbeta1.Helpers;

import static com.example.pancentbeta1.Helpers.FBHelper.refauth;

import java.util.ArrayList;

/**
 * The type User.
 */
public class User {
    private String username;
    private String email;
    private String uid;
    private ArrayList<Calibration> pastcals;

    /**
     * Instantiates a new User.
     *
     * @param username the username
     * @param email    the email
     */
    public User(String username, String email){
        this.username = username;
        this.email = email;
        this.uid = refauth.getCurrentUser().getUid();
    }

    /**
     * Gets uid.
     *
     * @return the uid
     */
    public String getUid() {
        return uid;
    }

    /**
     * Gets email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets email.
     *
     * @param email the email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets uid.
     *
     * @param uid the uid
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Gets pastcals.
     *
     * @return the pastcals
     */
    public ArrayList<Calibration> getPastcals() {
        return pastcals;
    }

    /**
     * Sets pastcals.
     *
     * @param pastcals the pastcals
     */
    public void setPastcals(ArrayList<Calibration> pastcals) {
        this.pastcals = pastcals;
    }

    /**
     * Gets username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets username.
     *
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }
}
