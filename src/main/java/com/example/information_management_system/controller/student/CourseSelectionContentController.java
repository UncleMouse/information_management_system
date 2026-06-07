package com.example.information_management_system.controller.student;

import com.example.information_management_system.entity.Data;
import com.example.information_management_system.model.Course;
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
import javafx.scene.control.*;

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
        selectedTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupAvailableTable();
        setupSelectedTable();

        // 初始化学期列表
        if (!Data.getInstance().getSemesterList().isEmpty()) {
            termSelector.setItems(Data.getInstance().getSemesterList());
            String ct = Data.getInstance().getCurrentTerm();
            termSelector.setValue(ct != null && !ct.isEmpty() ? ct : termSelector.getItems().get(0));
        }
        // 兜底：实时获取学期列表
        fetchTerms();

        termSelector.setOnAction(e -> refreshAll());
        searchBtn.setOnAction(e -> performSearch());
        searchField.setOnAction(e -> performSearch());

        availableTable.setItems(availableCourses);
        selectedTable.setItems(selectedCourses);

    }

    private void setupAvailableTable() {
        availColName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCode()));
        availColTeacher.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTeacherName()));
        availColCredit.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getCredit()).asObject());
        availColType.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getType()));
        availColCapacity.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getClassNum()).asObject());
        availColEnrolled.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getPeopleNum()).asObject());
        availColTime.setCellValueFactory(cell -> new SimpleStringProperty(formatTimeDisplay(cell.getValue().getTerm())));

        // 居中
        centerCell(availColName); centerCell(availColTeacher); centerCell(availColCredit);
        centerCell(availColType); centerCell(availColCapacity); centerCell(availColEnrolled);
        centerCell(availColTime);

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

    @SuppressWarnings("unchecked")
    private <T> void centerCell(TableColumn<Course, T> col) {
        col.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(T item, boolean empty) { super.updateItem(item, empty); setText(empty||item==null?null:item.toString()); setStyle("-fx-alignment: CENTER;"); }
        });
    }

    private void setupSelectedTable() {
        selColName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCode()));
        selColTeacher.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTeacherName()));
        selColCredit.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getCredit()).asObject());
        selColType.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getType()));
        selColTime.setCellValueFactory(cell -> new SimpleStringProperty(formatTimeDisplay(cell.getValue().getTerm())));
        centerCell(selColName); centerCell(selColTeacher); centerCell(selColCredit);
        centerCell(selColType); centerCell(selColTime);

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
                        JsonArray arr = JsonUtil.extractArray(res, "data");
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

    private void fetchTerms() {
        NetworkUtils.get("/term/getTermList", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = res.getAsJsonArray("data");
                        javafx.collections.ObservableList<String> list = FXCollections.observableArrayList();
                        for (int i = 0; i < arr.size(); i++)
                            list.add(arr.get(i).getAsJsonObject().get("term").getAsString());
                        Platform.runLater(() -> {
                            termSelector.setItems(list);
                            Data.getInstance().getSemesterList().setAll(list);
                            if (termSelector.getValue() == null && !list.isEmpty())
                                termSelector.setValue(list.get(0));
                            refreshAll();
                        });
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Exception ignored) {}
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
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        Platform.runLater(() -> {
                            availableCourses.clear();
                            availableCourses.addAll(parseCourseArray(arr));
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> availableCourses.clear());
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> availableCourses.clear());
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
                        JsonArray arr = JsonUtil.extractArray(res, "data");
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

    private static final String[] DAY_NAMES = {"周一","周二","周三","周四","周五","周六","周日"};
    private static final String[] TIME_NAMES = {"1-2节","3-4节","5-6节","7-8节","9-10节"};

    private String formatTimeDisplay(String rawTime) {
        if (rawTime == null || rawTime.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String part : rawTime.split(",")) {
            try {
                int gs = Integer.parseInt(part.trim());
                int d = gs / 5, s = gs % 5;
                if (d >= 0 && d < DAY_NAMES.length && s >= 0 && s < TIME_NAMES.length) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(DAY_NAMES[d]).append(" ").append(TIME_NAMES[s]);
                }
            } catch (NumberFormatException ignored) {}
        }
        return sb.toString();
    }

    private ObservableList<Course> parseCourseArray(JsonArray arr) {
        ObservableList<Course> list = FXCollections.observableArrayList();
        for (int i = 0; i < arr.size(); i++) {
            JsonObject obj = arr.get(i).getAsJsonObject();
            Course course = new Course();
            course.setId(obj.has("id") ? obj.get("id").getAsInt() : 0);
            course.setCode(obj.has("name") ? obj.get("name").getAsString()
                    : obj.has("code") ? obj.get("code").getAsString() : "");
            course.setTeacherName(JsonUtil.safeGetString(obj, "teacherName"));
            if (obj.has("point")) course.setCredit(obj.get("point").getAsDouble());
            else if (obj.has("credit")) course.setCredit(obj.get("credit").getAsDouble());
            if (obj.has("type")) course.setType(obj.get("type").getAsString());
            if (obj.has("classNum")) course.setTeacher(obj.get("classNum").getAsString());  // 课序号存到 teacher 字段展示
            if (obj.has("capacity")) course.setClassNum(obj.get("capacity").getAsInt());
            if (obj.has("selectedCount")) course.setPeopleNum(obj.get("selectedCount").getAsInt());
            else if (obj.has("peopleNum")) course.setPeopleNum(obj.get("peopleNum").getAsInt());
            if (obj.has("term")) course.setTerm(obj.get("term").getAsString());
            if (obj.has("time")) course.setTerm(obj.get("time").getAsString());
            if (obj.has("status")) course.setStatus(obj.get("status").getAsString());
            // 只显示已排课的课程（有时间+教室）
            String t = JsonUtil.safeGetString(obj, "time");
            String cr = JsonUtil.safeGetString(obj, "classroom");
            if (t.isEmpty() && cr.isEmpty()) continue;
            list.add(course);
        }
        return list;
    }
}
