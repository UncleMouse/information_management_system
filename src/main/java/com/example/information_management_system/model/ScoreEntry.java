package com.example.information_management_system.model;

import javafx.beans.property.*;

public class ScoreEntry {
    private final IntegerProperty studentId = new SimpleIntegerProperty();
    private final StringProperty sduid = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty className = new SimpleStringProperty();
    private final StringProperty courseName = new SimpleStringProperty();
    private final DoubleProperty regularScore = new SimpleDoubleProperty();
    private final DoubleProperty finalScore = new SimpleDoubleProperty();
    private final DoubleProperty totalScore = new SimpleDoubleProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty remarks = new SimpleStringProperty();

    public ScoreEntry() {
        regularScore.addListener((obs, old, val) -> updateTotal());
        finalScore.addListener((obs, old, val) -> updateTotal());
    }

    private void updateTotal() {
        setTotalScore(getRegularScore() + getFinalScore());
    }

    public int getStudentId() { return studentId.get(); }
    public IntegerProperty studentIdProperty() { return studentId; }
    public void setStudentId(int studentId) { this.studentId.set(studentId); }

    public String getSduid() { return sduid.get(); }
    public StringProperty sduidProperty() { return sduid; }
    public void setSduid(String sduid) { this.sduid.set(sduid); }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }
    public void setName(String name) { this.name.set(name); }

    public String getClassName() { return className.get(); }
    public StringProperty classNameProperty() { return className; }
    public void setClassName(String className) { this.className.set(className); }

    public String getCourseName() { return courseName.get(); }
    public StringProperty courseNameProperty() { return courseName; }
    public void setCourseName(String courseName) { this.courseName.set(courseName); }

    public double getRegularScore() { return regularScore.get(); }
    public DoubleProperty regularScoreProperty() { return regularScore; }
    public void setRegularScore(double regularScore) { this.regularScore.set(regularScore); }

    public double getFinalScore() { return finalScore.get(); }
    public DoubleProperty finalScoreProperty() { return finalScore; }
    public void setFinalScore(double finalScore) { this.finalScore.set(finalScore); }

    public double getTotalScore() { return totalScore.get(); }
    public DoubleProperty totalScoreProperty() { return totalScore; }
    public void setTotalScore(double totalScore) { this.totalScore.set(totalScore); }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }
    public void setStatus(String status) { this.status.set(status); }

    public String getRemarks() { return remarks.get(); }
    public StringProperty remarksProperty() { return remarks; }
    public void setRemarks(String remarks) { this.remarks.set(remarks); }
}
