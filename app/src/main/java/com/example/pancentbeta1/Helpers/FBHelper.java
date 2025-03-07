package com.example.pancentbeta1.Helpers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class FBHelper {
    public static final FirebaseAuth refauth = FirebaseAuth.getInstance();
    public static final FirebaseDatabase refdb = FirebaseDatabase.getInstance();
    public static final DatabaseReference refUsers = refdb.getReference("Users");
    public static final DatabaseReference refCals = refdb.getReference("Calibrations");
    public static final FirebaseFirestore refFBFS = FirebaseFirestore.getInstance();
}
