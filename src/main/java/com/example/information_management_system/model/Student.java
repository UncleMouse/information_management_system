package com.example.information_management_system.model;

import javafx.beans.property.*;

public class Student {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty sduid = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty gender = new SimpleStringProperty();
    private final StringProperty department = new SimpleStringProperty();
    private final StringProperty major = new SimpleStringProperty();
    private final StringProperty grade = new SimpleStringProperty();
    private final StringProperty className = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty nation = new SimpleStringProperty();
    private final StringProperty ethnic = new SimpleStringProperty();
    private final StringProperty politicsStatus = new SimpleStringProperty();
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public void setId(int id) { this.id.set(id); }

    public String getSduid() { return sduid.get(); }
    public StringProperty sduidProperty() { return sduid; }
    public void setSduid(String sduid) { this.sduid.set(sduid); }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }
    public void setName(String name) { this.name.set(name); }

    public String getGender() { return gender.get(); }
    public StringProperty genderProperty() { return gender; }
    public void setGender(String gender) { this.gender.set(gender); }

    public String getDepartment() { return department.get(); }
    public StringProperty departmentProperty() { return department; }
    public void setDepartment(String department) { this.department.set(department); }

    public String getMajor() { return major.get(); }
    public StringProperty majorProperty() { return major; }
    public void setMajor(String major) { this.major.set(major); }

    public String getGrade() { return grade.get(); }
    public StringProperty gradeProperty() { return grade; }
    public void setGrade(String grade) { this.grade.set(grade); }

    public String getClassName() { return className.get(); }
    public StringProperty classNameProperty() { return className; }
    public void setClassName(String className) { this.className.set(className); }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }
    public void setStatus(String status) { this.status.set(status); }

    public String getNation() { return nation.get(); }
    public StringProperty nationProperty() { return nation; }
    public void setNation(String nation) { this.nation.set(nation); }

    public String getEthnic() { return ethnic.get(); }
    public StringProperty ethnicProperty() { return ethnic; }
    public void setEthnic(String ethnic) { this.ethnic.set(ethnic); }

    public String getPoliticsStatus() { return politicsStatus.get(); }
    public StringProperty politicsStatusProperty() { return politicsStatus; }
    public void setPoliticsStatus(String politicsStatus) { this.politicsStatus.set(politicsStatus); }

    public boolean isSelected() { return selected.get(); }
    public BooleanProperty selectedProperty() { return selected; }
    public void setSelected(boolean selected) { this.selected.set(selected); }
}
