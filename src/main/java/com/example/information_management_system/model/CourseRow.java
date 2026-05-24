package com.example.information_management_system.model;

public class CourseRow {
    private String time;
    private String monday;
    private String tuesday;
    private String wednesday;
    private String thursday;
    private String friday;
    private String saturday;
    private String sunday;

    public CourseRow(String time) { this.time = time; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getMonday() { return monday; }
    public void setMonday(String monday) { this.monday = monday; }
    public String getTuesday() { return tuesday; }
    public void setTuesday(String tuesday) { this.tuesday = tuesday; }
    public String getWednesday() { return wednesday; }
    public void setWednesday(String wednesday) { this.wednesday = wednesday; }
    public String getThursday() { return thursday; }
    public void setThursday(String thursday) { this.thursday = thursday; }
    public String getFriday() { return friday; }
    public void setFriday(String friday) { this.friday = friday; }
    public String getSaturday() { return saturday; }
    public void setSaturday(String saturday) { this.saturday = saturday; }
    public String getSunday() { return sunday; }
    public void setSunday(String sunday) { this.sunday = sunday; }
}
