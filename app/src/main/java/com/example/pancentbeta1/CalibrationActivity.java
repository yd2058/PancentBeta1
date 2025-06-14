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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
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

/**
 * The type Calibration activity.
 */
public class CalibrationActivity extends AppCompatActivity {
    /**
     * The F lc.
     */
    FusedLocationProviderClient fLC;
    /**
     * The Location left.
     */
    Location locationLeft,
    /**
     * The Location right.
     */
    locationRight,
    /**
     * The Location foh.
     */
    locationFOH;

    /**
     * The S bvert.
     */
    SeekBar sBvert,
    /**
     * The S bhori.
     */
    sBhori;
    /**
     * The Btn l.
     */
    Button btnL,
    /**
     * The Btn r.
     */
    btnR,
    /**
     * The Anabtn.
     */
    anabtn,
    /**
     * The Savebtn.
     */
    savebtn,
    /**
     * The Resbtn.
     */
    resbtn;

    /**
     * The Console coordinates.
     */
    double[] consoleCoordinates = new double[2];

    /**
     * The S p.
     */
    SharedPreferences sP;
    /**
     * The S peditor.
     */
    SharedPreferences.Editor sPeditor;

    /**
     * The Counter.
     */
    int counter;
    /**
     * The V name.
     */
    String vName;
    /**
     * The Is location.
     */
    boolean isLocation;
    private static CalibrationActivity ins;

    /**
     * The Live db meter.
     */
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

    /**
     * Reset the calibration process.
     *
     * @param view the visual component initiated
     */
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

    /**
     * Save left speaker location.
     *
     * @param view the visual component initiated
     */
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

    /**
     * Save right speaker location.
     *
     * @param view the visual component initiated
     */
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

    /**
     * Save the calibration.
     *
     * @param view the visual component initiated
     */
    public void save(View view) {
        nameandpic();
    }

    /**
     * Console coordinates double [ ].
     *
     * @param xl         the xl
     * @param yl         the yl
     * @param xr         the xr
     * @param yr         the yr
     * @param distanceAB the distance ab
     * @return the coordinates of the balance location
     */
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

    /**
     * Y double.
     *
     * @param xc the xc
     * @param xl the xl
     * @param yl the yl
     * @param xr the xr
     * @param yr the yr
     * @return y value of the balance location
     */
    public double y(double xc, double xl, double yl, double xr, double yr) {
        return ((((xl - xr) / (yr - yl)) * xc) + ((Math.pow(yr, 2) - Math.pow(yl, 2) - Math.pow(xl, 2) + Math.pow(xr, 2)) / (2 * (yr - yl))));
    }

    /**
     * X 1 double.
     *
     * @param a the a
     * @param b the b
     * @param c the c
     * @return the possible x value of the balance location
     */
    public double x1(double a, double b, double c) {
        return ((-b + Math.sqrt((b * b) - (4 * a * c))) / (2 * a));
    }

    /**
     * X 2 double.
     *
     * @param a the a
     * @param b the b
     * @param c the c
     * @return the possible x value of the balance location
     */
    public double x2(double a, double b, double c) {
        return ((-b - Math.sqrt((b * b) - (4 * a * c))) / (2 * a));
    }

    /**
     * Distance p over cm double.
     *
     * @param xw the xw
     * @param yw the yw
     * @param xc the xc
     * @param yc the yc
     * @return the distance from point w from point c along cm
     */
    public static double distancePOverCM(double xw, double yw, double xc, double yc) {
        return d(xw, yw, xc, yc);
    }

    /**
     * Distance p over mc double.
     *
     * @param xw the xw
     * @param yw the yw
     * @param xr the xr
     * @param yr the yr
     * @param xl the xl
     * @param yl the yl
     * @return the distance from point w from point m along mc
     */
    public static double distancePOverMC(double xw, double yw, double xr, double yr, double xl, double yl) {
        return d(xw, yw, (xl + xr) / 2, (yl + yr) / 2);
    }

    /**
     * Distance p over lr double.
     *
     * @param xn the xn
     * @param yn the yn
     * @param xl the xl
     * @param yl the yl
     * @return the distance from point n to point l along lr
     */
    public static double distancePOverLR(double xn, double yn, double xl, double yl) {//distance of point w from point l
        return d(xn, yn, xl, yl);
    }

    /**
     * D double.
     *
     * @param x1 the x 1
     * @param y1 the y 1
     * @param x2 the x 2
     * @param y2 the y 2
     * @return the distance between two points in 2D space
     */
    public static double d(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    /**
     * Coords double [ ].
     *
     * @param xc the xc
     * @param yc the yc
     * @param xr the xr
     * @param yr the yr
     * @param xl the xl
     * @param yl the yl
     * @param xp the xp
     * @param yp the yp
     * @return the coordinates of the balance location and of points n and w
     */
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
        Log.i("FB", "uploadpic");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        String address = "images/" + refauth.getCurrentUser().getUid() + "_" + counter + ".jpg";
        StorageReference ref = storef.child(address);

        Calibration curCal = new Calibration(locationFOH,locationLeft,locationRight,address, vName);
        refCals.child(vName+""+counter).setValue(curCal).addOnSuccessListener(unused -> Log.i("upload succuss","yeepee")).addOnFailureListener(e -> Log.i("upload fail","fail: "+e.getMessage()));

        UploadTask tsk = ref.putBytes(data);
        tsk.addOnSuccessListener(taskSnapshot -> Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(this, "fail: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }


    /**
     * Start guide.
     */
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

    /**
     * reaction to the guide.
     *
     * @param view the visual component initiated
     */
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

    /**
     * Gets instance.
     *
     * @return the instance of this class
     */
    public static CalibrationActivity getInstance() {return ins;}

    /**
     * Update rms.
     *
     * @param rmsprog the progress of the guide
     */
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Home");
        menu.add("Logout");
        menu.add("Map");
        menu.add("Credits");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String st = item.getTitle() != null ? item.getTitle().toString().trim() : "";


        if (st.equals("Logout")) {
            refauth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
        } else if (st.equals("home")) {
            startActivity(new Intent(this, HomeActivity.class));
        }
        else if (st.equals("Map")) {
            startActivity(new Intent(this, MapActivity.class));
        } else if (st.equals("Credits")) {
            // Do nothing, already in CreditsActivity
        } else {
            // Handle other menu items if needed
        }
        return super.onOptionsItemSelected(item);

    }
}
