package com.example.pancentbeta1.Helpers;

import android.location.Location;



import static com.example.pancentbeta1.Helpers.FBHelper.refauth;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Calibration {
    Location locationFOH;
    Location locationR;
    Location locationL;
    long calibrationTime; // yyyymmddhhmi
    boolean is_location_calibrated;
    String Uid;
    String photoAddress;

    public Calibration(Location locationFOH, Location locationR, Location locationL, String photoAddress){
        this.locationFOH = locationFOH;
        this.locationR = locationR;
        this.locationL = locationL;
        this.photoAddress = photoAddress;
        this.is_location_calibrated = locationL!=null;

        LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        this.calibrationTime = Long.parseLong(currentDateTime.format(formatter));

        this.Uid = refauth.getCurrentUser().getUid();
    }

}
