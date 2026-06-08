package com.example.information_management_system.model;

public class TeacherInfo {
    private int id;
    private String sduid;
    private String name;
    private String college;
    private String sex;
    private String contactInfo;
    private String status;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSduid() { return sduid; }
    public void setSduid(String sduid) { this.sduid = sduid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
