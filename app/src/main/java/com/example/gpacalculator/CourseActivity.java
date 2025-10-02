package com.example.gpacalculator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseActivity extends AppCompatActivity implements CourseAdapter.CourseClickListener {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private CourseAdapter adapter;
    private List<Course> courseList;
    private FloatingActionButton fabAddCourse;
    private TextView tvCumulativeGPA, tvSemesterGPA;
    private Spinner spinnerYear, spinnerSemester;

    private long studentId;
    private String studentName;
    private String studentIndex;
    private String currentYear = "Year 1";
    private String currentSemester = "Semester 1";

    private static final Map<String, Double> GRADE_POINTS = new HashMap<>();
    static {
        GRADE_POINTS.put("A+", 4.0);
        GRADE_POINTS.put("A", 4.0);
        GRADE_POINTS.put("A-", 3.7);
        GRADE_POINTS.put("B+", 3.3);
        GRADE_POINTS.put("B", 3.0);
        GRADE_POINTS.put("B-", 2.7);
        GRADE_POINTS.put("C+", 2.3);
        GRADE_POINTS.put("C", 2.0);
        GRADE_POINTS.put("C-", 1.7);
        GRADE_POINTS.put("D+", 1.3);
        GRADE_POINTS.put("D", 1.0);
        GRADE_POINTS.put("D-", 0.7);
        GRADE_POINTS.put("F", 0.0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        studentId = getIntent().getLongExtra("student_id", -1);
        studentName = getIntent().getStringExtra("student_name");
        studentIndex = getIntent().getStringExtra("student_index");

        getSupportActionBar().setTitle(studentName + " - " + studentIndex);

        dbHelper = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerViewCourses);
        fabAddCourse = findViewById(R.id.fabAddCourse);
        tvCumulativeGPA = findViewById(R.id.tvCumulativeGPA);
        tvSemesterGPA = findViewById(R.id.tvSemesterGPA);
        spinnerYear = findViewById(R.id.spinnerYear);
        spinnerSemester = findViewById(R.id.spinnerSemester);

        setupSpinners();
        setupRecyclerView();
        loadCourses();
        calculateGPA();

        fabAddCourse.setOnClickListener(v -> showAddCourseDialog());
    }

    private void setupSpinners() {
        String[] years = {"Year 1", "Year 2", "Year 3", "Year 4", "Year 5"};
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        String[] semesters = {"Semester 1", "Semester 2", "Summer"};
        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, semesters);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemester.setAdapter(semesterAdapter);

        spinnerYear.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                currentYear = years[position];
                loadCourses();
                calculateGPA();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        spinnerSemester.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                currentSemester = semesters[position];
                loadCourses();
                calculateGPA();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerView() {
        courseList = new ArrayList<>();
        adapter = new CourseAdapter(courseList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadCourses() {
        courseList.clear();
        courseList.addAll(dbHelper.getCourses(studentId, currentYear, currentSemester));
        adapter.notifyDataSetChanged();
    }

    private void showAddCourseDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_course, null);
        TextInputEditText etCourseName = dialogView.findViewById(R.id.etCourseName);
        TextInputEditText etCredits = dialogView.findViewById(R.id.etCredits);
        Spinner spinnerGrade = dialogView.findViewById(R.id.spinnerGrade);

        ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "F"});
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGrade.setAdapter(gradeAdapter);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Add Course")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String courseName = etCourseName.getText().toString().trim();
                    String creditsStr = etCredits.getText().toString().trim();
                    String grade = spinnerGrade.getSelectedItem().toString();

                    if (courseName.isEmpty() || creditsStr.isEmpty()) {
                        Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double credits = Double.parseDouble(creditsStr);
                        if (credits <= 0) {
                            Toast.makeText(this, "Credits must be positive", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Course course = new Course(studentId, currentYear, currentSemester, courseName, grade, credits);
                        long id = dbHelper.addCourse(course);

                        if (id > 0) {
                            Toast.makeText(this, "Course added successfully", Toast.LENGTH_SHORT).show();
                            loadCourses();
                            calculateGPA();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid credits value", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditCourseDialog(Course course) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_course, null);
        TextInputEditText etCourseName = dialogView.findViewById(R.id.etCourseName);
        TextInputEditText etCredits = dialogView.findViewById(R.id.etCredits);
        Spinner spinnerGrade = dialogView.findViewById(R.id.spinnerGrade);

        etCourseName.setText(course.getCourseName());
        etCredits.setText(String.valueOf(course.getCredits()));

        String[] grades = {"A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "F"};
        ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, grades);
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGrade.setAdapter(gradeAdapter);

        for (int i = 0; i < grades.length; i++) {
            if (grades[i].equals(course.getGrade())) {
                spinnerGrade.setSelection(i);
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Edit Course")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String courseName = etCourseName.getText().toString().trim();
                    String creditsStr = etCredits.getText().toString().trim();
                    String grade = spinnerGrade.getSelectedItem().toString();

                    if (courseName.isEmpty() || creditsStr.isEmpty()) {
                        Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double credits = Double.parseDouble(creditsStr);
                        if (credits <= 0) {
                            Toast.makeText(this, "Credits must be positive", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        course.setCourseName(courseName);
                        course.setGrade(grade);
                        course.setCredits(credits);

                        if (dbHelper.updateCourse(course)) {
                            Toast.makeText(this, "Course updated successfully", Toast.LENGTH_SHORT).show();
                            loadCourses();
                            calculateGPA();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid credits value", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void calculateGPA() {
        // Cumulative GPA
        List<Course> allCourses = dbHelper.getAllCoursesForStudent(studentId);
        double totalPoints = 0;
        double totalCredits = 0;

        for (Course course : allCourses) {
            double gradePoint = GRADE_POINTS.getOrDefault(course.getGrade(), 0.0);
            totalPoints += gradePoint * course.getCredits();
            totalCredits += course.getCredits();
        }

        double cumulativeGPA = totalCredits > 0 ? totalPoints / totalCredits : 0.0;
        tvCumulativeGPA.setText(String.format("Cumulative GPA: %.2f (Credits: %.1f)", cumulativeGPA, totalCredits));

        // Semester GPA
        double semPoints = 0;
        double semCredits = 0;

        for (Course course : courseList) {
            double gradePoint = GRADE_POINTS.getOrDefault(course.getGrade(), 0.0);
            semPoints += gradePoint * course.getCredits();
            semCredits += course.getCredits();
        }

        double semesterGPA = semCredits > 0 ? semPoints / semCredits : 0.0;
        tvSemesterGPA.setText(String.format("%s %s GPA: %.2f (Credits: %.1f)",
                currentYear, currentSemester, semesterGPA, semCredits));
    }

    @Override
    public void onEditClick(Course course) {
        showEditCourseDialog(course);
    }

    @Override
    public void onDeleteClick(Course course) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Course")
                .setMessage("Delete '" + course.getCourseName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (dbHelper.deleteCourse(course.getId())) {
                        Toast.makeText(this, "Course deleted", Toast.LENGTH_SHORT).show();
                        loadCourses();
                        calculateGPA();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_course, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_export) {
            exportToExcel();
            return true;
        } else if (id == R.id.action_import) {
            Toast.makeText(this, "Import feature: Use file picker to import Excel", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void exportToExcel() {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Courses");

            // Header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Year");
            headerRow.createCell(1).setCellValue("Semester");
            headerRow.createCell(2).setCellValue("Course Name");
            headerRow.createCell(3).setCellValue("Grade");
            headerRow.createCell(4).setCellValue("Credits");

            // Data rows
            List<Course> allCourses = dbHelper.getAllCoursesForStudent(studentId);
            int rowNum = 1;
            for (Course course : allCourses) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(course.getYear());
                row.createCell(1).setCellValue(course.getSemester());
                row.createCell(2).setCellValue(course.getCourseName());
                row.createCell(3).setCellValue(course.getGrade());
                row.createCell(4).setCellValue(course.getCredits());
            }

            // Save file
            File file = new File(getExternalFilesDir(null), studentName + "_courses.xlsx");
            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            Toast.makeText(this, "Exported to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            // Share file
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Excel File"));

        } catch (Exception e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}