package com.example.information_management_system.model;

public class CourseForScoreInput {
    private int id;
    private String name;
    private String teacherName;
    private String term;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
}
