package com.example.information_management_system.model;

import javafx.beans.property.*;

public class Section {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty className = new SimpleStringProperty();
    private final StringProperty major = new SimpleStringProperty();
    private final StringProperty grade = new SimpleStringProperty();
    private final IntegerProperty teacherId = new SimpleIntegerProperty();
    private final IntegerProperty number = new SimpleIntegerProperty();
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public void setId(int id) { this.id.set(id); }

    public String getClassName() { return className.get(); }
    public StringProperty classNameProperty() { return className; }
    public void setClassName(String className) { this.className.set(className); }

    public String getMajor() { return major.get(); }
    public StringProperty majorProperty() { return major; }
    public void setMajor(String major) { this.major.set(major); }

    public String getGrade() { return grade.get(); }
    public StringProperty gradeProperty() { return grade; }
    public void setGrade(String grade) { this.grade.set(grade); }

    public int getTeacherId() { return teacherId.get(); }
    public IntegerProperty teacherIdProperty() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId.set(teacherId); }

    public int getNumber() { return number.get(); }
    public IntegerProperty numberProperty() { return number; }
    public void setNumber(int number) { this.number.set(number); }

    public boolean isSelected() { return selected.get(); }
    public BooleanProperty selectedProperty() { return selected; }
    public void setSelected(boolean selected) { this.selected.set(selected); }
}
