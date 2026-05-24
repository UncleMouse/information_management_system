package com.example.information_management_system.controller.student;

import com.example.information_management_system.entity.Data;
import com.example.information_management_system.entity.UserSession;
import com.example.information_management_system.model.Course;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.HashMap;
import java.util.Map;

public class CourseSelectionContentController {

    private final Gson gson = new Gson();

    @FXML private ComboBox<String> termSelector;
    @FXML private TextField searchField;
    @FXML private Button searchBtn;
    @FXML private Tab availableTab;
    @FXML private Tab selectedTab;

    @FXML private TableView<Course> availableTable;
    @FXML private TableColumn<Course, String> availColName;
    @FXML private TableColumn<Course, String> availColTeacher;
    @FXML private TableColumn<Course, Double> availColCredit;
    @FXML private TableColumn<Course, String> availColType;
    @FXML private TableColumn<Course, Integer> availColCapacity;
    @FXML private TableColumn<Course, Integer> availColEnrolled;
    @FXML private TableColumn<Course, String> availColTime;
    @FXML private TableColumn<Course, Void> availColAction;

    @FXML private TableView<Course> selectedTable;
    @FXML private TableColumn<Course, String> selColName;
    @FXML private TableColumn<Course, String> selColTeacher;
    @FXML private TableColumn<Course, Double> selColCredit;
    @FXML private TableColumn<Course, String> selColType;
    @FXML private TableColumn<Course, String> selColTime;
    @FXML private TableColumn<Course, Void> selColAction;

    private final ObservableList<Course> availableCourses = FXCollections.observableArrayList();
    private final ObservableList<Course> selectedCourses = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupAvailableTable();
        setupSelectedTable();

        termSelector.setItems(Data.getInstance().getSemesterList());
        String currentTerm = Data.getInstance().getCurrentTerm();
        if (currentTerm != null && !currentTerm.isEmpty()) {
            termSelector.setValue(currentTerm);
        } else if (!termSelector.getItems().isEmpty()) {
            termSelector.setValue(termSelector.getItems().get(0));
        }

        termSelector.setOnAction(e -> refreshAll());
        searchBtn.setOnAction(e -> performSearch());
        searchField.setOnAction(e -> performSearch());

        availableTable.setItems(availableCourses);
        selectedTable.setItems(selectedCourses);

        if (termSelector.getValue() != null) {
            refreshAll();
        }
    }

    private void setupAvailableTable() {
        availColName.setCellValueFactory(new PropertyValueFactory<>("code"));
        availColTeacher.setCellValueFactory(new PropertyValueFactory<>("teacherName"));
        availColCredit.setCellValueFactory(new PropertyValueFactory<>("credit"));
        availColType.setCellValueFactory(new PropertyValueFactory<>("type"));
        availColCapacity.setCellValueFactory(new PropertyValueFactory<>("classNum"));
        availColEnrolled.setCellValueFactory(new PropertyValueFactory<>("peopleNum"));
        availColTime.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getTerm() != null ? cellData.getValue().getTerm() : ""));

        availColAction.setCellFactory(col -> new TableCell<>() {
            private final Button selectBtn = new Button("选课");
            {
                selectBtn.getStyleClass().add("action-btn-select");
                selectBtn.setOnAction(e -> {
                    Course course = getTableView().getItems().get(getIndex());
                    handleSelectCourse(course);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(selectBtn);
                }
            }
        });
    }

    private void setupSelectedTable() {
        selColName.setCellValueFactory(new PropertyValueFactory<>("code"));
        selColTeacher.setCellValueFactory(new PropertyValueFactory<>("teacherName"));
        selColCredit.setCellValueFactory(new PropertyValueFactory<>("credit"));
        selColType.setCellValueFactory(new PropertyValueFactory<>("type"));
        selColTime.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getTerm() != null ? cellData.getValue().getTerm() : ""));

        selColAction.setCellFactory(col -> new TableCell<>() {
            private final Button dropBtn = new Button("退课");
            {
                dropBtn.getStyleClass().add("action-btn-drop");
                dropBtn.setOnAction(e -> {
                    Course course = getTableView().getItems().get(getIndex());
                    handleDropCourse(course);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(dropBtn);
                }
            }
        });
    }

    private void handleSelectCourse(Course course) {
        boolean confirmed = ShowMessage.showConfirmMessage("确认选课",
                "确定要选择课程 \"" + course.getCode() + "\" 吗？");
        if (!confirmed) return;

        JsonObject body = new JsonObject();
        body.addProperty("studentId", UserSession.getInstance().getId());
        body.addProperty("courseId", course.getCode());

        NetworkUtils.put("/course-selection/select/" + course.getCode(), gson.toJson(body), new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    Platform.runLater(() -> {
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            ShowMessage.showInfoMessage("选课成功", "课程 \"" + course.getCode() + "\" 选课成功！");
                            refreshAll();
                        } else {
                            String msg = res.has("msg") ? res.get("msg").getAsString() : "选课失败";
                            ShowMessage.showErrorMessage("选课失败", msg);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("选课失败", "响应解析失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("选课失败", e.getMessage()));
            }
        });
    }

    private void handleDropCourse(Course course) {
        boolean confirmed = ShowMessage.showConfirmMessage("确认退课",
                "确定要退选课程 \"" + course.getCode() + "\" 吗？");
        if (!confirmed) return;

        JsonObject body = new JsonObject();
        body.addProperty("studentId", UserSession.getInstance().getId());
        body.addProperty("courseId", course.getCode());

        NetworkUtils.delete("/course-selection/drop/" + course.getCode(), gson.toJson(body), new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    Platform.runLater(() -> {
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            ShowMessage.showInfoMessage("退课成功", "课程 \"" + course.getCode() + "\" 退课成功！");
                            refreshAll();
                        } else {
                            String msg = res.has("msg") ? res.get("msg").getAsString() : "退课失败";
                            ShowMessage.showErrorMessage("退课失败", msg);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("退课失败", "响应解析失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("退课失败", e.getMessage()));
            }
        });
    }

    private void performSearch() {
        String keyword = searchField.getText();
        String term = termSelector.getValue();
        if (term == null || term.isEmpty()) {
            ShowMessage.showWarningMessage("提示", "请先选择学期");
            return;
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            loadAvailableCourses(term);
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("keyword", keyword.trim());
        params.put("term", term);

        NetworkUtils.get("/course-selection/search", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = res.getAsJsonArray("data");
                        Platform.runLater(() -> {
                            availableCourses.clear();
                            availableCourses.addAll(parseCourseArray(arr));
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("搜索失败", "数据解析失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("搜索失败", e.getMessage()));
            }
        });
    }

    private void refreshAll() {
        String term = termSelector.getValue();
        if (term == null || term.isEmpty()) return;
        loadAvailableCourses(term);
        loadSelectedCourses(term);
    }

    private void loadAvailableCourses(String term) {
        Map<String, String> params = new HashMap<>();
        params.put("term", term);

        NetworkUtils.get("/course-selection/unChoose", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = res.getAsJsonArray("data");
                        Platform.runLater(() -> {
                            availableCourses.clear();
                            availableCourses.addAll(parseCourseArray(arr));
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        availableCourses.clear();
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    availableCourses.clear();
                });
            }
        });
    }

    private void loadSelectedCourses(String term) {
        Map<String, String> params = new HashMap<>();
        params.put("term", term);

        NetworkUtils.get("/course-selection/results", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = res.getAsJsonArray("data");
                        Platform.runLater(() -> {
                            selectedCourses.clear();
                            selectedCourses.addAll(parseCourseArray(arr));
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        selectedCourses.clear();
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    selectedCourses.clear();
                });
            }
        });
    }

    private ObservableList<Course> parseCourseArray(JsonArray arr) {
        ObservableList<Course> list = FXCollections.observableArrayList();
        for (int i = 0; i < arr.size(); i++) {
            JsonObject obj = arr.get(i).getAsJsonObject();
            Course course = new Course();
            if (obj.has("code")) course.setCode(obj.get("code").getAsString());
            if (obj.has("name")) course.setCode(obj.get("name").getAsString());
            if (obj.has("courseName")) course.setCode(obj.get("courseName").getAsString());
            if (obj.has("teacherName")) course.setTeacherName(obj.get("teacherName").getAsString());
            if (obj.has("teacher")) course.setTeacherName(obj.get("teacher").getAsString());
            if (obj.has("credit")) course.setCredit(obj.get("credit").getAsDouble());
            if (obj.has("type")) course.setType(obj.get("type").getAsString());
            if (obj.has("classNum")) course.setClassNum(obj.get("classNum").getAsInt());
            if (obj.has("peopleNum")) course.setPeopleNum(obj.get("peopleNum").getAsInt());
            if (obj.has("term")) course.setTerm(obj.get("term").getAsString());
            if (obj.has("status")) course.setStatus(obj.get("status").getAsString());
            if (obj.has("department")) course.setDepartment(obj.get("department").getAsString());
            if (obj.has("teacher")) course.setTeacher(obj.get("teacher").getAsString());
            list.add(course);
        }
        return list;
    }
}
