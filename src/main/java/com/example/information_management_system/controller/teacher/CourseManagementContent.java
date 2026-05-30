package com.example.information_management_system.controller.teacher;

import com.example.information_management_system.model.Course;
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
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.Objects;

public class CourseManagementContent {

    private final Gson gson = new Gson();

    @FXML private TableView<Course> courseTable;
    @FXML private TableColumn<Course, String> nameColumn;
    @FXML private TableColumn<Course, String> codeColumn;
    @FXML private TableColumn<Course, Double> creditColumn;
    @FXML private TableColumn<Course, String> typeColumn;
    @FXML private TableColumn<Course, Integer> peopleNumColumn;
    @FXML private TableColumn<Course, String> statusColumn;
    @FXML private TextField searchField;
    @FXML private Button applyNewCourseButton;
    @FXML private Button editCourseButton;
    @FXML private Button viewStudentsButton;

    private ObservableList<Course> courseList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        courseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTableColumns();
        courseTable.setItems(courseList);

        if (applyNewCourseButton != null) {
            applyNewCourseButton.setOnAction(e -> openApplyNewCourse());
        }
        if (editCourseButton != null) {
            editCourseButton.setOnAction(e -> handleEditCourse());
        }
        if (viewStudentsButton != null) {
            viewStudentsButton.setOnAction(e -> handleViewStudents());
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, val) -> filterCourses());
        }

        fetchCourses();
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(cell -> {
            String code = cell.getValue().getCode();
            String teacherName = cell.getValue().getTeacherName();
            String displayName = code;
            return new SimpleStringProperty(displayName != null ? displayName : "");
        });
        codeColumn.setCellValueFactory(cell -> {
            String name = cell.getValue().getCode();
            return new SimpleStringProperty(name != null ? name : "");
        });
        creditColumn.setCellValueFactory(new PropertyValueFactory<>("credit"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        peopleNumColumn.setCellValueFactory(new PropertyValueFactory<>("peopleNum"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void fetchCourses() {
        NetworkUtils.get("/class/list", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        // data 可能是对象 {list:[], total:0, ...} 或直接是数组
                        JsonArray arr = extractArray(res, "data");
                        ObservableList<Course> list = FXCollections.observableArrayList();
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject obj = arr.get(i).getAsJsonObject();
                            Course course = new Course();
                            if (obj.has("id")) course.setId(obj.get("id").getAsInt());
                            if (obj.has("name")) course.setCode(obj.get("name").getAsString());
                            if (obj.has("code")) course.setCode(obj.get("code").getAsString());
                            if (obj.has("name")) course.setTeacherName(obj.get("name").getAsString());
                            if (obj.has("credit")) course.setCredit(obj.get("credit").getAsDouble());
                            if (obj.has("type")) course.setType(obj.get("type").getAsString());
                            if (obj.has("peopleNum")) course.setPeopleNum(obj.get("peopleNum").getAsInt());
                            if (obj.has("status")) course.setStatus(obj.get("status").getAsString());
                            if (obj.has("classNum")) course.setClassNum(obj.get("classNum").getAsInt());
                            if (obj.has("term")) course.setTerm(obj.get("term").getAsString());
                            if (obj.has("teacher")) course.setTeacher(obj.get("teacher").getAsString());
                            if (obj.has("department")) course.setDepartment(obj.get("department").getAsString());
                            list.add(course);
                        }
                        Platform.runLater(() -> courseList.setAll(list));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() ->
                        ShowMessage.showErrorMessage("错误", "数据加载失败: " + e.getMessage()));
            }
        });
    }

    /** 兼容后端返回 data 为对象 {list:[],...} 或直接为数组 */
    private JsonArray extractArray(JsonObject res, String key) {
        if (res.get(key).isJsonArray()) return res.getAsJsonArray(key);
        if (res.get(key).isJsonObject()) {
            JsonObject obj = res.getAsJsonObject(key);
            if (obj.has("list")) return obj.getAsJsonArray("list");
            if (obj.has("records")) return obj.getAsJsonArray("records");
        }
        return new JsonArray();
    }

    private void filterCourses() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            courseTable.setItems(courseList);
            return;
        }
        ObservableList<Course> filtered = FXCollections.observableArrayList();
        for (Course c : courseList) {
            if ((c.getCode() != null && c.getCode().toLowerCase().contains(query))
                    || (c.getTeacherName() != null && c.getTeacherName().toLowerCase().contains(query))
                    || (c.getType() != null && c.getType().toLowerCase().contains(query))) {
                filtered.add(c);
            }
        }
        courseTable.setItems(filtered);
    }

    private void openApplyNewCourse() {
        try {
            Pane contentArea = findContentArea();
            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(
                        Objects.requireNonNull(getClass().getResource(
                                "/com/example/information_management_system/teacher/ApplyNewCourse.fxml"))
                );
                Parent view = loader.load();
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
            ShowMessage.showErrorMessage("错误", "无法打开申请新课页面");
        }
    }

    private void handleEditCourse() {
        Course selected = courseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowMessage.showWarningMessage("提示", "请先选择一门课程进行编辑");
            return;
        }
        try {
            Pane contentArea = findContentArea();
            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(
                        Objects.requireNonNull(getClass().getResource(
                                "/com/example/information_management_system/teacher/editCourse.fxml"))
                );
                Parent view = loader.load();
                editCourseController controller = loader.getController();
                controller.setCourseData(selected);
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
            ShowMessage.showErrorMessage("错误", "无法打开编辑课程页面");
        }
    }

    private void handleViewStudents() {
        Course selected = courseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowMessage.showWarningMessage("提示", "请先选择一门课程查看学生名单");
            return;
        }
        try {
            Pane contentArea = findContentArea();
            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(
                        Objects.requireNonNull(getClass().getResource(
                                "/com/example/information_management_system/teacher/StudentListView.fxml"))
                );
                Parent view = loader.load();
                StudentListViewController controller = loader.getController();
                controller.loadStudentsForCourse(selected.getId(), selected.getCode());
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
            ShowMessage.showErrorMessage("错误", "无法打开学生名单页面");
        }
    }

    private Pane findContentArea() {
        if (courseTable != null && courseTable.getScene() != null) {
            return (Pane) courseTable.getScene().lookup("#contentArea");
        }
        return null;
    }
}
