package com.example.gpacalculator;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements StudentAdapter.StudentClickListener {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private List<Student> studentList;
    private FloatingActionButton fabAddStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerViewStudents);
        fabAddStudent = findViewById(R.id.fabAddStudent);

        setupRecyclerView();
        loadStudents();

        fabAddStudent.setOnClickListener(v -> showAddStudentDialog());
    }

    private void setupRecyclerView() {
        studentList = new ArrayList<>();
        adapter = new StudentAdapter(studentList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadStudents() {
        studentList.clear();
        studentList.addAll(dbHelper.getAllStudents());
        adapter.notifyDataSetChanged();

        if (studentList.isEmpty()) {
            Toast.makeText(this, "No students found. Add a student to begin.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddStudentDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_student, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etStudentName);
        TextInputEditText etIndex = dialogView.findViewById(R.id.etIndexNumber);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Add Student")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String indexNumber = etIndex.getText().toString().trim();

                    if (name.isEmpty() || indexNumber.isEmpty()) {
                        Toast.makeText(this, "Both fields are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Student student = new Student(name, indexNumber);
                    long id = dbHelper.addStudent(student);

                    if (id > 0) {
                        Toast.makeText(this, "Student added successfully", Toast.LENGTH_SHORT).show();
                        loadStudents();
                    } else {
                        Toast.makeText(this, "Student already exists or error occurred", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditStudentDialog(Student student) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_student, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etStudentName);
        TextInputEditText etIndex = dialogView.findViewById(R.id.etIndexNumber);

        etName.setText(student.getName());
        etIndex.setText(student.getIndexNumber());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Edit Student")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String indexNumber = etIndex.getText().toString().trim();

                    if (name.isEmpty() || indexNumber.isEmpty()) {
                        Toast.makeText(this, "Both fields are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    student.setName(name);
                    student.setIndexNumber(indexNumber);

                    if (dbHelper.updateStudent(student)) {
                        Toast.makeText(this, "Student updated successfully", Toast.LENGTH_SHORT).show();
                        loadStudents();
                    } else {
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onStudentClick(Student student) {
        Intent intent = new Intent(this, CourseActivity.class);
        intent.putExtra("student_id", student.getId());
        intent.putExtra("student_name", student.getName());
        intent.putExtra("student_index", student.getIndexNumber());
        startActivity(intent);
    }

    @Override
    public void onEditClick(Student student) {
        showEditStudentDialog(student);
    }

    @Override
    public void onDeleteClick(Student student) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Student")
                .setMessage("Delete '" + student.getName() + "' and all related courses?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (dbHelper.deleteStudent(student.getId())) {
                        Toast.makeText(this, "Student deleted", Toast.LENGTH_SHORT).show();
                        loadStudents();
                    } else {
                        Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStudents();
    }
}