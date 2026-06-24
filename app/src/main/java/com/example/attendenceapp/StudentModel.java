package com.example.attendenceapp;

public class StudentModel {

    String studentName;
    String studentNumber;

    boolean isPresent;
    boolean isAbsent;

    public StudentModel() {
        // Required for Firebase
    }

    public StudentModel(String studentName,
                        String studentNumber) {

        this.studentName = studentName;
        this.studentNumber = studentNumber;

        isPresent = false;
        isAbsent = false;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public void setPresent(boolean present) {
        isPresent = present;
    }

    public boolean isAbsent() {
        return isAbsent;
    }

    public void setAbsent(boolean absent) {
        isAbsent = absent;
    }
}
