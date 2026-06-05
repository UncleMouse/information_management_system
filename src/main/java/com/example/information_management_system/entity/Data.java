package com.example.information_management_system.entity;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Data {
    private static Data instance;
    private String currentTerm;
    private ObservableList<String> semesterList = FXCollections.observableArrayList();
    private ObservableList<String> classRoomList = FXCollections.observableArrayList();
    private final java.util.Map<String, String> sectionNameMap = new java.util.HashMap<>();

    private Data() {}

    public static synchronized Data getInstance() {
        if (instance == null) instance = new Data();
        return instance;
    }

    public String getCurrentTerm() { return currentTerm; }
    public void setCurrentTerm(String currentTerm) { this.currentTerm = currentTerm; }
    public ObservableList<String> getSemesterList() { return semesterList; }
    public void setSemesterList(ObservableList<String> list) { this.semesterList = list; }
    public ObservableList<String> getClassRoomList() { return classRoomList; }
    public void setClassRoomList(ObservableList<String> list) { this.classRoomList = list; }
    public java.util.Map<String, String> getSectionNameMap() { return sectionNameMap; }
}
