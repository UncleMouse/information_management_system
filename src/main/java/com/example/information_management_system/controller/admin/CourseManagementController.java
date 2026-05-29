package com.example.information_management_system.controller.admin;

import com.example.information_management_system.entity.Data;
import com.example.information_management_system.model.Course;
import com.example.information_management_system.util.JsonUtil;
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
        colCode.setCellFactory(col -> new TableCell<Course, String>() {
            @Override protected void updateItem(String item, boolean empty) { super.updateItem(item, empty); setText(empty?null:String.valueOf(getIndex()+1)); setStyle("-fx-alignment: CENTER;"); }
        });
        colName.setCellValueFactory(new PropertyValueFactory<>("code"));
        colTeacher.setCellValueFactory(new PropertyValueFactory<>("teacherName"));
        colCredit.setCellValueFactory(new PropertyValueFactory<>("credit"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colTerm.setCellValueFactory(new PropertyValueFactory<>("term"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colCredit.setCellFactory(col -> new TableCell<Course, Double>() {
            @Override protected void updateItem(Double item, boolean empty) { super.updateItem(item, empty); setText(empty||item==null?null:String.valueOf(item)); setStyle("-fx-alignment: CENTER;"); }
        });
        colType.setCellFactory(col -> new TableCell<Course, String>() {
            @Override protected void updateItem(String item, boolean empty) { super.updateItem(item, empty); setText(empty||item==null?null:item); setStyle("-fx-alignment: CENTER;"); }
        });
        colTerm.setCellFactory(col -> new TableCell<Course, String>() {
            @Override protected void updateItem(String item, boolean empty) { super.updateItem(item, empty); setText(empty||item==null?null:item); setStyle("-fx-alignment: CENTER;"); }
        });
        colStatus.setCellFactory(column -> new TableCell<Course, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if ("已通过".equals(item) || "APPROVED".equalsIgnoreCase(item)) {
                    setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-alignment: CENTER;");
                } else if ("待审核".equals(item) || "PENDING".equalsIgnoreCase(item)) {
                    setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-alignment: CENTER;");
                } else if ("已拒绝".equals(item) || "REJECTED".equalsIgnoreCase(item)) {
                    setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-alignment: CENTER;");
                } else {
                    setStyle("-fx-text-fill: #64748b; -fx-alignment: CENTER;");
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
        Map<String, String> params = new HashMap<>();
        params.put("pageNum", "1");
        params.put("pageSize", "100");

        NetworkUtils.get("/class/list", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        List<Course> list = new ArrayList<>();
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject obj = arr.get(i).getAsJsonObject();
                            Course c = new Course();
                            // 使用 name 作为课程名（显示在 code 字段）
                            if (obj.has("name")) {
                                c.setCode(obj.get("name").getAsString());
                            } else if (obj.has("courseId")) {
                                c.setCode(String.valueOf(obj.get("courseId").getAsInt()));
                            }
                            c.setTeacherName(obj.has("teacherName") ? obj.get("teacherName").getAsString() : "");
                            c.setCredit(obj.has("credit") ? obj.get("credit").getAsDouble() : 0.0);
                            c.setType(obj.has("type") ? obj.get("type").getAsString() : "");
                            c.setTerm(obj.has("term") ? obj.get("term").getAsString() : "");
                            c.setStatus(obj.has("status") ? obj.get("status").getAsString() : "");
                            list.add(c);
                        }
                        Platform.runLater(() -> {
                            courseList.setAll(list);
                            statusLabel.setText("共 " + list.size() + " 条");
                        });
                    } else {
                        Platform.runLater(() -> {
                            statusLabel.setText("数据加载失败");
                            ShowMessage.showErrorMessage("错误", "数据加载失败: " + (res.has("msg") ? res.get("msg").getAsString() : "未知错误"));
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        statusLabel.setText("数据解析失败");
                        ShowMessage.showErrorMessage("错误", "数据解析失败: " + e.getMessage());
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("网络请求失败");
                    ShowMessage.showErrorMessage("错误", "网络请求失败: " + e.getMessage());
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
        params.put("pageNum", "1");
        params.put("pageSize", "100");
        if (semesterFilter.getValue() != null && !"全部".equals(semesterFilter.getValue())) {
            params.put("term", semesterFilter.getValue());
        }
        NetworkUtils.get("/class/list", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        List<Course> list = new ArrayList<>();
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject obj = arr.get(i).getAsJsonObject();
                            Course c = new Course();
                            if (obj.has("name")) {
                                c.setCode(obj.get("name").getAsString());
                            } else if (obj.has("courseId")) {
                                c.setCode(String.valueOf(obj.get("courseId").getAsInt()));
                            }
                            c.setTeacherName(obj.has("teacherName") ? obj.get("teacherName").getAsString() : "");
                            c.setCredit(obj.has("credit") ? obj.get("credit").getAsDouble() : 0.0);
                            c.setType(obj.has("type") ? obj.get("type").getAsString() : "");
                            c.setTerm(obj.has("term") ? obj.get("term").getAsString() : "");
                            c.setStatus(obj.has("status") ? obj.get("status").getAsString() : "");
                            list.add(c);
                        }
                        Platform.runLater(() -> {
                            courseList.setAll(list);
                            statusLabel.setText("共 " + list.size() + " 条");
                        });
                    } else {
                        Platform.runLater(() -> {
                            statusLabel.setText("搜索失败");
                            ShowMessage.showErrorMessage("错误", "搜索失败: " + (res.has("msg") ? res.get("msg").getAsString() : "未知错误"));
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        statusLabel.setText("数据解析失败");
                        ShowMessage.showErrorMessage("错误", "数据解析失败: " + e.getMessage());
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("网络请求失败");
                    ShowMessage.showErrorMessage("错误", "网络请求失败: " + e.getMessage());
                });
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

        statusLabel.setText("加载中...");
        int courseId = course.getId();

        // 后端期望: status=1(通过) 或 2(拒绝) 作为查询参数, JSON body 为 sectionId 数组
        Map<String, String> params = new HashMap<>();
        params.put("status", approve ? "1" : "2");

        String json = "[]"; // 空的 sectionId 数组
        String endpoint = "/class/approve/" + courseId;

        NetworkUtils.post(endpoint, params, json, new NetworkUtils.Callback<String>() {
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
                                res.has("msg") ? res.get("msg").getAsString() : "操作失败"));
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "解析响应失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("操作失败");
                    ShowMessage.showErrorMessage("错误", "操作失败: " + e.getMessage());
                });
            }
        });
    }
}
