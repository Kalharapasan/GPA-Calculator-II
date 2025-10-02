package com.example.gpacalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "gpa_calculator.db";
    private static final int DATABASE_VERSION = 1;

    // Students table
    private static final String TABLE_STUDENTS = "students";
    private static final String COL_STUDENT_ID = "id";
    private static final String COL_STUDENT_NAME = "name";
    private static final String COL_STUDENT_INDEX = "index_number";

    // Courses table
    private static final String TABLE_COURSES = "courses";
    private static final String COL_COURSE_ID = "id";
    private static final String COL_COURSE_STUDENT_ID = "student_id";
    private static final String COL_COURSE_YEAR = "year";
    private static final String COL_COURSE_SEMESTER = "semester";
    private static final String COL_COURSE_NAME = "course_name";
    private static final String COL_COURSE_GRADE = "grade";
    private static final String COL_COURSE_CREDITS = "credits";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createStudentsTable = "CREATE TABLE " + TABLE_STUDENTS + " (" +
                COL_STUDENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_STUDENT_NAME + " TEXT NOT NULL, " +
                COL_STUDENT_INDEX + " TEXT UNIQUE NOT NULL)";

        String createCoursesTable = "CREATE TABLE " + TABLE_COURSES + " (" +
                COL_COURSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_COURSE_STUDENT_ID + " INTEGER, " +
                COL_COURSE_YEAR + " TEXT, " +
                COL_COURSE_SEMESTER + " TEXT, " +
                COL_COURSE_NAME + " TEXT, " +
                COL_COURSE_GRADE + " TEXT, " +
                COL_COURSE_CREDITS + " REAL, " +
                "FOREIGN KEY(" + COL_COURSE_STUDENT_ID + ") REFERENCES " +
                TABLE_STUDENTS + "(" + COL_STUDENT_ID + "))";

        db.execSQL(createStudentsTable);
        db.execSQL(createCoursesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENTS);
        onCreate(db);
    }

    // Student CRUD operations
    public long addStudent(Student student) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STUDENT_NAME, student.getName());
        values.put(COL_STUDENT_INDEX, student.getIndexNumber());

        long id = db.insert(TABLE_STUDENTS, null, values);
        db.close();
        return id;
    }

    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_STUDENTS + " ORDER BY " + COL_STUDENT_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Student student = new Student(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2)
                );
                students.add(student);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return students;
    }

    public boolean updateStudent(Student student) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STUDENT_NAME, student.getName());
        values.put(COL_STUDENT_INDEX, student.getIndexNumber());

        int result = db.update(TABLE_STUDENTS, values,
                COL_STUDENT_ID + " = ?",
                new String[]{String.valueOf(student.getId())});
        db.close();
        return result > 0;
    }

    public boolean deleteStudent(long studentId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete all courses for this student first
        db.delete(TABLE_COURSES, COL_COURSE_STUDENT_ID + " = ?",
                new String[]{String.valueOf(studentId)});

        // Delete the student
        int result = db.delete(TABLE_STUDENTS, COL_STUDENT_ID + " = ?",
                new String[]{String.valueOf(studentId)});
        db.close();
        return result > 0;
    }

    // Course CRUD operations
    public long addCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COURSE_STUDENT_ID, course.getStudentId());
        values.put(COL_COURSE_YEAR, course.getYear());
        values.put(COL_COURSE_SEMESTER, course.getSemester());
        values.put(COL_COURSE_NAME, course.getCourseName());
        values.put(COL_COURSE_GRADE, course.getGrade());
        values.put(COL_COURSE_CREDITS, course.getCredits());

        long id = db.insert(TABLE_COURSES, null, values);
        db.close();
        return id;
    }

    public List<Course> getCourses(long studentId, String year, String semester) {
        List<Course> courses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_COURSES,
                null,
                COL_COURSE_STUDENT_ID + " = ? AND " + COL_COURSE_YEAR + " = ? AND " + COL_COURSE_SEMESTER + " = ?",
                new String[]{String.valueOf(studentId), year, semester},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Course course = new Course(
                        cursor.getLong(0),
                        cursor.getLong(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getDouble(6)
                );
                courses.add(course);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return courses;
    }

    public List<Course> getAllCoursesForStudent(long studentId) {
        List<Course> courses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_COURSES,
                null,
                COL_COURSE_STUDENT_ID + " = ?",
                new String[]{String.valueOf(studentId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Course course = new Course(
                        cursor.getLong(0),
                        cursor.getLong(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getDouble(6)
                );
                courses.add(course);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return courses;
    }

    public boolean updateCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COURSE_NAME, course.getCourseName());
        values.put(COL_COURSE_GRADE, course.getGrade());
        values.put(COL_COURSE_CREDITS, course.getCredits());
        values.put(COL_COURSE_YEAR, course.getYear());
        values.put(COL_COURSE_SEMESTER, course.getSemester());

        int result = db.update(TABLE_COURSES, values,
                COL_COURSE_ID + " = ?",
                new String[]{String.valueOf(course.getId())});
        db.close();
        return result > 0;
    }

    public boolean deleteCourse(long courseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_COURSES, COL_COURSE_ID + " = ?",
                new String[]{String.valueOf(courseId)});
        db.close();
        return result > 0;
    }
}