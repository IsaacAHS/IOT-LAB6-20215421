package com.example.telegest;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseManager {
    private static FirebaseManager instance;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private FirebaseManager() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    // Authentication methods
    public FirebaseAuth getAuth() {
        return mAuth;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        FirebaseUser user = getCurrentUser();
        return user != null && user.isEmailVerified();
    }

    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // Firestore methods
    public FirebaseFirestore getFirestore() {
        return db;
    }

    public void signOut() {
        mAuth.signOut();
        // También cerrar sesión en proveedores sociales
        LoginManager.getInstance().logOut();
    }
}