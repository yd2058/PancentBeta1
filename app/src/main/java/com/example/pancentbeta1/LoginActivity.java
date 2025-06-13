package com.example.pancentbeta1;

/**
 * @author		Yiftah Daid yd2058@bs.amalnet.k12.il
 * @version	    0.2.1
 * @since		22/12/2024
 * activity to manage user login and/or registration using Firebase Authentication
 */


import static com.example.pancentbeta1.Helpers.FBHelper.refUsers;
import static com.example.pancentbeta1.Helpers.FBHelper.refauth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pancentbeta1.Helpers.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    EditText eTUname, eTem, eTpwd;
    String username, email, password;
    ToggleButton stayloggedin;
    Button logregbtn, regtogbtn;
    TextView titv;
    Intent gotohome;
    SharedPreferences sP;
    SharedPreferences.Editor sPeditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        eTUname = findViewById(R.id.eTUname);
        eTem = findViewById(R.id.eTem);
        eTpwd = findViewById(R.id.eTpwd);
        stayloggedin = findViewById(R.id.staylogtg);
        logregbtn = findViewById(R.id.logregbtn);
        regtogbtn = findViewById(R.id.regtogbtn);
        titv = findViewById(R.id.titv);
        gotohome = new Intent(this, HomeActivity.class);
        sP = getSharedPreferences("sp", MODE_PRIVATE);
        sPeditor = sP.edit();

        if(sP.getBoolean("loginNextTime", true)){
            refauth.signOut();
            sPeditor.putBoolean("loginNextTime", false);
            sPeditor.commit();
        }



        if(refauth.getCurrentUser()!= null){
            startActivity(gotohome);
        }





    }

    /**
     * @param view refers to the element clicked
     *
     * logs in the user using Firebase Authentication
     */
    public void login(View view) {
        email = eTem.getText().toString();
        password = eTpwd.getText().toString();
        if(!email.equals("") && !password.equals("")) {
            sPeditor.putBoolean("loginNextTime", stayloggedin.isChecked());
            sPeditor.commit();
            if (logregbtn.getText().equals("Login")) {
                refauth.signInWithEmailAndPassword(email,password);
                startActivity(gotohome);
            } else {
                username = eTUname.getText().toString();
                ProgressDialog pd = new ProgressDialog(this);
                pd.setTitle("Connecting");
                pd.setMessage("Creating User...");
                pd.show();
                refauth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        pd.dismiss();
                        if(task.isSuccessful()){
                            Log.i("FB", "Register Success");
                            FirebaseUser user = refauth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Successfully registered user"+user.getUid(), Toast.LENGTH_SHORT).show();
                            User currentUser = new User(username, email);
                            refUsers.child(currentUser.getUid()).setValue(currentUser);
                            startActivity(gotohome);
                        }
                        else{
                            Exception e = task.getException();
                            if(e instanceof FirebaseAuthInvalidUserException) Toast.makeText(LoginActivity.this, "Invalid Email", Toast.LENGTH_SHORT).show();
                            else if(e instanceof FirebaseAuthWeakPasswordException) Toast.makeText(LoginActivity.this, "Weak Password", Toast.LENGTH_SHORT).show();
                            else if(e instanceof FirebaseAuthUserCollisionException) Toast.makeText(LoginActivity.this, "User Already Exists", Toast.LENGTH_SHORT).show();
                            else if(e instanceof FirebaseAuthInvalidCredentialsException) Toast.makeText(LoginActivity.this, "Gnenral Auth failure", Toast.LENGTH_SHORT).show();
                            else if(e instanceof FirebaseNetworkException) Toast.makeText(LoginActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                            else Toast.makeText(LoginActivity.this, "Unknown Error. Try Again Later", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        else{Toast.makeText(this, "Email or password is missing!", Toast.LENGTH_SHORT).show();}

    }

    /**
     * @param view refers to the element clicked
     *
     * registers the user using Firebase Authentication
     */
    public void register(View view) {
        if(regtogbtn.getText().equals("Register")){
            titv.setText("Register");
            logregbtn.setText("Register User");
            regtogbtn.setText("Login");
            eTUname.setVisibility(View.VISIBLE);
        }
        else{
            titv.setText("Login");
            logregbtn.setText("Login");
            regtogbtn.setText("Register");
            eTUname.setVisibility(View.INVISIBLE);
        }
    }


    public void toglogtype(View view) {
        if(stayloggedin.isChecked()){
            sPeditor.putBoolean("loginNextTime", false);
        }
        else{
            sPeditor.putBoolean("loginNextTime", true);
        }

        sPeditor.commit();
    }
}