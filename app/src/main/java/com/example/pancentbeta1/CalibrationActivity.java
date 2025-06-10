package com.example.pancentbeta1;

/**
 * @author		Yiftah Daid yd2058@bs.amalnet.k12.il
 * @version	    0.2.1
 * @since		22/12/2024
 * activity to calibrate and find the ideal listening spot for the user via GPS location or sound analysis(unavailable atm)
 */

import static com.example.pancentbeta1.Helpers.FBHelper.refCals;
import static com.example.pancentbeta1.Helpers.FBHelper.refauth;
import static com.example.pancentbeta1.Helpers.FBHelper.storef;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import android.Manifest;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.pancentbeta1.Helpers.Calibration;
import com.example.pancentbeta1.Helpers.LiveDbMeter;
import com.example.pancentbeta1.Helpers.LiveLogcatToFile;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class CalibrationActivity extends AppCompatActivity {
    FusedLocationProviderClient fLC;
    Location locationLeft, locationRight, locationFOH;

    SeekBar sBvert, sBhori;
    Button btnL, btnR, anabtn, savebtn, resbtn;



    double[] consoleCoordinates = new double[2];






    SharedPreferences sP;
    SharedPreferences.Editor sPeditor;

    int counter;
    String vName;
    boolean isLocation;
    private static CalibrationActivity ins;



    LiveDbMeter liveDbMeter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        Intent gi = getIntent();
        isLocation = gi.getBooleanExtra("isLocation",true);

        locationRight = null;
        locationLeft = null;
        consoleCoordinates[0] = -1;
        consoleCoordinates[1] = -1;

        sBvert = findViewById(R.id.sBvert);
        sBhori = findViewById(R.id.sBhori);
        btnL = findViewById(R.id.Lbtn);
        btnR = findViewById(R.id.Rbtn);
        anabtn = findViewById(R.id.anabtn);
        savebtn = findViewById(R.id.savebtn);
        resbtn = findViewById(R.id.resbtn);

        savebtn.setVisibility(View.INVISIBLE);

        fLC = LocationServices.getFusedLocationProviderClient(this);



        if(isLocation){
            sBvert.setVisibility(View.VISIBLE);
            btnL.setVisibility(View.VISIBLE);
            btnR.setVisibility(View.VISIBLE);

            anabtn.setVisibility(View.INVISIBLE);

            anabtn.setText("Test Location");
            resbtn.setText("Reset");




        }
        else{
            sBvert.setVisibility(View.INVISIBLE);
            btnL.setVisibility(View.INVISIBLE);
            btnR.setVisibility(View.INVISIBLE);

            anabtn.setVisibility(View.VISIBLE);

            anabtn.setText("Guide Panning");
            resbtn.setText("Pause");

            ins = this;





            liveDbMeter = new LiveDbMeter(this);
        }




    }

    @Override
    protected void onPause() {
        super.onPause();
        LiveLogcatToFile.startLogging(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        LiveLogcatToFile.startLogging(getApplicationContext());
    }

    public void reset(View view) {
        if(isLocation) {
            locationRight = null;
            locationLeft = null;
            consoleCoordinates[0] = -1;
            consoleCoordinates[1] = -1;
            btnL.setBackgroundColor(getResources().getColor(R.color.unapproving_Red));
            btnR.setBackgroundColor(getResources().getColor(R.color.unapproving_Red));
            sBvert.setProgress(0);
            sBhori.setProgress(0);
            sBvert.setThumb(DrawableCompat.wrap(Objects.requireNonNull(AppCompatResources.getDrawable(this, android.R.drawable.ic_media_ff))));
            sBhori.setThumb(DrawableCompat.wrap(Objects.requireNonNull(AppCompatResources.getDrawable(this, android.R.drawable.ic_media_ff))));
        }
        else{
            //pause recording
            liveDbMeter.stop();
        }
    }

    public void saveLeft(View view) {
        Log.i("FB", "saveLeft");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        fLC.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(this, location -> {
            if (location != null) {
                Toast.makeText(this, "Location Left registered", Toast.LENGTH_SHORT).show();

                if (locationLeft != null) {
                    if (location.distanceTo(locationLeft) != 0) {
                        locationRight = location;
                        btnL.setBackgroundColor(0xFF5e9732);
                        startGuide();
                    } else {
                        Toast.makeText(this, "You placed them both on the same location! \nPlease reset and try again", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    locationRight = location;
                    btnL.setBackgroundColor(0xFF5e9732);
                }
            }
            else{
                Log.i("LLOCATION", "no location");
            }
        });

    }

    public void saveRight(View view) {
        Log.i("FB", "saveRight");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        fLC.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(this, location -> {
            if (location != null) {
                Toast.makeText(this, "Location Right registered", Toast.LENGTH_SHORT).show();
                if (locationRight != null) {
                    if (location.distanceTo(locationRight) != 0) {
                        locationLeft = location;
                        btnR.setBackgroundColor(0xFF5e9732);
                        startGuide();
                    } else {
                        Toast.makeText(this, "You placed them both on the same location! \nPlease reset and try again", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    locationLeft = location;
                    btnR.setBackgroundColor(0xFF5e9732);
                }
            }
            else{
                Log.i("RLOCATION", "no location");
            }
        });
    }

    public void save(View view) {
        nameandpic();
    }

    public double[] consoleCoordinates(double xl, double yl, double xr, double yr, double distanceAB) {
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
        } else {
            System.out.println("error");
        }
        return consoleCoordinates;
    }

    public double y(double xc, double xl, double yl, double xr, double yr) {
        return ((((xl - xr) / (yr - yl)) * xc) + ((Math.pow(yr, 2) - Math.pow(yl, 2) - Math.pow(xl, 2) + Math.pow(xr, 2)) / (2 * (yr - yl))));
    }

    public double x1(double a, double b, double c) {
        return ((-b + Math.sqrt((b * b) - (4 * a * c))) / (2 * a));
    }

    public double x2(double a, double b, double c) {
        return ((-b - Math.sqrt((b * b) - (4 * a * c))) / (2 * a));
    }

    public static double distancePOverCM(double xw, double yw, double xc, double yc) {
        return d(xw, yw, xc, yc);
    }

    public static double distancePOverMC(double xw, double yw, double xr, double yr, double xl, double yl) {
        return d(xw, yw, (xl + xr) / 2, (yl + yr) / 2);
    }

    public static double distancePOverLR(double xn, double yn, double xl, double yl) {//distance of point w from point l
        return d(xn, yn, xl, yl);
    }

    public static double distancePOverRL(double xn, double yn, double xr, double yr) {//distance of point w from point l
        return d(xn, yn, xr, yr);
    }

    public static double d(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public static double[] coords(double xc, double yc, double xr, double yr, double xl, double yl, double xp, double yp) {
        double xn;
        double yn;
        double xw;
        double yw;
        //coordinates for M dot
        double xm = (xl + xr) / 2;
        double ym = (yl + yr) / 2;
        //slope of LR(MR) and WP
        double mmr = (yr - ym) / (xr - xm);
        //slope of CM and PN
        double mpn = -1 / mmr;
        //b of graph pn
        double bpn = yp - (mpn * xp);
        //b of graph mr
        double bmr = ym - (mmr * xm);
        //b of graph wp
        double bwp = yp - (mmr * xp);
        //b of graph cm
        double bcm = yc - (mpn * xc);
        //b of graph lr
        double blr= yl - (mmr * xl);
        //find the difference between LR and PW
        double dlrpw = (Math.abs(bwp-blr)/Math.sqrt(Math.pow(mmr,2)+1));
        //find the difference between MR and PN
        double dmrpn = (Math.abs(bpn-bmr)/Math.sqrt(Math.pow(mmr,2)+1));
        //difference in x's between lr and pw
        double diflrpw = Math.sqrt(Math.pow(blr-bwp,2)-dlrpw*dlrpw);
        //difference in x's between cm and pn
        double difmrpn = Math.sqrt(Math.pow(bcm-bpn,2)-dmrpn*dmrpn);
        //coordinates of n and w
        if(bwp>blr){
            xn = xp+diflrpw;
        }
        else{
            xn = xp-diflrpw;
        }
        if(bpn>bcm){
            xw = xc-difmrpn;
        }
        else {
            xw = xc + difmrpn;
        }
        yn = mpn * xn + bpn;
        yw = mmr * xw + bwp;
        return new double[]{xn, yn, xw, yw};
    }

    private void nameandpic() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        fLC.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(this, location -> {
            locationFOH = location;
                });
        if(!isLocation) liveDbMeter.stop();
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setCancelable(false);
        adb.setTitle("Name this location and take photo from console location");
        final EditText etname = new EditText(this);
        etname.setHint("Venue Name");
        adb.setView(etname);
        adb.setPositiveButton("Confirm & shoot", (dialog, which) -> {
            sP = getSharedPreferences("sp", MODE_PRIVATE);
            sPeditor = sP.edit();
            counter = sP.getInt("counter", 0) + 1;
            sPeditor.putInt("counter", counter);
            sPeditor.commit();
            vName = etname.getText().toString();
            Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePicIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePicIntent, 1);
            }
        });
        AlertDialog ad = adb.create();
        ad.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            uploadpic(imageBitmap);
            finish();
        }
    }

    private void uploadpic(Bitmap imageBitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        String address = "images/" + refauth.getCurrentUser().getUid() + "_" + counter + ".jpg";
        StorageReference ref = storef.child(address);

        Calibration curCal = new Calibration(locationFOH,locationLeft,locationRight,address, vName);
        refCals.child(vName+""+counter).setValue(curCal.toString());

        UploadTask tsk = ref.putBytes(data);
        tsk.addOnSuccessListener(taskSnapshot -> Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(this, "fail: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }


    public void startGuide() {
        Log.i("FB", "startGuide");
        if (locationRight != null && locationLeft != null) {

            sBvert.getThumb().setColorFilter(0xFFC41230, PorterDuff.Mode.SRC);
            sBhori.getThumb().setColorFilter(0xFFC41230, PorterDuff.Mode.SRC);
            consoleCoordinates = consoleCoordinates(locationRight.getLatitude(), locationRight.getLongitude(), locationLeft.getLatitude(), locationLeft.getLongitude(), locationRight.distanceTo(locationLeft));
            anabtn.setVisibility(View.VISIBLE);
            anabtn.setText("Update Location");


        }

    }

    public void guide(View view) {
        if(isLocation) {
            Log.i("FB", "continue");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            }
            fLC.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(this, location -> {
                Log.i("FB", "location");
                Toast.makeText(this, "ping", Toast.LENGTH_SHORT).show();
                if (location != null) {
                    double[] coordinates = coords(consoleCoordinates[0], consoleCoordinates[1], locationLeft.getLongitude(), locationLeft.getLatitude(), locationRight.getLongitude(), locationRight.getLatitude(), location.getLongitude(), location.getLatitude());
                    Log.i("loc", "xn = "+coordinates[0]+" yn = "+coordinates[1]+" xw = "+coordinates[2]+" yw = "+coordinates[3]);
                    double distanceLR = d(locationRight.getLongitude(), locationRight.getLatitude(), locationLeft.getLongitude(), locationLeft.getLatitude());
                    Log.i("loc", "distanceLR = "+distanceLR);
                    double distancePOverMC = distancePOverMC(coordinates[2], coordinates[3], locationLeft.getLongitude(), locationLeft.getLatitude(), locationRight.getLongitude(), locationRight.getLatitude());
                    Log.i("loc", "distancePOverMC = "+distancePOverMC);
                    double distancePOverCM = distancePOverCM(coordinates[2], coordinates[3], consoleCoordinates[0], consoleCoordinates[1]);
                    Log.i("loc", "distancePOverCM = "+distancePOverCM);
                    double distancePOverLR = distancePOverLR(coordinates[0], coordinates[1], locationRight.getLongitude(), locationRight.getLatitude());
                    Log.i("loc", "distancePOverLR = "+distancePOverLR);



                    sBhori.setProgress((int) ((distancePOverLR / distanceLR) * 100));
                    Log.i("loc", "sBhori.getProgress() = "+sBhori.getProgress());


                    sBvert.setProgress((int) (distancePOverCM / distanceLR));
                    Log.i("loc", "sBvert.getProgress() = "+sBvert.getProgress());

                    if (sBvert.getProgress() > 72 && sBhori.getProgress() < 78 && sBhori.getProgress() > 47 && sBhori.getProgress() < 53) {
                        sBvert.getThumb().setColorFilter(0xFF5e9732, PorterDuff.Mode.SRC);
                        savebtn.setVisibility(View.VISIBLE);
                    }

                }
            });
        }
        else{
            //start recording
            liveDbMeter.start();
        }


    }
    public static CalibrationActivity getInstance() {return ins;}

    public void updateRMS(double rmsprog) {
        CalibrationActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sBhori.setProgress((int) rmsprog);
                if(rmsprog>40&&rmsprog<60){
                    savebtn.setVisibility(View.VISIBLE);
                    sBhori.setThumb(DrawableCompat.wrap(Objects.requireNonNull(AppCompatResources.getDrawable(getInstance(), android.R.drawable.presence_online))));
                }
                else if (rmsprog<40){
                    savebtn.setVisibility(View.INVISIBLE);
                    sBhori.setThumb(DrawableCompat.wrap(Objects.requireNonNull(AppCompatResources.getDrawable(getInstance(), android.R.drawable.ic_media_ff))));
                }
                else if(rmsprog>60){
                    savebtn.setVisibility(View.INVISIBLE);
                    sBhori.setThumb(DrawableCompat.wrap(Objects.requireNonNull(AppCompatResources.getDrawable(getInstance(), android.R.drawable.ic_media_rew))));
                }
            }
        });
    }
}
