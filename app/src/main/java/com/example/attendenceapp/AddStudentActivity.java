package com.example.attendenceapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class AddStudentActivity extends AppCompatActivity {

    EditText etStudentName, etStudentNumber;
    MaterialButton btnSave;
    MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_student);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        etStudentName = findViewById(R.id.etStudentName);
        etStudentNumber = findViewById(R.id.etStudentNumber);
        btnSave = findViewById(R.id.btnSaveStudent);

        View cardAddStudent = findViewById(R.id.cardAddStudent);
        if (cardAddStudent != null) {
            cardAddStudent.setTranslationY(150f);
            cardAddStudent.setAlpha(0f);
            cardAddStudent.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(800)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
        }

        btnSave.setOnClickListener(v -> {
            String name = etStudentName.getText().toString().trim();
            String roll = etStudentNumber.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                etStudentName.setError("Name is required");
                return;
            }
            if (TextUtils.isEmpty(roll)) {
                etStudentNumber.setError("Roll number is required");
                return;
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("studentName", name);
            resultIntent.putExtra("studentNumber", roll);
            setResult(RESULT_OK, resultIntent);
            finish();
            
            Toast.makeText(this, "Student Added Successfully", Toast.LENGTH_SHORT).show();
        });
    }
}
