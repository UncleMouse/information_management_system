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
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;

import java.io.IOException;
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
    @FXML private Button btnSchedule;
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
        if (btnSchedule != null) btnSchedule.setOnAction(e -> handleAutoSchedule());
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
        colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCode()));
        colTeacher.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTeacherName()));
        colCredit.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getCredit()).asObject());
        colType.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getType()));
        colTerm.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTerm()));
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));

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
        semesterFilter.getItems().add(0, "全部");
        semesterFilter.getSelectionModel().selectFirst();
        semesterFilter.setOnAction(e -> applyFilters());
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                "全部", "待审核", "已通过");
        statusFilter.setItems(statusOptions);
        statusFilter.getSelectionModel().selectFirst();
        statusFilter.setOnAction(e -> applyFilters());
    }

    private void applyFilters() {
        String term = semesterFilter.getValue();
        String statusVal = statusFilter.getValue();
        ObservableList<Course> filtered = FXCollections.observableArrayList();
        for (Course c : courseList) {
            boolean termOk = term == null || "全部".equals(term) ||
                    (c.getTerm() != null && c.getTerm().equals(term));
            boolean statusOk = statusVal == null || "全部".equals(statusVal) ||
                    ("待审核".equals(statusVal) && "PENDING".equalsIgnoreCase(c.getStatus())) ||
                    ("已通过".equals(statusVal) && "APPROVED".equalsIgnoreCase(c.getStatus()));
            if (termOk && statusOk) filtered.add(c);
        }
        courseTable.setItems(filtered);
    }

    private void loadCourses() {
        statusLabel.setText("加载中…");
        // 并行加载两个接口，合并去重
        new Thread(() -> {
            Map<Integer, Course> map = new HashMap<>();
            int[] done = {0};

            Map<String, String> clParams = new HashMap<>();
            clParams.put("pageSize", "200");
            NetworkUtils.get("/class/list", clParams, new NetworkUtils.Callback<String>() {
                @Override public void onSuccess(String result) { parseAndMerge(result, map, done); }
                @Override public void onFailure(Exception e) { checkDone(map, done); }
            });
            NetworkUtils.get("/class/pending", new NetworkUtils.Callback<String>() {
                @Override public void onSuccess(String result) { parseAndMerge(result, map, done); }
                @Override public void onFailure(Exception e) { checkDone(map, done); }
            });
        }).start();
    }

    private void parseAndMerge(String result, Map<Integer, Course> map, int[] done) {
        try {
            JsonObject res = gson.fromJson(result, JsonObject.class);
            if (res.has("code") && res.get("code").getAsInt() == 200) {
                JsonArray arr = JsonUtil.extractArray(res, "data");
                synchronized (map) {
                    for (int i = 0; i < arr.size(); i++) {
                        JsonObject obj = arr.get(i).getAsJsonObject();
                        int id = JsonUtil.safeGetInt(obj, "id");
                        if (map.containsKey(id)) continue;
                        Course c = new Course();
                        c.setId(id);
                        c.setCode(JsonUtil.safeGetString(obj, "name"));
                        c.setTeacherName(JsonUtil.safeGetString(obj, "teacherName"));
                        c.setCredit(obj.has("point") && !obj.get("point").isJsonNull() ? obj.get("point").getAsDouble() : 0.0);
                        c.setType(JsonUtil.safeGetString(obj, "type"));
                        c.setTerm(JsonUtil.safeGetString(obj, "term"));
                        c.setStatus(JsonUtil.safeGetString(obj, "status"));
                        map.put(id, c);
                    }
                }
            }
        } catch (Exception ignored) {}
        checkDone(map, done);
    }

    private void checkDone(Map<Integer, Course> map, int[] done) {
        synchronized (done) { done[0]++; if (done[0] < 2) return; }
        Platform.runLater(() -> {
            courseList.setAll(FXCollections.observableArrayList(map.values()));
            statusLabel.setText("共 " + map.size() + " 条");
        });
    }

    private void searchCourses() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            courseTable.setItems(courseList);
            return;
        }
        ObservableList<Course> filtered = FXCollections.observableArrayList();
        for (Course c : courseList) {
            if ((c.getCode() != null && c.getCode().toLowerCase().contains(keyword))
                    || (c.getTeacherName() != null && c.getTeacherName().toLowerCase().contains(keyword))
                    || (c.getType() != null && c.getType().toLowerCase().contains(keyword))) {
                filtered.add(c);
            }
        }
        courseTable.setItems(filtered);
    }

    private void handleApprove() {
        Course selected = courseTable.getSelectionModel().getSelectedItem();
        if (selected == null) { ShowMessage.showWarningMessage("提示", "请先选择一门课程"); return; }
        reviewCourse(selected, true, null);
    }

    private void handleAutoSchedule() {
        try {
            javafx.scene.layout.Pane contentArea = (javafx.scene.layout.Pane)
                    statusLabel.getScene().lookup("#contentArea");
            if (contentArea != null) {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/information_management_system/admin/ScheduleManagement.fxml"));
                javafx.scene.Parent view = loader.load();
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
            }
        } catch (IOException e) {
            ShowMessage.showErrorMessage("错误", "无法打开排课界面");
        }
    }

    private void handleReject() {
        Course selected = courseTable.getSelectionModel().getSelectedItem();
        if (selected == null) { ShowMessage.showWarningMessage("提示", "请先选择一门课程"); return; }
        // 弹出输入框填写拒绝理由
        TextInputDialog d = new TextInputDialog();
        d.setTitle("拒绝理由"); d.setHeaderText("请输入拒绝 " + selected.getCode() + " 的理由"); d.setContentText("理由:");
        d.showAndWait().ifPresent(reason -> {
            if (reason.trim().isEmpty()) { ShowMessage.showWarningMessage("提示", "拒绝理由不能为空"); return; }
            reviewCourse(selected, false, reason.trim());
        });
    }

    private void reviewCourse(Course course, boolean approve, String reason) {
        String action = approve ? "通过" : "拒绝";
        boolean confirmed = ShowMessage.showConfirmMessage("确认",
                "确定要" + action + "课程 " + course.getCode() + " 吗？");
        if (!confirmed) return;

        statusLabel.setText("加载中...");
        int courseId = course.getId();

        Map<String, String> params = new HashMap<>();
        params.put("status", approve ? "1" : "2");
        if (!approve && reason != null) params.put("reason", reason);

        String json = "[]";
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
