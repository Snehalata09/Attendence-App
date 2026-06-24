package com.example.attendenceapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    EditText loginEmail, loginPassword;
    MaterialButton btnLogin;
    TextView txtRegister, txtForgotPassword;
    DatabaseHelper db;
    SessionManager sessionManager;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Handle Edge-to-Edge window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginEmail = findViewById(R.id.loginemail);
        loginPassword = findViewById(R.id.loginpassword);

        btnLogin = findViewById(R.id.btnlogin);

        txtRegister = findViewById(R.id.txtregister);
        txtForgotPassword = findViewById(R.id.txtforgotpassword);

        db = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        mAuth = FirebaseAuth.getInstance();

        if (sessionManager.isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        }

        // Clear input fields (useful when returning after logout)
        loginEmail.setText("");
        loginPassword.setText("");

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = loginEmail.getText().toString().trim();
                String password = loginPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {

                    loginEmail.setError("Enter Email");
                    loginEmail.requestFocus();
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                    loginEmail.setError("Invalid Email");
                    loginEmail.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(password)) {

                    loginPassword.setError("Enter Password");
                    loginPassword.requestFocus();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, task -> {
                            if (task.isSuccessful()) {
                                sessionManager.setLogin(true, email);
                                Toast.makeText(LoginActivity.this,
                                        "Login Successful",
                                        Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(LoginActivity.this,
                                        DashboardActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this,
                                        "Use proper credentials",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgotPasswordDialog();
            }
        });

        // Set up premium entry animations
        ImageView imgTopWave = findViewById(R.id.img_top_wave);
        ImageView imgBottomWave = findViewById(R.id.img_bottom_wave);
        ImageView imgLogo = findViewById(R.id.img_logo);
        TextView txtTitle = findViewById(R.id.txttitle);
        View cardForm = findViewById(R.id.card_form);

        if (imgTopWave != null) {
            imgTopWave.setTranslationY(-200f);
            imgTopWave.setAlpha(0f);
            imgTopWave.animate().translationY(0f).alpha(1f).setDuration(800).start();
        }
        if (imgBottomWave != null) {
            imgBottomWave.setTranslationY(200f);
            imgBottomWave.setAlpha(0f);
            imgBottomWave.animate().translationY(0f).alpha(1f).setDuration(800).start();
        }
        if (imgLogo != null) {
            imgLogo.setScaleX(0f);
            imgLogo.setScaleY(0f);
            imgLogo.setAlpha(0f);
            imgLogo.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(800).setStartDelay(200)
                    .setInterpolator(new android.view.animation.OvershootInterpolator()).start();
        }
        if (txtTitle != null) {
            txtTitle.setTranslationY(50f);
            txtTitle.setAlpha(0f);
            txtTitle.animate().translationY(0f).alpha(1f).setDuration(600).setStartDelay(400).start();
        }
        if (cardForm != null) {
            cardForm.setTranslationY(150f);
            cardForm.setAlpha(0f);
            cardForm.animate().translationY(0f).alpha(1f).setDuration(800).setStartDelay(500)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
        }
    }

    private void showForgotPasswordDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        bottomSheetDialog.setContentView(dialogView);

        EditText etEmail = dialogView.findViewById(R.id.etResetEmail);
        MaterialButton btnReset = dialogView.findViewById(R.id.btnResetPassword);

        // Autofill email from login field if user entered it
        String currentEmail = loginEmail.getText().toString().trim();
        if (!currentEmail.isEmpty()) {
            etEmail.setText(currentEmail);
        }

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    etEmail.setError("Enter registered Email");
                    etEmail.requestFocus();
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.setError("Invalid Email");
                    etEmail.requestFocus();
                    return;
                }

                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                bottomSheetDialog.dismiss();
                                showSuccessDialog("Link Sent", "A password reset email has been sent. Please check your inbox!");
                            } else {
                                Toast.makeText(LoginActivity.this, "Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        bottomSheetDialog.show();
    }

    private void showSuccessDialog(String title, String message) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_success_popup, null);
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvSuccessTitle);
        TextView tvMsg = dialogView.findViewById(R.id.tvSuccessMessage);
        MaterialButton btnOk = dialogView.findViewById(R.id.btnSuccessOk);

        tvTitle.setText(title);
        tvMsg.setText(message);

        btnOk.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}