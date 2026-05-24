package com.example.information_management_system.model;

public class ClassInfo {
    private int id;
    private String name;
    private String department;
    private String grade;
    private String counselor;
    private int studentCount;
    private String status;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getCounselor() { return counselor; }
    public void setCounselor(String counselor) { this.counselor = counselor; }
    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
