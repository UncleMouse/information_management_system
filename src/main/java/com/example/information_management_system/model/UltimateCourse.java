package com.example.information_management_system.model;

public class UltimateCourse {
    private int id;
    private String name;
    private String teacherName;
    private String type;
    private double credit;
    private String time;
    private String classroom;
    private int weekStart;
    private int weekEnd;
    private String term;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getCredit() { return credit; }
    public void setCredit(double credit) { this.credit = credit; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getClassroom() { return classroom; }
    public void setClassroom(String classroom) { this.classroom = classroom; }
    public int getWeekStart() { return weekStart; }
    public void setWeekStart(int weekStart) { this.weekStart = weekStart; }
    public int getWeekEnd() { return weekEnd; }
    public void setWeekEnd(int weekEnd) { this.weekEnd = weekEnd; }
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
}
