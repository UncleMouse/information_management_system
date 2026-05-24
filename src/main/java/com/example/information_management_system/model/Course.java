package com.example.information_management_system.model;

import javafx.beans.property.*;

public class Course {
    private final StringProperty code = new SimpleStringProperty();
    private final StringProperty teacherName = new SimpleStringProperty();
    private final StringProperty department = new SimpleStringProperty();
    private final DoubleProperty credit = new SimpleDoubleProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final StringProperty teacher = new SimpleStringProperty();
    private final BooleanProperty isActive = new SimpleBooleanProperty(false);
    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final IntegerProperty classNum = new SimpleIntegerProperty();
    private final IntegerProperty peopleNum = new SimpleIntegerProperty();
    private final StringProperty term = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    public String getCode() { return code.get(); }
    public StringProperty codeProperty() { return code; }
    public void setCode(String code) { this.code.set(code); }

    public String getTeacherName() { return teacherName.get(); }
    public StringProperty teacherNameProperty() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName.set(teacherName); }

    public String getDepartment() { return department.get(); }
    public StringProperty departmentProperty() { return department; }
    public void setDepartment(String department) { this.department.set(department); }

    public double getCredit() { return credit.get(); }
    public DoubleProperty creditProperty() { return credit; }
    public void setCredit(double credit) { this.credit.set(credit); }

    public String getType() { return type.get(); }
    public StringProperty typeProperty() { return type; }
    public void setType(String type) { this.type.set(type); }

    public String getTeacher() { return teacher.get(); }
    public StringProperty teacherProperty() { return teacher; }
    public void setTeacher(String teacher) { this.teacher.set(teacher); }

    public boolean isIsActive() { return isActive.get(); }
    public BooleanProperty isActiveProperty() { return isActive; }
    public void setIsActive(boolean isActive) { this.isActive.set(isActive); }

    public boolean isSelected() { return selected.get(); }
    public BooleanProperty selectedProperty() { return selected; }
    public void setSelected(boolean selected) { this.selected.set(selected); }

    public int getClassNum() { return classNum.get(); }
    public IntegerProperty classNumProperty() { return classNum; }
    public void setClassNum(int classNum) { this.classNum.set(classNum); }

    public int getPeopleNum() { return peopleNum.get(); }
    public IntegerProperty peopleNumProperty() { return peopleNum; }
    public void setPeopleNum(int peopleNum) { this.peopleNum.set(peopleNum); }

    public String getTerm() { return term.get(); }
    public StringProperty termProperty() { return term; }
    public void setTerm(String term) { this.term.set(term); }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }
    public void setStatus(String status) { this.status.set(status); }
}
