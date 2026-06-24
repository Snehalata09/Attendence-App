package com.example.attendenceapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    EditText Email1, Password1;
    TextView treg, txtlog, sclass;
    MaterialButton btn;
    CheckBox c1, c2, c3, c4, c5;
    DatabaseHelper db;
    SessionManager sessionManager;
    FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Handle Edge-to-Edge window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Email1 = findViewById(R.id.email);
        Password1 = findViewById(R.id.password);
        treg = findViewById(R.id.teacher);
        txtlog = findViewById(R.id.txtlogin);
        sclass = findViewById(R.id.selectclass);
        btn = findViewById(R.id.mbtn);
        c1 = findViewById(R.id.check1);
        c2 = findViewById(R.id.check2);
        c3 = findViewById(R.id.check3);
        c4 = findViewById(R.id.check4);
        c5 = findViewById(R.id.check5);

        db = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        mAuth = FirebaseAuth.getInstance();

        // Set up premium entry animations
        android.widget.ImageView imgTopWave = findViewById(R.id.img_top_wave);
        android.widget.ImageView imgBottomWave = findViewById(R.id.img_bottom_wave);
        android.widget.ImageView imgLogo = findViewById(R.id.img_logo);
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

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail = Email1.getText().toString().trim();
                String pass = Password1.getText().toString().trim();

                if (TextUtils.isEmpty(mail)) {
                    Email1.setError("Email is required");
                    return;
                }
                if (TextUtils.isEmpty(pass)) {
                    Password1.setError("Password is required");
                    return;
                }
                if (pass.length() < 6) {
                    Password1.setError("Password must be at least 6 characters");
                    return;
                }

                // Check if at least one class is selected
                if (!c1.isChecked() && !c2.isChecked() && !c3.isChecked() && !c4.isChecked() && !c5.isChecked()) {
                    Toast.makeText(RegisterActivity.this, "Please select at least one class", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (db.checkEmail(mail)) {
                    Toast.makeText(RegisterActivity.this, "User already exists in local DB", Toast.LENGTH_SHORT).show();
                    // Optional: Continue to firebase check or just return
                }

                mAuth.createUserWithEmailAndPassword(mail, pass)
                        .addOnCompleteListener(RegisterActivity.this, task -> {
                            if (task.isSuccessful()) {
                                String uId = mAuth.getCurrentUser().getUid();
                                ArrayList<String> selectedClasses = new ArrayList<>();
                                if (c1.isChecked()) selectedClasses.add("Class 1");
                                if (c2.isChecked()) selectedClasses.add("Class 2");
                                if (c3.isChecked()) selectedClasses.add("Class 3");
                                if (c4.isChecked()) selectedClasses.add("Class 4");
                                if (c5.isChecked()) selectedClasses.add("Class 5");

                                // Save to Firebase Cloud Firestore
                                FirebaseFirestore dbFirestore = FirebaseFirestore.getInstance();
                                Map<String, Object> teacherData = new HashMap<>();
                                teacherData.put("email", mail);
                                teacherData.put("classes", selectedClasses);
                                dbFirestore.collection("teachers")
                                        .document(uId)
                                        .set(teacherData);

                                // Save to SessionManager (comma separated)
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < selectedClasses.size(); i++) {
                                    sb.append(selectedClasses.get(i));
                                    if (i < selectedClasses.size() - 1) {
                                        sb.append(", ");
                                    }
                                }

                                db.registerUser(mail, pass); // Sync local DB
                                sessionManager.setLogin(true, mail, sb.toString());
                                Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // If account already exists in Firebase, just sign in and update classes
                                if (task.getException() instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                                    mAuth.signInWithEmailAndPassword(mail, pass).addOnCompleteListener(loginTask -> {
                                        if (loginTask.isSuccessful()) {
                                            String uId = mAuth.getCurrentUser().getUid();
                                            ArrayList<String> selectedClasses = new ArrayList<>();
                                            if (c1.isChecked()) selectedClasses.add("Class 1");
                                            if (c2.isChecked()) selectedClasses.add("Class 2");
                                            if (c3.isChecked()) selectedClasses.add("Class 3");
                                            if (c4.isChecked()) selectedClasses.add("Class 4");
                                            if (c5.isChecked()) selectedClasses.add("Class 5");

                                            FirebaseFirestore dbFirestore = FirebaseFirestore.getInstance();
                                            Map<String, Object> teacherData = new HashMap<>();
                                            teacherData.put("email", mail);
                                            teacherData.put("classes", selectedClasses);
                                            dbFirestore.collection("teachers")
                                                    .document(uId)
                                                    .set(teacherData);

                                            StringBuilder sb = new StringBuilder();
                                            for (int i = 0; i < selectedClasses.size(); i++) {
                                                sb.append(selectedClasses.get(i));
                                                if (i < selectedClasses.size() - 1) {
                                                    sb.append(", ");
                                                }
                                            }

                                            db.registerUser(mail, pass);
                                            sessionManager.setLogin(true, mail, sb.toString());
                                            Toast.makeText(RegisterActivity.this, "Welcome Back!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(RegisterActivity.this, "Account exists but login failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    String error = task.getException() != null ? task.getException().getMessage() : "Registration Failed";
                                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        txtlog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });
    }
}
