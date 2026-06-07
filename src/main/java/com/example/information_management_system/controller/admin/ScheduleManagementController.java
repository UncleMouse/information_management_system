package com.example.information_management_system.controller.admin;

import com.example.information_management_system.entity.Data;
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
import java.util.*;

public class ScheduleManagementController {

    private final Gson gson = new Gson();

    @FXML private ComboBox<String> termSelector;
    @FXML private Button btnLoad;
    @FXML private Button btnSaveAll;
    @FXML private Button backButton;
    @FXML private TableView<CourseScheduleRow> scheduleTable;
    @FXML private TableColumn<CourseScheduleRow, String> colName;
    @FXML private TableColumn<CourseScheduleRow, String> colTeacher;
    @FXML private TableColumn<CourseScheduleRow, String> colDay;
    @FXML private TableColumn<CourseScheduleRow, String> colStatus;
    @FXML private TableColumn<CourseScheduleRow, String> colTime;
    @FXML private TableColumn<CourseScheduleRow, String> colClassroom;
    @FXML private TableColumn<CourseScheduleRow, String> colAction;

    private final ObservableList<CourseScheduleRow> rows = FXCollections.observableArrayList();
    private final ObservableList<String> roomList = FXCollections.observableArrayList();

    private static final List<String> DAYS = Arrays.asList("周一","周二","周三","周四","周五","周六","周日");
    private static final List<String> TIMES = Arrays.asList("1-2节","3-4节","5-6节","7-8节","9-10节");

    public static class CourseScheduleRow {
        final int courseId;
        JsonObject rawData;
        final SimpleStringProperty name = new SimpleStringProperty();
        final SimpleStringProperty teacher = new SimpleStringProperty();
        final SimpleStringProperty status = new SimpleStringProperty();
        final SimpleStringProperty day = new SimpleStringProperty("");
        final SimpleStringProperty time = new SimpleStringProperty("");
        final SimpleStringProperty classroom = new SimpleStringProperty("");
        public CourseScheduleRow(int id, String n, String t, String s, JsonObject raw) { courseId = id; name.set(n); teacher.set(t); status.set(s); rawData = raw; }
        public String getName() { return name.get(); }
        public String getTeacher() { return teacher.get(); }
        public String getStatus() { return status.get(); }
        public String getDay() { return day.get(); }
        public String getTime() { return time.get(); }
        public String getClassroom() { return classroom.get(); }
    }

    @FXML
    public void initialize() {
        scheduleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        scheduleTable.setItems(rows);

        colName.setCellValueFactory(cell -> cell.getValue().name);
        colTeacher.setCellValueFactory(cell -> cell.getValue().teacher);
        colStatus.setCellValueFactory(cell -> cell.getValue().status);

        // Day column - editable ComboBox
        colDay.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> cb = new ComboBox<>(FXCollections.observableArrayList(DAYS));
            { cb.getStyleClass().add("form-combo"); cb.setPrefWidth(90); cb.setOnAction(e -> { getTableRow().getItem().day.set(cb.getValue()); }); }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                cb.setValue(getTableRow().getItem().getDay());
                setGraphic(cb);
            }
        });
        colDay.setCellValueFactory(cell -> cell.getValue().day);

        // Time column - editable ComboBox
        colTime.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> cb = new ComboBox<>(FXCollections.observableArrayList(TIMES));
            { cb.getStyleClass().add("form-combo"); cb.setPrefWidth(100); cb.setOnAction(e -> { getTableRow().getItem().time.set(cb.getValue()); }); }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                cb.setValue(getTableRow().getItem().getTime());
                setGraphic(cb);
            }
        });
        colTime.setCellValueFactory(cell -> cell.getValue().time);

        // Classroom column - editable ComboBox
        colClassroom.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> cb = new ComboBox<>(roomList);
            { cb.getStyleClass().add("form-combo"); cb.setPrefWidth(140); cb.setEditable(true); cb.setOnAction(e -> { getTableRow().getItem().classroom.set(cb.getValue()); }); }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                cb.setValue(getTableRow().getItem().getClassroom());
                setGraphic(cb);
            }
        });
        colClassroom.setCellValueFactory(cell -> cell.getValue().classroom);

        // Action column - save button
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("保存");
            { btn.getStyleClass().add("btn-primary"); btn.setOnAction(e -> {
                CourseScheduleRow row = getTableRow().getItem();
                if (row != null) saveOne(row, () -> Platform.runLater(() -> ShowMessage.showInfoMessage("成功", "已保存")));
            });}
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // Init
        if (!Data.getInstance().getSemesterList().isEmpty()) {
            termSelector.setItems(Data.getInstance().getSemesterList());
            String ct = Data.getInstance().getCurrentTerm();
            termSelector.setValue(ct != null && !ct.isEmpty() ? ct : termSelector.getItems().get(0));
        }
        fetchTerms();
        fetchClassrooms();
        termSelector.setOnAction(e -> loadCourses());
        btnLoad.setOnAction(e -> loadCourses());
        btnSaveAll.setOnAction(e -> saveAll());
        if (backButton != null) backButton.setOnAction(e -> navigateBack());
    }

    private void fetchTerms() {
        NetworkUtils.get("/term/getTermList", new NetworkUtils.Callback<String>() {
            @Override public void onSuccess(String result) {
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
                            if (termSelector.getValue() == null && !list.isEmpty()) {
                                termSelector.setValue(list.get(0));
                                loadCourses();
                            }
                        });
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Exception ignored) {}
        });
    }

    private void fetchClassrooms() {
        NetworkUtils.get("/Teacher/getClassRoom", new NetworkUtils.Callback<String>() {
            @Override public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = res.getAsJsonArray("data");
                        Platform.runLater(() -> {
                            roomList.clear();
                            for (int i = 0; i < arr.size(); i++)
                                roomList.add(arr.get(i).getAsJsonObject().get("location").getAsString());
                            Data.getInstance().getClassRoomList().setAll(roomList);
                        });
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Exception e) {
                Platform.runLater(() -> roomList.setAll(Data.getInstance().getClassRoomList()));
            }
        });
    }

    private void loadCourses() {
        String term = termSelector.getValue();
        if (term == null || term.isEmpty()) return;

        Map<String, String> cp = new HashMap<>();
        cp.put("pageSize", "200");
        NetworkUtils.get("/class/list", cp, new NetworkUtils.Callback<String>() {
            @Override public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        int total = arr.size();
                        if (total == 0) { Platform.runLater(() -> rows.clear()); return; }
                        // 先收集基本信息，再逐门获取详情（/class/list 不含 time/classroom）
                        List<CourseScheduleRow> list = new ArrayList<>();
                        for (int i = 0; i < total; i++) {
                            JsonObject obj = arr.get(i).getAsJsonObject();
                            CourseScheduleRow row = new CourseScheduleRow(
                                JsonUtil.safeGetInt(obj, "id"),
                                JsonUtil.safeGetString(obj, "name"),
                                JsonUtil.safeGetString(obj, "teacherName"),
                                JsonUtil.safeGetString(obj, "status"),
                                obj);
                            list.add(row);
                        }
                        // 逐门获取详情，拿到完整的 time / classroom 等字段
                        final int[] loaded = {0};
                        for (CourseScheduleRow row : list) {
                            NetworkUtils.get("/class/detail/" + row.courseId, new NetworkUtils.Callback<String>() {
                                @Override public void onSuccess(String detailResult) {
                                    try {
                                        JsonObject dr = gson.fromJson(detailResult, JsonObject.class);
                                        if (dr.has("code") && dr.get("code").getAsInt() == 200) {
                                            JsonObject detail = dr.getAsJsonObject("data");
                                            // 用详情数据更新 rawData，以便 saveOne 能拿到完整字段
                                            row.rawData = detail;
                                            String t = JsonUtil.safeGetString(detail, "time");
                                            if (!t.isEmpty()) {
                                                String firstPart = t.contains(",") ? t.split(",")[0].trim() : t.trim();
                                                try {
                                                    int globalSlot = Integer.parseInt(firstPart);
                                                    int dayIdx = globalSlot / 5;
                                                    int slotIdx = globalSlot % 5;
                                                    if (dayIdx >= 0 && dayIdx < DAYS.size()) row.day.set(DAYS.get(dayIdx));
                                                    if (slotIdx >= 0 && slotIdx < TIMES.size()) row.time.set(TIMES.get(slotIdx));
                                                } catch (NumberFormatException ignored) {}
                                            }
                                            String cr = JsonUtil.safeGetString(detail, "classroom");
                                            if (!cr.isEmpty()) row.classroom.set(cr);
                                        }
                                    } catch (Exception ignored) {}
                                    synchronized (loaded) { loaded[0]++; if (loaded[0] >= list.size()) Platform.runLater(() -> rows.setAll(list)); }
                                }
                                @Override public void onFailure(Exception e) {
                                    synchronized (loaded) { loaded[0]++; if (loaded[0] >= list.size()) Platform.runLater(() -> rows.setAll(list)); }
                                }
                            });
                        }
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Exception ignored) {}
        });
    }

    private void saveOne(CourseScheduleRow row, Runnable onComplete) {
        String day = row.getDay();
        String time = row.getTime();
        String classroom = row.getClassroom();
        if (day == null || day.isEmpty() || time == null || time.isEmpty()) { if (onComplete != null) onComplete.run(); return; }

        int slotIdx = switch (time) {
            case "1-2节" -> 0; case "3-4节" -> 1; case "5-6节" -> 2;
            case "7-8节" -> 3; case "9-10节" -> 4; default -> 0;
        };
        int dayIdx = DAYS.indexOf(day);
        if (dayIdx < 0) dayIdx = 0;
        String combinedTime = String.valueOf(dayIdx * 5 + slotIdx);

        Map<String, Object> body = new HashMap<>();
        JsonObject r = row.rawData;
        body.put("name", JsonUtil.safeGetString(r, "name"));
        body.put("type", JsonUtil.safeGetString(r, "type"));
        body.put("point", r.has("point") ? r.get("point").getAsDouble() : 0);
        body.put("capacity", r.has("capacity") ? r.get("capacity").getAsInt() : 100);
        body.put("weekStart", r.has("weekStart") ? r.get("weekStart").getAsInt() : 1);
        body.put("weekEnd", r.has("weekEnd") ? r.get("weekEnd").getAsInt() : 16);
        body.put("period", r.has("period") ? r.get("period").getAsInt() : 16);
        body.put("college", JsonUtil.safeGetString(r, "college"));
        body.put("term", JsonUtil.safeGetString(r, "term"));
        body.put("classNum", JsonUtil.safeGetString(r, "classNum"));
        body.put("examination", r.has("examination") ? r.get("examination").getAsInt() : 1);
        body.put("regularRatio", r.has("regularRatio") ? r.get("regularRatio").getAsDouble() : 0.5);
        body.put("finalRatio", r.has("finalRatio") ? r.get("finalRatio").getAsDouble() : 0.5);
        body.put("status", JsonUtil.safeGetString(r, "status"));
        body.put("time", combinedTime);
        body.put("classroom", classroom != null ? classroom : "");

        String req = gson.toJson(body);
        NetworkUtils.post("/class/adUpdate/" + row.courseId, req, new NetworkUtils.Callback<String>() {
            @Override public void onSuccess(String result) {
                if (onComplete != null) onComplete.run();
            }
            @Override public void onFailure(Exception e) {
                if (onComplete != null) onComplete.run();
            }
        });
    }

    private void saveAll() {
        int total = rows.size();
        if (total == 0) { ShowMessage.showWarningMessage("提示", "没有可保存的课程"); return; }
        final int[] done = {0};
        for (CourseScheduleRow row : rows) {
            saveOne(row, () -> {
                done[0]++;
                if (done[0] >= total) {
                    Platform.runLater(() -> ShowMessage.showInfoMessage("完成", "已全部保存 " + total + " 门课程"));
                }
            });
        }
    }

    private void navigateBack() {
        try {
            Pane contentArea = findContentArea();
            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/information_management_system/admin/courseManagement.fxml"));
                Parent view = loader.load();
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
            }
        } catch (IOException ignored) {}
    }

    private Pane findContentArea() {
        if (backButton != null && backButton.getScene() != null)
            return (Pane) backButton.getScene().lookup("#contentArea");
        return null;
    }
}
