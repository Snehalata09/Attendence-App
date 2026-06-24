package com.example.attendenceapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.Calendar;

public class DashboardActivity extends AppCompatActivity {

    // Header Section
    MaterialButton btnSubmitAttendance;
    TextView txtTeacherName, txtClassName;
    ImageView menuIcon;
    ImageView imgAddStudent;

    // Attendance Summary
    TextView txtTotal, txtPresent, txtAbsent;
    int totalStudents = 0;
    int presentStudents = 0;
    int absentStudents = 0;

    // Date Section
    TextView txtDate;
    MaterialButton btnDate;

    // RecyclerView Section
    RecyclerView recyclerStudents;
    ArrayList<StudentModel> studentList;
    StudentAdapter adapter;
    int ADD_STUDENT_REQUEST = 1;

    SessionManager sessionManager;
    FirebaseFirestore dbFirestore;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard_acitvity);

        // Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });

        // Initialize Views
        txtTeacherName = findViewById(R.id.txtTeacherName);
        txtClassName = findViewById(R.id.txtClassName);
        menuIcon = findViewById(R.id.menuIcon);
        txtTotal = findViewById(R.id.txtTotal);
        txtPresent = findViewById(R.id.txtPresent);
        txtAbsent = findViewById(R.id.txtAbsent);
        txtDate = findViewById(R.id.txtDate);
        btnDate = findViewById(R.id.btnDate);
        btnSubmitAttendance = findViewById(R.id.btnSubmitAttendance);
        imgAddStudent = findViewById(R.id.imgAddStudent);

        sessionManager = new SessionManager(this);
        
        // Ensure we have the latest UID from Firebase
        userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(this, "Session expired, please login again", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        dbFirestore = FirebaseFirestore.getInstance();

        // RecyclerView Setup
        recyclerStudents = findViewById(R.id.recyclerview);
        recyclerStudents.setLayoutManager(new LinearLayoutManager(this));
        studentList = new ArrayList<>();
        adapter = new StudentAdapter(studentList, this::calculateAttendance);
        recyclerStudents.setAdapter(adapter);

        // Teacher Info
        String email = sessionManager.getEmail();
        if (email != null) {
            txtTeacherName.setText("Welcome, " + extractTeacherName(email));
        } else {
            txtTeacherName.setText("Welcome, Teacher");
        }

        String classes = sessionManager.getClasses();
        if (classes != null && !classes.isEmpty()) {
            txtClassName.setText("Class: " + classes);
        } else {
            txtClassName.setText("Class: Not Assigned");
        }

        // Load data from Firebase
        loadStudentsFromFirebase();
        loadClassesFromFirebase();

        // Date Picker
        btnDate.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(DashboardActivity.this,
                    (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        txtDate.setText(selectedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Add Student
        imgAddStudent.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardActivity.this, AddStudentActivity.class);
            startActivityForResult(intent, ADD_STUDENT_REQUEST);
        });

        // Submit Attendance
        btnSubmitAttendance.setOnClickListener(view -> {
            String date = txtDate.getText().toString();
            if (date.equals("Select Date") || date.isEmpty()) {
                Toast.makeText(this, "Please select a date first", Toast.LENGTH_SHORT).show();
                return;
            }

            String firebaseDateKey = date.replace("/", "-");
            Map<String, Object> attendanceData = new HashMap<>();
            for (StudentModel model : studentList) {
                attendanceData.put(model.getStudentNumber(), model.isPresent() ? "Present" : "Absent");
            }

            dbFirestore.collection("teachers")
                    .document(userId)
                    .collection("attendance")
                    .document(firebaseDateKey)
                    .set(attendanceData)
                    .addOnSuccessListener(aVoid -> {
                        // Reset UI
                        for (StudentModel model : studentList) {
                            model.setPresent(false);
                            model.setAbsent(false);
                        }
                        adapter.notifyDataSetChanged();
                        calculateAttendance();
                        showSuccessDialog("Attendance Saved", "Attendance saved to Firestore successfully!");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(DashboardActivity.this, "Failed to submit attendance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // Menu Icon (Logout)
        menuIcon.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(DashboardActivity.this, menuIcon);
            popupMenu.getMenuInflater().inflate(R.menu.menu_dashboard, popupMenu.getMenu());
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.logout) {
                    sessionManager.logout();
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                }
                return false;
            });
        });

        updateAttendanceCount();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_STUDENT_REQUEST && resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("studentName");
            String roll = data.getStringExtra("studentNumber");
            saveStudentToFirebase(name, roll);
        }
    }

    private void calculateAttendance() {
        presentStudents = 0;
        for (StudentModel model : studentList) {
            if (model.isPresent()) presentStudents++;
        }
        absentStudents = totalStudents - presentStudents;
        updateAttendanceCount();
    }

    private void updateAttendanceCount() {
        txtTotal.setText(String.valueOf(totalStudents));
        txtPresent.setText(String.valueOf(presentStudents));
        txtAbsent.setText(String.valueOf(absentStudents));
    }

    private void saveStudentToFirebase(String name, String roll) {
        StudentModel newStudent = new StudentModel(name, roll);
        dbFirestore.collection("teachers")
                .document(userId)
                .collection("students")
                .document(roll)
                .set(newStudent)
                .addOnSuccessListener(aVoid -> {
                    showSuccessDialog("Student Added", "Student " + name + " has been added successfully.");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DashboardActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadStudentsFromFirebase() {
        dbFirestore.collection("teachers")
                .document(userId)
                .collection("students")
                .addSnapshotListener(new com.google.firebase.firestore.EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@androidx.annotation.Nullable QuerySnapshot value,
                                        @androidx.annotation.Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(DashboardActivity.this, "Error loading students: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (value != null) {
                            studentList.clear();
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                StudentModel student = doc.toObject(StudentModel.class);
                                if (student != null) studentList.add(student);
                            }
                            totalStudents = studentList.size();
                            calculateAttendance();
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void loadClassesFromFirebase() {
        dbFirestore.collection("teachers")
                .document(userId)
                .addSnapshotListener(new com.google.firebase.firestore.EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@androidx.annotation.Nullable DocumentSnapshot snapshot,
                                        @androidx.annotation.Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            return;
                        }
                        if (snapshot != null && snapshot.exists()) {
                            ArrayList<String> selectedClasses = (ArrayList<String>) snapshot.get("classes");
                            if (selectedClasses != null && !selectedClasses.isEmpty()) {
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < selectedClasses.size(); i++) {
                                    sb.append(selectedClasses.get(i));
                                    if (i < selectedClasses.size() - 1) {
                                        sb.append(", ");
                                    }
                                }
                                String classesStr = sb.toString();
                                txtClassName.setText("Class: " + classesStr);
                                sessionManager.saveClasses(classesStr);
                            }
                        }
                    }
                });
    }

    private String extractTeacherName(String email) {
        if (email == null || email.isEmpty()) return "Teacher";
        String name = email.split("@")[0].split("[._\\-\\d]")[0];
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
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
