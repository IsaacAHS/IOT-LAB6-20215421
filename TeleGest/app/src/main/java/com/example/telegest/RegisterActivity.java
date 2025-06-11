package com.example.telegest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.telegest.R;
import com.example.telegest.Usuario;
import com.example.telegest.FirebaseManager;
import com.example.telegest.ValidationUtils;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    // Views
    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();
        setupFirebase();
        setupClickListeners();
    }

    private void initializeViews() {
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupFirebase() {
        mAuth = FirebaseManager.getInstance().getAuth();
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validaciones
        if (name.isEmpty()) {
            etName.setError("Ingrese su nombre");
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError("Ingrese un email válido");
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            return;
        }

        showProgressBar();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Enviar email de verificación
                            sendEmailVerification(user, name);
                            // Guardar datos adicionales del usuario
                            saveUserToFirestore(user, name);
                        }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        hideProgressBar();
                        showError("Error al crear la cuenta. El email puede estar en uso.");
                    }
                });
    }

    private void sendEmailVerification(FirebaseUser user, String name) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    hideProgressBar();
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this,
                                "Cuenta creada exitosamente. Se ha enviado un email de verificación a " + user.getEmail(),
                                Toast.LENGTH_LONG).show();

                        // Cerrar sesión hasta que verifique el email
                        mAuth.signOut();

                        // Volver al login
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        showError("Error al enviar email de verificación");
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser user, String name) {
        Usuario usuario = new Usuario();
        usuario.setUid(user.getUid());
        usuario.setEmail(user.getEmail());
        usuario.setNombre(name);
        usuario.setPhotoUrl("");
        usuario.setProvider("email");

        FirebaseManager.getInstance().getFirestore()
                .collection("usuarios")
                .document(user.getUid())
                .set(usuario)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Usuario guardado en Firestore"))
                .addOnFailureListener(e -> Log.w(TAG, "Error al guardar usuario", e));
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        btnRegister.setEnabled(true);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}