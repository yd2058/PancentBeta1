package com.example.pancentbeta1;

import static com.example.pancentbeta1.Helpers.FBHelper.refauth;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * The type Credits activity.
 */
public class CreditsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_credits);

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