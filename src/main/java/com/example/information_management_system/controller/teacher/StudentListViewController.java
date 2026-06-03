package com.example.information_management_system.controller.teacher;

import com.example.information_management_system.model.Student;
import com.example.information_management_system.util.JsonUtil;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.Objects;

public class StudentListViewController {

    private final Gson gson = new Gson();

    @FXML private Label courseNameLabel;
    @FXML private TextField searchField;
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> sduidColumn;
    @FXML private TableColumn<Student, String> nameColumn;
    @FXML private TableColumn<Student, String> genderColumn;
    @FXML private TableColumn<Student, String> departmentColumn;
    @FXML private TableColumn<Student, String> majorColumn;
    @FXML private TableColumn<Student, String> gradeColumn;
    @FXML private TableColumn<Student, String> classNameColumn;
    @FXML private Button backButton;
    @FXML private Label studentCountLabel;

    private ObservableList<Student> studentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTableColumns();
        studentTable.setItems(studentList);
        if (searchField != null) searchField.textProperty().addListener((obs, old, val) -> filterStudents());
        if (backButton != null) backButton.setOnAction(e -> navigateBack());
    }

    private void setupTableColumns() {
        sduidColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSduid()));
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        genderColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getGender()));
        departmentColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDepartment()));
        majorColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMajor()));
        gradeColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getGrade()));
        classNameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getClassName()));
    }

    public void loadStudentsForCourse(int courseId, String courseName) {
        if (courseNameLabel != null) courseNameLabel.setText("学生名单 - " + (courseName != null ? courseName : ""));
        fetchStudents(courseId, courseName);
    }

    private void fetchStudents(int courseId, String courseName) {
        NetworkUtils.get("/class/" + courseId + "/students", new NetworkUtils.Callback<String>() {
            @Override public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        ObservableList<Student> list = FXCollections.observableArrayList();
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject obj = arr.get(i).getAsJsonObject();
                            Student s = new Student();
                            // API: sduid, username, sectionNumber, major, number
                            String sduid = obj.has("sduid") ? obj.get("sduid").getAsString() : "";
                            if (!sduid.isEmpty()) { s.setSduid(sduid); s.setGrade(sduid.length()>=4 ? sduid.substring(0,4) : "-"); }
                            else s.setSduid("-");
                            if (obj.has("username")) s.setName(obj.get("username").getAsString());
                            if (obj.has("major")) { s.setMajor(obj.get("major").getAsString()); s.setDepartment(obj.get("major").getAsString()); }
                            if (obj.has("number")) s.setClassName(obj.get("number").getAsString());
                            else if (obj.has("sectionNumber") && obj.get("sectionNumber").getAsInt() > 0) s.setClassName(String.valueOf(obj.get("sectionNumber").getAsInt()));
                            s.setGender("-"); s.setStatus("-");
                            list.add(s);
                        }
                        Platform.runLater(() -> {
                            studentList.setAll(list);
                            if (studentCountLabel != null) studentCountLabel.setText("共 " + list.size() + " 条");
                        });
                    }
                } catch (Exception e) { Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "数据解析失败")); }
            }
            @Override public void onFailure(Exception e) { Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "数据加载失败")); }
        });
    }

    private void filterStudents() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) { studentTable.setItems(studentList); return; }
        ObservableList<Student> filtered = FXCollections.observableArrayList();
        for (Student s : studentList) {
            if ((s.getName() != null && s.getName().toLowerCase().contains(query))
                    || (s.getSduid() != null && s.getSduid().toLowerCase().contains(query))
                    || (s.getClassName() != null && s.getClassName().toLowerCase().contains(query))
                    || (s.getMajor() != null && s.getMajor().toLowerCase().contains(query))) {
                filtered.add(s);
            }
        }
        studentTable.setItems(filtered);
    }

    private void navigateBack() {
        try {
            Pane contentArea = findContentArea();
            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/com/example/information_management_system/teacher/CourseManagementContent.fxml")));
                Parent view = loader.load();
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private Pane findContentArea() {
        if (studentTable != null && studentTable.getScene() != null)
            return (Pane) studentTable.getScene().lookup("#contentArea");
        return null;
    }
}
