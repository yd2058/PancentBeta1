package com.example.pancentbeta1.Helpers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * The type Fb helper.
 */
public class FBHelper {
    /**
     * The constant refauth.
     */
    public static final FirebaseAuth refauth = FirebaseAuth.getInstance();
    /**
     * The constant refdb.
     */
    public static final FirebaseDatabase refdb = FirebaseDatabase.getInstance();
    /**
     * The constant refUsers.
     */
    public static final DatabaseReference refUsers = refdb.getReference("Users");
    /**
     * The constant refCals.
     */
    public static final DatabaseReference refCals = refdb.getReference("Calibrations");
    /**
     * The constant storef.
     */
    public static final StorageReference storef = FirebaseStorage.getInstance().getReference();
}
