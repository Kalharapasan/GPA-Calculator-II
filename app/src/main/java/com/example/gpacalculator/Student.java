package com.example.gpacalculator;
public class Student {
    private long id;
    private String name;
    private String indexNumber;

    public Student(String name, String indexNumber) {
        this.name = name;
        this.indexNumber = indexNumber;
    }

    public Student(long id, String name, String indexNumber) {
        this.id = id;
        this.name = name;
        this.indexNumber = indexNumber;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndexNumber() {
        return indexNumber;
    }

    public void setIndexNumber(String indexNumber) {
        this.indexNumber = indexNumber;
    }
}