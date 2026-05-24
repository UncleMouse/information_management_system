package com.example.information_management_system.model;

public class ScoreRecord {
    private int index;
    private int id;
    private String courseName;
    private double point;
    private String type;
    private String teacher;
    private String grade;
    private double gpa;
    private int rank;
    private double regular;
    private double finalScore;

    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public double getPoint() { return point; }
    public void setPoint(double point) { this.point = point; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public double getGpa() { return gpa; }
    public void setGpa(double gpa) { this.gpa = gpa; }
    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public double getRegular() { return regular; }
    public void setRegular(double regular) { this.regular = regular; }
    public double getFinalScore() { return finalScore; }
    public void setFinalScore(double finalScore) { this.finalScore = finalScore; }
}
