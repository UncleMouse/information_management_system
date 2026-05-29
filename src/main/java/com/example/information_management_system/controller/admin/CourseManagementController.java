package com.example.information_management_system.controller.admin;

import com.example.information_management_system.entity.Data;
import com.example.information_management_system.model.Course;
import com.example.information_management_system.util.JsonUtil;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.lang.reflect.Type;
import java.util.*;

public class CourseManagementController {

    private final Gson gson = new Gson();
    private final ObservableList<Course> courseList = FXCollections.observableArrayList();

    @FXML private TableView<Course> courseTable;
    @FXML private TableColumn<Course, String> colCode;
    @FXML private TableColumn<Course, String> colName;
    @FXML private TableColumn<Course, String> colTeacher;
    @FXML private TableColumn<Course, Double> colCredit;
    @FXML private TableColumn<Course, String> colType;
    @FXML private TableColumn<Course, String> colTerm;
    @FXML private TableColumn<Course, String> colStatus;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> semesterFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button btnSearch;
    @FXML private Button btnApprove;
    @FXML private Button btnReject;
    @FXML private Button btnRefresh;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        courseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        final double[] courseRatios = {1.0, 1.8, 1.0, 0.6, 0.8, 1.0, 0.8};
        final double courseTotalRatio = java.util.Arrays.stream(courseRatios).sum();
        courseTable.widthProperty().addListener((_obs, oldW, newW) -> {
            double w = newW.doubleValue() - 2;
            for (int i = 0; i < courseRatios.length && i < courseTable.getColumns().size(); i++)
                courseTable.getColumns().get(i).setPrefWidth(w * courseRatios[i] / courseTotalRatio);
        });
        setupTableColumns();
        courseTable.setItems(courseList);
        setupFilters();

        btnSearch.setOnAction(e -> searchCourses());
        btnApprove.setOnAction(e -> handleApprove());
        btnReject.setOnAction(e -> handleReject());
        btnRefresh.setOnAction(e -> loadCourses());

        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) searchCourses();
        });

        courseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            btnApprove.setDisable(!hasSelection);
            btnReject.setDisable(!hasSelection);
        });
        btnApprove.setDisable(true);
        btnReject.setDisable(true);

        loadCourses();
    }

    private void setupTableColumns() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        // Course 模型没有 "name" 属性，用 code 代替课程名称
        colName.setCellValueFactory(new PropertyValueFactory<>("code"));
        colTeacher.setCellValueFactory(new PropertyValueFactory<>("teacherName"));
        colCredit.setCellValueFactory(new PropertyValueFactory<>("credit"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colTerm.setCellValueFactory(new PropertyValueFactory<>("term"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colStatus.setCellFactory(column -> new TableCell<Course, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("已通过".equals(item) || "APPROVED".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    } else if ("待审核".equals(item) || "PENDING".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    } else if ("已拒绝".equals(item) || "REJECTED".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #64748b;");
                    }
                }
            }
        });
    }

    private void setupFilters() {
        semesterFilter.setItems(Data.getInstance().getSemesterList());
        if (!semesterFilter.getItems().isEmpty()) {
            semesterFilter.getSelectionModel().selectFirst();
        }
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                "全部", "待审核", "已通过", "已拒绝");
        statusFilter.setItems(statusOptions);
        statusFilter.getSelectionModel().selectFirst();
    }

    private void loadCourses() {
        statusLabel.setText("加载中…");
        NetworkUtils.get("/class/pending", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        Type listType = new TypeToken<List<Course>>() {}.getType();
                        List<Course> list = gson.fromJson(arr, listType);
                        Platform.runLater(() -> {
                            courseList.setAll(list);
                            statusLabel.setText("共 " + list.size() + " 条");
                        });
                    } else {
                        Platform.runLater(() -> statusLabel.setText("数据加载失败"));
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> statusLabel.setText("数据解析失败，请稍后重试"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("网络请求失败，请检查连接");
                    ShowMessage.showErrorMessage("错误", "数据加载失败: " + e.getMessage());
                });
            }
        });
    }

    private void searchCourses() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadCourses();
            return;
        }
        statusLabel.setText("加载中…");
        Map<String, String> params = new HashMap<>();
        params.put("keyword", keyword);
        if (semesterFilter.getValue() != null && !"全部".equals(semesterFilter.getValue())) {
            params.put("semester", semesterFilter.getValue());
        }
        if (statusFilter.getValue() != null && !"全部".equals(statusFilter.getValue())) {
            params.put("status", statusFilter.getValue());
        }
        NetworkUtils.get("/class/pending", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        Type listType = new TypeToken<List<Course>>() {}.getType();
                        List<Course> list = gson.fromJson(arr, listType);
                        Platform.runLater(() -> {
                            courseList.setAll(list);
                            statusLabel.setText("共 " + list.size() + " 条");
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> statusLabel.setText("数据加载失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> statusLabel.setText("数据加载失败"));
            }
        });
    }

    private void handleApprove() {
        Course selected = courseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowMessage.showWarningMessage("提示", "请先选择一门课程");
            return;
        }
        reviewCourse(selected, true);
    }

    private void handleReject() {
        Course selected = courseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ShowMessage.showWarningMessage("提示", "请先选择一门课程");
            return;
        }
        reviewCourse(selected, false);
    }

    private void reviewCourse(Course course, boolean approve) {
        String action = approve ? "通过" : "拒绝";
        boolean confirmed = ShowMessage.showConfirmMessage("确认",
                "确定要" + action + "课程 " + course.getCode() + " 吗？");
        if (!confirmed) return;

        statusLabel.setText("加载中…");
        Map<String, Object> body = new HashMap<>();
        body.put("courseId", course.getCode());
        body.put("status", approve ? "APPROVED" : "REJECTED");

        String json = gson.toJson(body);
        NetworkUtils.post("/class/approve/" + course.getCode(), json, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        Platform.runLater(() -> {
                            ShowMessage.showInfoMessage("成功", "已成功更新");
                            loadCourses();
                        });
                    } else {
                        Platform.runLater(() -> ShowMessage.showErrorMessage("错误",
                                res.has("msg") ? res.get("msg").getAsString() : "操作失败，请稍后重试"));
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "解析响应失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("数据加载失败");
                    ShowMessage.showErrorMessage("错误", "操作失败，请稍后重试");
                });
            }
        });
    }
}
