package com.example.gpacalculator;

public class Course {
    private long id;
    private long studentId;
    private String year;
    private String semester;
    private String courseName;
    private String grade;
    private double credits;

    public Course(long studentId, String year, String semester,
                  String courseName, String grade, double credits) {
        this.studentId = studentId;
        this.year = year;
        this.semester = semester;
        this.courseName = courseName;
        this.grade = grade;
        this.credits = credits;
    }

    public Course(long id, long studentId, String year, String semester,
                  String courseName, String grade, double credits) {
        this.id = id;
        this.studentId = studentId;
        this.year = year;
        this.semester = semester;
        this.courseName = courseName;
        this.grade = grade;
        this.credits = credits;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public double getCredits() {
        return credits;
    }

    public void setCredits(double credits) {
        this.credits = credits;
    }
}