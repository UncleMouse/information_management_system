package com.example.information_management_system.controller.teacher;

import com.example.information_management_system.model.Student;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
    @FXML private TableColumn<Student, String> statusColumn;
    @FXML private Button backButton;
    @FXML private Label studentCountLabel;

    private ObservableList<Student> studentList = FXCollections.observableArrayList();
    private String currentCourseName;

    @FXML
    public void initialize() {
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTableColumns();
        studentTable.setItems(studentList);

        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, val) -> filterStudents());
        }
        if (backButton != null) {
            backButton.setOnAction(e -> navigateBack());
        }
    }

    private void setupTableColumns() {
        sduidColumn.setCellValueFactory(new PropertyValueFactory<>("sduid"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        majorColumn.setCellValueFactory(new PropertyValueFactory<>("major"));
        gradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));
        classNameColumn.setCellValueFactory(new PropertyValueFactory<>("className"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    public void loadStudentsForCourse(int courseId, String courseName) {
        this.currentCourseName = courseName;
        if (courseNameLabel != null) {
            courseNameLabel.setText("学生名单 - " + (courseName != null ? courseName : ""));
        }
        fetchStudents(courseId, courseName);
    }

    private void fetchStudents(int courseId, String courseName) {
        Map<String, String> params = new HashMap<>();
        params.put("courseName", courseName);

        NetworkUtils.get("/class/" + courseId + "/students", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = res.getAsJsonArray("data");
                        ObservableList<Student> list = FXCollections.observableArrayList();
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject obj = arr.get(i).getAsJsonObject();
                            Student student = new Student();
                            if (obj.has("sduid")) student.setSduid(obj.get("sduid").getAsString());
                            if (obj.has("name")) student.setName(obj.get("name").getAsString());
                            if (obj.has("gender")) student.setGender(obj.get("gender").getAsString());
                            if (obj.has("department")) student.setDepartment(obj.get("department").getAsString());
                            if (obj.has("major")) student.setMajor(obj.get("major").getAsString());
                            if (obj.has("grade")) student.setGrade(obj.get("grade").getAsString());
                            if (obj.has("className")) student.setClassName(obj.get("className").getAsString());
                            if (obj.has("status")) student.setStatus(obj.get("status").getAsString());
                            if (obj.has("id")) student.setId(obj.get("id").getAsInt());
                            list.add(student);
                        }
                        Platform.runLater(() -> {
                            studentList.setAll(list);
                            if (studentCountLabel != null) {
                                studentCountLabel.setText("共 " + list.size() + " 条");
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() ->
                            ShowMessage.showErrorMessage("错误", "数据解析失败，请稍后重试"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() ->
                        ShowMessage.showErrorMessage("错误", "数据加载失败: " + e.getMessage()));
            }
        });
    }

    private void filterStudents() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            studentTable.setItems(studentList);
            return;
        }
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
            StackPane contentArea = findContentArea();
            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(
                        Objects.requireNonNull(getClass().getResource(
                                "/com/example/information_management_system/teacher/CourseManagementContent.fxml"))
                );
                Parent view = loader.load();
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private StackPane findContentArea() {
        if (studentTable != null && studentTable.getScene() != null) {
            return (StackPane) studentTable.getScene().lookup("#contentArea");
        }
        return null;
    }
}
