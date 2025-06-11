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

public class Calibration {
    public List<Double> locationFOH;
    public List<Double> locationR;
    public List<Double> locationL;
    public long calibrationTime; // yyyymmddhhmi
    public boolean locationCalibrated;
    public String uid;
    public String photoAddress;
    public String venueName;

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

    public Calibration() {

    }

    public List<Double> getLocationFOH() {
        return locationFOH;
    }

    public void setLocationFOH(List<Double> locationFOH) {
        this.locationFOH = locationFOH;
    }

    public List<Double> getLocationR() {
        return locationR;
    }

    public void setLocationR(List<Double> locationR) {
        this.locationR = locationR;
    }

    public List<Double> getLocationL() {
        return locationL;
    }

    public void setLocationL(List<Double> locationL) {
        this.locationL = locationL;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getCalibrationTime() {
        return calibrationTime;
    }

    public void setCalibrationTime(long calibrationTime) {
        this.calibrationTime = calibrationTime;
    }

    public boolean isLocationCalibrated() {
        return locationCalibrated;
    }

    public void setLocationCalibrated(boolean locationCalibrated) {
        this.locationCalibrated = locationCalibrated;
    }

    public String getPhotoAddress() {
        return photoAddress;
    }

    public void setPhotoAddress(String photoAddress) {
        this.photoAddress = photoAddress;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }
}
