package com.example.information_management_system.controller.student;

import com.example.information_management_system.entity.Data;
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
        availableTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        final double[] availRatios = {2.0, 1.2, 0.7, 1.0, 0.8, 0.7, 1.8, 0.8};
        final double availTotalRatio = java.util.Arrays.stream(availRatios).sum();
        availableTable.widthProperty().addListener((obs, oldW, newW) -> {
            double w = newW.doubleValue() - 2;
            for (int i = 0; i < availRatios.length && i < availableTable.getColumns().size(); i++)
                availableTable.getColumns().get(i).setPrefWidth(w * availRatios[i] / availTotalRatio);
        });
        selectedTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        final double[] selRatios = {2.5, 1.2, 0.7, 1.0, 2.5, 0.8};
        final double selTotalRatio = java.util.Arrays.stream(selRatios).sum();
        selectedTable.widthProperty().addListener((obs, oldW, newW) -> {
            double w = newW.doubleValue() - 2;
            for (int i = 0; i < selRatios.length && i < selectedTable.getColumns().size(); i++)
                selectedTable.getColumns().get(i).setPrefWidth(w * selRatios[i] / selTotalRatio);
        });
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
        availColTime.setCellValueFactory(new PropertyValueFactory<>("term"));

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
        selColTime.setCellValueFactory(new PropertyValueFactory<>("term"));

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
        boolean confirmed = ShowMessage.showConfirmMessage("确认",
                "确定要选择课程 \"" + course.getCode() + "\" 吗？");
        if (!confirmed) return;

        int courseId = course.getId();
        NetworkUtils.post("/course-selection/select/" + courseId, "", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    Platform.runLater(() -> {
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            ShowMessage.showInfoMessage("成功", "已成功选课");
                            refreshAll();
                        } else {
                            String msg = res.has("msg") ? res.get("msg").getAsString() : "操作失败，请稍后重试";
                            ShowMessage.showErrorMessage("错误", msg);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "数据解析失败，请稍后重试"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "网络请求失败，请检查连接"));
            }
        });
    }

    private void handleDropCourse(Course course) {
        boolean confirmed = ShowMessage.showConfirmMessage("确认",
                "确定要退选课程 \"" + course.getCode() + "\" 吗？");
        if (!confirmed) return;

        int courseId = course.getId();
        NetworkUtils.post("/course-selection/drop/" + courseId, "", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    Platform.runLater(() -> {
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            ShowMessage.showInfoMessage("成功", "已成功退课");
                            refreshAll();
                        } else {
                            String msg = res.has("msg") ? res.get("msg").getAsString() : "操作失败，请稍后重试";
                            ShowMessage.showErrorMessage("错误", msg);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "数据解析失败，请稍后重试"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "网络请求失败，请检查连接"));
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
                    Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "数据解析失败，请稍后重试"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "网络请求失败，请检查连接"));
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
            course.setId(obj.has("id") ? obj.get("id").getAsInt() : 0);
            course.setCode(obj.has("name") ? obj.get("name").getAsString()
                    : obj.has("code") ? obj.get("code").getAsString() : "");
            if (obj.has("teacherName")) course.setTeacherName(obj.get("teacherName").getAsString());
            if (obj.has("credit")) course.setCredit(obj.has("point") ? obj.get("point").getAsDouble()
                    : obj.get("credit").getAsDouble());
            if (obj.has("type")) course.setType(obj.get("type").getAsString());
            if (obj.has("capacity")) course.setClassNum(obj.get("capacity").getAsInt());
            if (obj.has("classNum")) course.setClassNum(obj.get("classNum").getAsInt());
            if (obj.has("peopleNum")) course.setPeopleNum(obj.get("peopleNum").getAsInt());
            if (obj.has("selectedCount")) course.setPeopleNum(obj.get("selectedCount").getAsInt());
            if (obj.has("term")) course.setTerm(obj.get("term").getAsString());
            if (obj.has("time")) course.setTerm(obj.get("time").getAsString());
            if (obj.has("status")) course.setStatus(obj.get("status").getAsString());
            list.add(course);
        }
        return list;
    }
}
