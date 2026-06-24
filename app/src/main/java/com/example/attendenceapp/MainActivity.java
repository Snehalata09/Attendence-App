package com.example.attendenceapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    MaterialButton btnGetStarted;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        btnGetStarted = findViewById(R.id.btnGetStarted);
        ImageView imgTopWave = findViewById(R.id.img_top_wave);
        ImageView imgBottomWave = findViewById(R.id.img_bottom_wave);
        ImageView imgLogo = findViewById(R.id.img_logo);
        TextView tvTitle = findViewById(R.id.tv_title);
        TextView tvSubtitle = findViewById(R.id.tv_subtitle);

        // Prepare animation starting states (invisible, translated/scaled)
        imgTopWave.setTranslationY(-300f);
        imgTopWave.setAlpha(0f);

        imgBottomWave.setTranslationY(300f);
        imgBottomWave.setAlpha(0f);

        imgLogo.setScaleX(0f);
        imgLogo.setScaleY(0f);
        imgLogo.setAlpha(0f);

        tvTitle.setTranslationY(100f);
        tvTitle.setAlpha(0f);

        tvSubtitle.setTranslationY(100f);
        tvSubtitle.setAlpha(0f);

        btnGetStarted.setScaleX(0.8f);
        btnGetStarted.setScaleY(0.8f);
        btnGetStarted.setAlpha(0f);

        // Run entrance animations
        imgTopWave.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(1000)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        imgBottomWave.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(1000)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        imgLogo.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(1200)
                .setStartDelay(200)
                .setInterpolator(new OvershootInterpolator(1.4f))
                .start();

        tvTitle.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        tvSubtitle.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(800)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        btnGetStarted.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(1000)
                .setStartDelay(1000)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();

        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
