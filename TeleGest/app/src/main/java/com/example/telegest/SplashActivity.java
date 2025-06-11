package com.example.telegest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.example.telegest.R;
import com.example.telegest.FirebaseManager;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY = 2000; // 2 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            // Verificar si el usuario está autenticado
            if (FirebaseManager.getInstance().isUserLoggedIn()) {
                // Usuario autenticado, ir a MainActivity
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                // Usuario no autenticado, ir a LoginActivity
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, SPLASH_DELAY);
    }
}
