package com.example.pancentbeta1;

/**
 * @author		Yiftah Daid yd2058@bs.amalnet.k12.il
 * @version	    0.2.1
 * @since		22/12/2024
 * activity to calibrate and find the ideal listening spot for the user via GPS location or sound analysis(unavailable atm)
 */

import static com.example.pancentbeta1.Helpers.FBHelper.refauth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import android.Manifest;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class CalibrationActivity extends AppCompatActivity {
    FusedLocationProviderClient fLC;
    Location locationLeft, locationRight;

    SeekBar sBvert, sBhori;
    Button btnL, btnR;

    int presconf;

    double[] consoleCoordinates = new double[2];


    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private static final String TAG = "CalibrationActivity";
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    SharedPreferences sP;
    SharedPreferences.Editor sPeditor;
    StorageReference storef;
    int counter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        locationLeft = null;
        locationRight = null;
        consoleCoordinates[0] = -1;
        consoleCoordinates[1] = -1;

        presconf = 24;

        sBvert = findViewById(R.id.sBvert);
        sBhori = findViewById(R.id.sBhori);
        btnL = findViewById(R.id.Lbtn);
        btnR = findViewById(R.id.Rbtn);

        fLC = LocationServices.getFusedLocationProviderClient(this);
        storef = FirebaseStorage.getInstance().getReference();

    }


    public void reset(View view) {
        locationLeft = null;
        locationRight = null;
        consoleCoordinates[0] = -1;
        consoleCoordinates[1] = -1;
        btnL.setBackgroundColor(getResources().getColor(R.color.unapproving_Red));
        btnR.setBackgroundColor(getResources().getColor(R.color.unapproving_Red));
        sBvert.setProgress(0);
        sBhori.setProgress(0);
        sBvert.getThumb().setColorFilter(0xFF625b71, PorterDuff.Mode.SRC);
        sBhori.getThumb().setColorFilter(0xFF625b71, PorterDuff.Mode.SRC);
    }

    public void saveLeft(View view) {
        checkAndRequestLocationPermissions(this, 1);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fLC.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(this, location -> {
            if (location != null) {
                if (locationRight != null) {
                    if (!locationLeft.equals(locationRight)) {
                        locationLeft = location;
                        btnL.setBackgroundColor(getResources().getColor(R.color.approving_Green));
                    }
                    else{Toast.makeText(this, "You placed them both on the same location! \nPlease reset and try again", Toast.LENGTH_SHORT).show();}
                }
                else{
                    locationLeft = location;
                    btnL.setBackgroundColor(getResources().getColor(R.color.approving_Green));
                }
            }
        });
        startGuide();
    }

    public void saveRight (View view){
        checkAndRequestLocationPermissions(this, 1);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fLC.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(this, location -> {
            if (location != null) {
                if (locationLeft != null) {
                    if (!locationLeft.equals(locationRight)) {
                        locationRight = location;
                        btnR.setBackgroundColor(getResources().getColor(R.color.approving_Green));
                    }
                    else{Toast.makeText(this, "You placed them both on the same location! \nPlease reset and try again", Toast.LENGTH_SHORT).show();}
                }
                else{
                    locationRight = location;
                    btnR.setBackgroundColor(getResources().getColor(R.color.approving_Green));
                }
            }
        });
        startGuide();
    }

    public void save (View view){
    }


    //Utility functions
    public void startGuide() {
        if(locationLeft != null&& locationRight != null){
            sBvert.getThumb().setColorFilter(0xFFC41230, PorterDuff.Mode.SRC);
            sBhori.getThumb().setColorFilter(0xFFC41230, PorterDuff.Mode.SRC);
            consoleCoordinates = consoleCoordinates(locationLeft.getLatitude(), locationLeft.getLongitude(), locationRight.getLatitude(), locationRight.getLongitude(), locationLeft.distanceTo(locationRight));
            
            
            createLocationRequest();
            createLocationCallback();
            if(checkLocationPermission()){
                startLocationUpdates();
            }else{
                requestLocationPermission();
            }
        }
        

    }

    public boolean checkAndRequestLocationPermissions (Activity activity,int requestCode){
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (allPermissionsGranted) {
            // Permissions are granted
            return true;
        } else {
            // Permissions are not granted, request them
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
            return false;
        }
    }
    public double[] consoleCoordinates ( double xl, double yl, double xr, double yr, double distanceAB){
        double[] consoleCoordinates = new double[2];
        double xc1 = (x1(4, -4 * (xl + xr), (xl + xr) * (xl + xr) - 3 * (yr - yl) * (yr - yl)));
        double xc2 = (x2(4, -4 * (xl + xr), (xl + xr) * (xl + xr) - 3 * (yr - yl) * (yr - yl)));
        if (yr == yl && xr != xl) {
            consoleCoordinates[0] = (xl + xr) / 2;
            if (xr > xl) {
                consoleCoordinates[1] = yr - Math.sqrt((3 * distanceAB) / 4);
            } else if (xl > xr) {
                consoleCoordinates[1] = yr + Math.sqrt((3 * distanceAB) / 4);
            }
        } else if (yr != yl && xr == xl) {
            consoleCoordinates[1] = (yr + yl) / 2;
            if (yr > yl) {
                consoleCoordinates[0] = Math.max(xc1, xc2);
            } else if (yl > yr) {
                consoleCoordinates[0] = Math.min(xc1, xc2);
            }
        } else if (yr != yl && xr != xl) {
            if (yr > yl) {
                consoleCoordinates[0] = xc1;
            } else if (yl > yr) {
                consoleCoordinates[0] = xc2;
            }
            y(consoleCoordinates[0], xl, yl, xr, yr);
        }
        else {System.out.println("error");}
        return consoleCoordinates;
        }
    public double y ( double xc, double xl, double yl, double xr, double yr){return ((((xl - xr) / (yr - yl)) * xc) + ((Math.pow(yr, 2) - Math.pow(yl, 2) - Math.pow(xl, 2) + Math.pow(xr, 2)) / (2 * (yr - yl))));}
    public double x1 ( double a, double b, double c){return ((-b + Math.sqrt((b * b) - (4 * a * c))) / (2 * a));}
    public double x2 ( double a, double b, double c){return ((-b - Math.sqrt((b * b) - (4 * a * c))) / (2 * a));}

    public static double distancePOverCM(double xw, double yw, double xc, double yc){
        return d(xw,yw,xc,yc);
    }

    public static double distancePOverMC(double xw, double yw, double xr, double yr, double xl, double yl){
        return d(xw,yw,(xl+xr)/2,(yl+yr)/2);
    }

    public static double distancePOverLR(double xn, double yn, double xl, double yl){//distance of point w from point l
        return d(xn,yn,xl,yl);
    }
    public static double distancePOverRL(double xn, double yn, double xr, double yr){//distance of point w from point l
        return d(xn,yn,xr,yr);
    }

    public static double d(double x1, double y1, double x2, double y2){
        return Math.sqrt(Math.pow(x1-x2,2)+Math.pow(y1-y2,2));
    }

    public static double[] coords(double xc, double yc, double xr, double yr, double xl, double yl,double xp,double yp){
        //coordinates for M dot
        double xm = (xl+xr)/2;
        double ym = (yl+yr)/2;
        //slope of LR(MR) and WP
        double mmr = (yr-ym)/(xr-xm);
        //slope of CM and PN
        double mpn = -1/mmr;
        //b of graph pn
        double bpn = yp - (mpn*xp);
        //b of graph mr
        double bmr = ym - (mmr*xm);
        //b of graph wp
        double bwp = yp - (mmr*xp);
        //b of graph cm
        double bcm = yc - (mpn*xc);
        //coordinates of n and w
        double xn = (bmr-bpn)/(mpn-mmr);
        double yn = mpn*xn + bpn;
        double xw = (bcm-bwp)/(mmr-mpn);
        double yw = mmr*xw + bwp;
        return new double[]{xn,yn,xw,yw};
    }


    private void createLocationRequest() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 250)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(250)
                .setMaxUpdateDelayMillis(250)
                .build();
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    double[] coordinates = coords(consoleCoordinates[0], consoleCoordinates[1], locationRight.getLongitude(), locationRight.getLatitude(), locationLeft.getLongitude(), locationLeft.getLatitude(), location.getLongitude(), location.getLatitude());
                    double distanceLR = locationLeft.distanceTo(locationRight);
                    double distancePOverMC = distancePOverMC(coordinates[2], coordinates[3], locationRight.getLongitude(), locationRight.getLatitude(), locationLeft.getLongitude(), locationLeft.getLatitude());
                    double distancePOverCM = distancePOverCM(coordinates[2], coordinates[3], consoleCoordinates[0], consoleCoordinates[1]);
                    double distancePOverLR = distancePOverLR(coordinates[0], coordinates[1], locationLeft.getLongitude(), locationLeft.getLatitude());
                    double distancePOverRL = distancePOverRL(coordinates[0], coordinates[1], locationRight.getLongitude(), locationRight.getLatitude());
                    if(distancePOverLR + distancePOverRL!=distanceLR) {
                        if (distancePOverLR < distancePOverRL) {
                        sBhori.setProgress(5 - (int) ((distancePOverLR / distanceLR) * 5));
                        }
                        else if (distancePOverLR > distancePOverRL) {
                        sBhori.setProgress(95 + (int) ((distancePOverRL / distanceLR) * 5));
                        }
                    }
                    else{
                        sBhori.setProgress((int) (5 + ((distancePOverLR/distanceLR)*90)));
                    }
                    if(distancePOverCM + distancePOverMC!=Math.sqrt(Math.pow(distanceLR,2)*0.75)) {
                        if (distancePOverCM < distancePOverMC) {
                            sBvert.setProgress(0);
                        }
                        else if (distancePOverCM > distancePOverMC) {
                            sBvert.setProgress((int) ((distancePOverCM/distanceLR)*100));
                        }
                    }
                    else{
                        sBvert.setProgress((int) ((distancePOverCM/distanceLR)*100));
                    }
                    if(sBhori.getProgress()>47&&sBhori.getProgress()<53) {sBhori.getThumb().setColorFilter(0xFF5e9732, PorterDuff.Mode.SRC); presconf--;}
                    else {sBhori.getThumb().setColorFilter(0xFF625b71, PorterDuff.Mode.SRC); presconf=24;}
                    if(sBvert.getProgress()>72&&sBhori.getProgress()<78) {sBvert.getThumb().setColorFilter(0xFF5e9732, PorterDuff.Mode.SRC); presconf--;}
                    else {sBvert.getThumb().setColorFilter(0xFF625b71, PorterDuff.Mode.SRC); presconf=24;}
                    if (presconf <= 0) {
                        stopLocationUpdates();
                        nameandpic();
                    }
                }
            }
        };
    }

    private void nameandpic() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setCancelable(false);
        adb.setTitle("Name this location and take photo from console location");
        final EditText etname = new EditText(this);
        etname.setHint("Venue Name");
        adb.setView(etname);
        adb.setPositiveButton("Confirm & shoot", (dialog, which) -> {
            sP = getSharedPreferences("sp", MODE_PRIVATE);
            sPeditor = sP.edit();
            counter = sP.getInt("counter", 0)+1;
            sPeditor.putInt("counter", counter);
            sPeditor.commit();
            Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(takePicIntent.resolveActivity(getPackageManager())!=null){
                startActivityForResult(takePicIntent,1);
            }
        });
        AlertDialog ad = adb.create();
        ad.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            uploadpic(imageBitmap);
            finish();
        }
    }

    private void uploadpic(Bitmap imageBitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] data = baos.toByteArray();
        StorageReference ref = storef.child("images/"+refauth.getCurrentUser().getUid()+"_"+counter+".jpg");
        UploadTask tsk = ref.putBytes(data);
        tsk.addOnSuccessListener(taskSnapshot -> Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(this, "fail: "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fLC.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkLocationPermission()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        fLC.removeLocationUpdates(locationCallback);
    }
}
