package com.example.pancentbeta1;

import static com.example.pancentbeta1.Helpers.FBHelper.refCals;
import static com.example.pancentbeta1.Helpers.FBHelper.refauth;
import static com.example.pancentbeta1.Helpers.FBHelper.refdb;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.pancentbeta1.Helpers.Calibration;
import com.google.android.libraries.places.api.Places;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    private GoogleMap mMap;
    private MapView mapView;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ProgressDialog Retrivieving_Location;


    int FindCalibration;
    List<LatLng> calibrationsLocation = new ArrayList<>();


    private static final String TAG = "Sign_Places";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        mapView = findViewById(R.id.mapView);
        if (mapView != null) {
            Bundle mapViewBundle = null;
            if (savedInstanceState != null) {
                mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
            }
            mapView.onCreate(mapViewBundle);
            mapView.getMapAsync(this);
        } else {
            Log.e(TAG, "mapView is null after findViewById");
        }

        Retrivieving_Location = ProgressDialog.show(this, "Map", "Retrieving location...", true);

        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize the Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCoOPzWZVFUkmZiBJQ2osN4HYCit4KV0pI");
        }

        // Request Location Permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE},
                    1);
        } else {
            getLocation();
            Retrivieving_Location.dismiss();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Enable My Location layer if permissions are granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        if (latitude != 0.0 && longitude != 0.0) {
            LatLng currentLocation = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14f));
        }
        Retrivieving_Location.dismiss();
    }

    private void getLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                // Request location updates
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (locationManager != null) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 3, this);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 3, this);
                }

                // Get last known location
                fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();

                                // Update map if it's ready
                                if (mMap != null) {
                                    LatLng currentLocation = new LatLng(latitude, longitude);
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14f));
                                }
                            }
                        });
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    // Handle location updates
    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    private void fetchCalibrationPlaces() {
        if (latitude == 0.0 && longitude == 0.0) {
            showAlertDialog("Current location not available.");
            return;
        }

        double radiusInMeters = 5000.0;

        String apiKey = "AIzaSyBFYlbpFPoon5R9C1oyIEVsICFgex7EcQw";

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
                + "?location=" + latitude + "," + longitude
                + "&radius=" + radiusInMeters
                + "&keyword=calibration+calibration"
                + "&type=sports_complex"
                + "&key=" + apiKey;

        Log.d(TAG, "Fetching calibration places with URL: " + url);
        final ProgressDialog pd = ProgressDialog.show(this, "Find Calibrations", "Searching...", true);
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                Log.d(TAG, "API Response: " + response.toString());

                String status = response.getString("status");
                if (!status.equals("OK")) {

                    showAlertDialog("API Error: " + status);
                    return;
                }

                JSONArray results = response.getJSONArray("results");
                Log.d(TAG, "Number of calibration places found: " + results.length());

                mMap.clear(); // Clear existing markers
                pd.dismiss();
                for (int i = 0; i < results.length(); i++) {
                    JSONObject place = results.getJSONObject(i);

                    // get place details
                    String placeName = place.getString("venueName");
                    JSONObject location = place.getJSONObject("locationFOH");
                    double lat = location.getDouble("0");
                    double lng = location.getDouble("1");

                    // add a marker for each place found
                    LatLng placeLatLng = new LatLng(lat, lng);
                    calibrationsLocation.add(placeLatLng);
                    mMap.addMarker(new MarkerOptions().position(placeLatLng).title(placeName));
                }

                refCals.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot caliSnapshot : snapshot.getChildren()) {
                            Calibration cal = caliSnapshot.getValue(Calibration.class);
                            if (cal != null && cal.getLocationFOH().get(0) != 0.0 && cal.getLocationFOH().get(1) != 0.0) {
                                double lat = cal.getLocationFOH().get(0);
                                double lng = cal.getLocationFOH().get(1);
                                LatLng calibrationLatLng = new LatLng(lat, lng);
                                Log.i(TAG, "Adding calibration marker: " + cal.getVenueName() + " at " + calibrationLatLng);
                                calibrationsLocation.add(calibrationLatLng);
                                mMap.addMarker(new MarkerOptions().position(calibrationLatLng).title(cal.getVenueName()));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                Toast.makeText(this, "Nearby calibration places added to the map.", Toast.LENGTH_SHORT).show();
                FindCalibration++;

            } catch (JSONException e) {
                Log.e(TAG, "JSON parsing error: ", e);
                showAlertDialog("Error parsing response data.");
            }
        },
                error -> {
                    Log.e(TAG, "Volley error: ", error);
                    showAlertDialog("Error fetching calibrationbasketball places.");
                }
        );

        queue.add(jsonObjectRequest);
    }


    public void Find_Place(View view) {
        fetchCalibrationPlaces();

    }

    // lifecycle for the map
    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null)
            mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null)
            mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        if (mapView != null)
            mapView.onSaveInstanceState(mapViewBundle);

        super.onSaveInstanceState(outState);
    }


    // permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            boolean allGranted = true;
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED)
                    allGranted = false;
            }
            if (allGranted) {
                getLocation();
                if (mapView != null)
                    mapView.getMapAsync(this);
            } else {
                showAlertDialog("Location permissions are required.");
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    private void showAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void go_back(View view) {
        finish();
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
            // Do nothing, already in MapActivity
        } else if (st.equals("Credits")) {
            startActivity(new Intent(this, CreditsActivity.class));
        } else {
            // Handle other menu items if needed
        }
        return super.onOptionsItemSelected(item);

    }
}