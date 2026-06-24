package com.example.attendenceapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StudentAdapter
        extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    ArrayList<StudentModel> studentList;
    OnAttendanceChangeListener listener;

    public StudentAdapter(ArrayList<StudentModel> studentList,
                          OnAttendanceChangeListener listener) {

        this.studentList = studentList;

        this.listener = listener;
    }
    public interface OnAttendanceChangeListener {

        void onAttendanceChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_student_item,
                        parent,
                        false);

        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull StudentViewHolder holder,
            int position) {

        StudentModel model = studentList.get(position);

        holder.txtStudentName.setText(model.getStudentName());
        holder.txtStudentNumber.setText(model.getStudentNumber());

        // Apply slide-up entry animation for rows
        holder.itemView.setScaleX(0.95f);
        holder.itemView.setScaleY(0.95f);
        holder.itemView.setAlpha(0.0f);
        holder.itemView.setTranslationY(60f);
        holder.itemView.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .translationY(0.0f)
                .setDuration(400)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

        // Reset radio buttons state to match model
        holder.radioPresent.setChecked(model.isPresent());
        holder.radioAbsent.setChecked(model.isAbsent());

        holder.radioPresent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                holder.radioPresent.setChecked(true);

                holder.radioAbsent.setChecked(false);

                model.setPresent(true);
                model.setAbsent(false);

                listener.onAttendanceChanged();
            }
        });

        holder.radioAbsent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                holder.radioAbsent.setChecked(true);

                holder.radioPresent.setChecked(false);

                model.setPresent(false);
                model.setAbsent(true);

                listener.onAttendanceChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class StudentViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtStudentName;
        TextView txtStudentNumber;

        RadioButton radioPresent, radioAbsent;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);

            txtStudentName =
                    itemView.findViewById(R.id.txtStudentName);
            txtStudentNumber =
                    itemView.findViewById(R.id.txtStudentNumber);

            radioPresent =
                    itemView.findViewById(R.id.radioPresent);

            radioAbsent =
                    itemView.findViewById(R.id.radioAbsent);
        }
    }
}