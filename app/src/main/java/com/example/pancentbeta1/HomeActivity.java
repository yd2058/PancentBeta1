package com.example.pancentbeta1;

/**
 * @author		Yiftah Daid yd2058@bs.amalnet.k12.il
 * @version	    0.2.1
 * @since		22/12/2024
 * centralized activity to let user access all parts of the app easily
 */


import static android.graphics.Color.BLUE;
import static android.graphics.Color.TRANSPARENT;
import static com.example.pancentbeta1.Helpers.FBHelper.refauth;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pancentbeta1.Helpers.Calibration;

/**
 * The type Home activity.
 */
public class HomeActivity extends AppCompatActivity {

    TextView hmTv;
    DownloadManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        hmTv = findViewById(R.id.hmTv);

        String part1 = "Welcome " + (refauth.getCurrentUser() != null ? refauth.getCurrentUser().getDisplayName() : "Guest") + "!\n";
        String part2 = "this is my experiment, it is woth noting that the speaker location based calibration is innacurate due to GPS inaccuracy.\n ";
        String clickableText = "click here";
        String part3 = " for the file for sound based calibration!";

        String fullText = part1 + part2 + clickableText + part3;

        SpannableString spannableString = new SpannableString(fullText);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = Uri.parse("https://drive.google.com/uc?export=download&id=1GHqb9AubBGPEr55sqCUkl5uYi9j1M6-z");
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                long reference = manager.enqueue(request);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true); // Make it look like a link
                ds.setColor(BLUE);     // Set link color to blue
            }
        };

        int startIndex = fullText.indexOf(clickableText);
        int endIndex = startIndex + clickableText.length();

        if (startIndex != -1) { // Ensure "click here" was found
            spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        hmTv.setText(spannableString);
        hmTv.setMovementMethod(LinkMovementMethod.getInstance()); // !IMPORTANT to make links clickable
        hmTv.setHighlightColor(TRANSPARENT); // Optional: removes the highlight color on click

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

    /**
     * S pl.
     *
     * @param view the view
     */
    public void sPl(View view) {
        Intent intent = new Intent(this, CalibrationActivity.class);
        intent.putExtra("isLocation",true);
        startActivity(intent);

    }

    /**
     * S pan.
     *
     * @param view the view
     */
    public void sPan(View view) {
        Intent intent = new Intent(this, CalibrationActivity.class);
        intent.putExtra("isLocation",false);
        startActivity(intent);
    }
}

