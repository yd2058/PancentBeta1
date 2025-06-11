package com.example.pancentbeta1;

/**
 * @author		Yiftah Daid yd2058@bs.amalnet.k12.il
 * @version	    0.2.1
 * @since		22/12/2024
 * centralized activity to let user access all parts of the app easily
 */


import static com.example.pancentbeta1.Helpers.FBHelper.refauth;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pancentbeta1.Helpers.Calibration;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        menu.add("Home");
        menu.add("Logout");
        menu.add("Map");
        menu.add("Credits");
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        if(item.getTitle().toString().equals("Logout")){
            refauth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
        }
        else if(item.getTitle().toString().equals("home")){
            //pass
        }
        else if(item.getTitle().toString().equals("Map")){
            startActivity(new Intent(this, MapActivity.class));
        }
        else if(item.getTitle().toString().equals("Credits")){
            startActivity(new Intent(this, CreditsActivity.class));
        }

        else{
            return false;
        }



        return super.onOptionsItemSelected(item);
    }

    public void sPl(View view) {
        Intent intent = new Intent(this, CalibrationActivity.class);
        intent.putExtra("isLocation",true);
        startActivity(intent);

    }

    public void sPan(View view) {
        Intent intent = new Intent(this, CalibrationActivity.class);
        intent.putExtra("isLocation",false);
        startActivity(intent);
    }
}

