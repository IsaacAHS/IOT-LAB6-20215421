package com.example.telegest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.example.telegest.R;
import com.example.telegest.Usuario;
import com.example.telegest.FirebaseManager;
import com.example.telegest.ValidationUtils;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;

    // Views
    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogleSignIn;
    private LoginButton btnFacebookLogin;
    private TextView tvRegister, tvForgotPassword;
    private ProgressBar progressBar;
    private ImageView ivLogo;

    // Firebase
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        setupFirebase();
        setupGoogleSignIn();
        setupFacebookSignIn();
        setupClickListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoogleSignIn = findViewById(R.id.btn_google_signin);
        btnFacebookLogin = findViewById(R.id.btn_facebook_login);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        progressBar = findViewById(R.id.progress_bar);
        ivLogo = findViewById(R.id.iv_logo);
    }

    private void setupFirebase() {
        mAuth = FirebaseManager.getInstance().getAuth();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupFacebookSignIn() {
        mCallbackManager = CallbackManager.Factory.create();

        btnFacebookLogin.setPermissions("email", "public_profile");
        btnFacebookLogin.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                hideProgressBar();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                hideProgressBar();
                showError("Error al iniciar sesión con Facebook");
            }
        });
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> loginWithEmail());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
        tvForgotPassword.setOnClickListener(v -> resetPassword());
    }

    private void loginWithEmail() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError("Ingrese un email válido");
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        showProgressBar();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            if (user.isEmailVerified()) {
                                loginSuccess();
                            } else {
                                hideProgressBar();
                                showError("Por favor verifica tu email antes de continuar");
                                mAuth.signOut();
                            }
                        }
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        hideProgressBar();
                        showError("Error al iniciar sesión. Verifica tus credenciales.");
                    }
                });
    }

    private void signInWithGoogle() {
        showProgressBar();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        showProgressBar();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Usuario nuevo o existente con Facebook
                            saveUserToFirestore(user, "facebook");
                            loginSuccess();
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        hideProgressBar();
                        showError("Error al autenticar con Facebook");
                    }
                });
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();

        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError("Ingrese un email válido");
            return;
        }

        showProgressBar();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    hideProgressBar();
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this,
                                "Se ha enviado un email para restablecer tu contraseña",
                                Toast.LENGTH_LONG).show();
                    } else {
                        showError("Error al enviar email de recuperación");
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser user, String provider) {
        Usuario usuario = new Usuario();
        usuario.setUid(user.getUid());
        usuario.setEmail(user.getEmail());
        usuario.setNombre(user.getDisplayName() != null ? user.getDisplayName() : "Usuario");
        usuario.setPhotoUrl(user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
        usuario.setProvider(provider);

        FirebaseManager.getInstance().getFirestore()
                .collection("usuarios")
                .document(user.getUid())
                .set(usuario)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Usuario guardado en Firestore"))
                .addOnFailureListener(e -> Log.w(TAG, "Error al guardar usuario", e));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Resultado de Google Sign-In
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                hideProgressBar();
                showError("Error al iniciar sesión con Google");
            }
        }

        // Resultado de Facebook
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Usuario nuevo o existente con Google
                            saveUserToFirestore(user, "google");
                            loginSuccess();
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        hideProgressBar();
                        showError("Error al autenticar con Google");
                    }
                });
    }

    private void loginSuccess() {
        hideProgressBar();
        Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        btnGoogleSignIn.setEnabled(false);
        btnFacebookLogin.setEnabled(false);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        btnLogin.setEnabled(true);
        btnGoogleSignIn.setEnabled(true);
        btnFacebookLogin.setEnabled(true);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}