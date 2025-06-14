package com.example.pancentbeta1.Helpers;

import android.location.Location;
import android.util.Log;


import static com.example.pancentbeta1.Helpers.FBHelper.refauth;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The type Calibration.
 */
public class Calibration {
    /**
     * The Location foh.
     */
    public List<Double> locationFOH;
    /**
     * The Location r.
     */
    public List<Double> locationR;
    /**
     * The Location l.
     */
    public List<Double> locationL;
    /**
     * The Calibration time.
     */
    public long calibrationTime; // yyyymmddhhmi
    /**
     * The Location calibrated.
     */
    public boolean locationCalibrated;
    /**
     * The Uid.
     */
    public String uid;
    /**
     * The Photo address.
     */
    public String photoAddress;
    /**
     * The Venue name.
     */
    public String venueName;

    /**
     * Instantiates a new Calibration.
     *
     * @param locationFOH  the location foh
     * @param locationR    the location r
     * @param locationL    the location l
     * @param photoAddress the photo address
     * @param venueName    the venue name
     */
    public Calibration(Location locationFOH, Location locationR, Location locationL, String photoAddress, String venueName) {
        this.locationFOH = new ArrayList<>();
        this.locationR = new ArrayList<>();
        this.locationL = new ArrayList<>();
        if (locationFOH != null) {

            this.locationFOH.add(locationFOH.getLatitude());
            this.locationFOH.add(locationFOH.getLongitude());
        } else {
            this.locationFOH.add(0.0);
            this.locationFOH.add(0.0); // or some default value
        }
        if (locationR != null) {
        this.locationR.add(locationR.getLatitude());
        this.locationR.add(locationR.getLongitude());
        }
        else {
            this.locationR.add(0.0);
            this.locationR.add(0.0); // or some default value
        }
        if (locationL != null) {
            this.locationL.add(locationL.getLatitude());
            this.locationL.add(locationL.getLongitude());
        }
        else {
            this.locationL.add(0.0);
            this.locationL.add(0.0); // or some default value
        }
        this.photoAddress = photoAddress;
        this.locationCalibrated = locationL!=null;
        this.venueName = venueName;

        LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        this.calibrationTime = Long.parseLong(currentDateTime.format(formatter));

        if (refauth != null && refauth.getCurrentUser() != null) {
            this.uid = refauth.getCurrentUser().getUid();
        } else {
            this.uid = null; // Or handle as an error, e.g., throw IllegalArgumentException
            Log.w("Calibration", "User not authenticated when creating Calibration object. UID set to null.");
        }
    }

    /**
     * Instantiates a new Calibration.
     */
    public Calibration() {

    }

    /**
     * Gets location foh.
     *
     * @return the location foh
     */
    public List<Double> getLocationFOH() {
        return locationFOH;
    }

    /**
     * Sets location foh.
     *
     * @param locationFOH the location foh
     */
    public void setLocationFOH(List<Double> locationFOH) {
        this.locationFOH = locationFOH;
    }

    /**
     * Gets location r.
     *
     * @return the location r
     */
    public List<Double> getLocationR() {
        return locationR;
    }

    /**
     * Sets location r.
     *
     * @param locationR the location r
     */
    public void setLocationR(List<Double> locationR) {
        this.locationR = locationR;
    }

    /**
     * Gets location l.
     *
     * @return the location l
     */
    public List<Double> getLocationL() {
        return locationL;
    }

    /**
     * Sets location l.
     *
     * @param locationL the location l
     */
    public void setLocationL(List<Double> locationL) {
        this.locationL = locationL;
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
     * Sets uid.
     *
     * @param uid the uid
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Gets calibration time.
     *
     * @return the calibration time
     */
    public long getCalibrationTime() {
        return calibrationTime;
    }

    /**
     * Sets calibration time.
     *
     * @param calibrationTime the calibration time
     */
    public void setCalibrationTime(long calibrationTime) {
        this.calibrationTime = calibrationTime;
    }

    /**
     * Is location calibrated boolean.
     *
     * @return the boolean
     */
    public boolean isLocationCalibrated() {
        return locationCalibrated;
    }

    /**
     * Sets location calibrated.
     *
     * @param locationCalibrated the location calibrated
     */
    public void setLocationCalibrated(boolean locationCalibrated) {
        this.locationCalibrated = locationCalibrated;
    }

    /**
     * Gets photo address.
     *
     * @return the photo address
     */
    public String getPhotoAddress() {
        return photoAddress;
    }

    /**
     * Sets photo address.
     *
     * @param photoAddress the photo address
     */
    public void setPhotoAddress(String photoAddress) {
        this.photoAddress = photoAddress;
    }

    /**
     * Gets venue name.
     *
     * @return the venue name
     */
    public String getVenueName() {
        return venueName;
    }

    /**
     * Sets venue name.
     *
     * @param venueName the venue name
     */
    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }
}
