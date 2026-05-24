package com.example.information_management_system.controller.teacher;

import com.example.information_management_system.model.CourseRow;
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

public class CourseScheduleManagementContent {

    private final Gson gson = new Gson();

    @FXML private ComboBox<String> termSelector;
    @FXML private TableView<CourseRow> scheduleTable;
    @FXML private TableColumn<CourseRow, String> timeColumn;
    @FXML private TableColumn<CourseRow, String> mondayColumn;
    @FXML private TableColumn<CourseRow, String> tuesdayColumn;
    @FXML private TableColumn<CourseRow, String> wednesdayColumn;
    @FXML private TableColumn<CourseRow, String> thursdayColumn;
    @FXML private TableColumn<CourseRow, String> fridayColumn;
    @FXML private TableColumn<CourseRow, String> saturdayColumn;
    @FXML private TableColumn<CourseRow, String> sundayColumn;
    @FXML private Label weekLabel;
    @FXML private Button btnPrevWeek;
    @FXML private Button btnNextWeek;

    private static final String[] TIME_SLOTS = {
            "第1-2节\n8:00-9:50",
            "第3-4节\n10:10-12:00",
            "第5-6节\n14:00-15:50",
            "第7-8节\n16:10-18:00",
            "第9-10节\n19:00-20:50"
    };

    private static final String[] DAYS = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};

    private int currentWeek = 1;
    private ObservableList<CourseRow> scheduleData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupColumns();
        scheduleTable.setItems(scheduleData);

        termSelector.setItems(com.example.information_management_system.entity.Data.getInstance().getSemesterList());
        String ct = com.example.information_management_system.entity.Data.getInstance().getCurrentTerm();
        if (ct != null && !ct.isEmpty()) {
            termSelector.setValue(ct);
        } else if (!termSelector.getItems().isEmpty()) {
            termSelector.setValue(termSelector.getItems().get(0));
        }

        termSelector.setOnAction(e -> loadSchedule());
        btnPrevWeek.setOnAction(e -> { if (currentWeek > 1) { currentWeek--; updateWeekLabel(); loadSchedule(); } });
        btnNextWeek.setOnAction(e -> { if (currentWeek < 20) { currentWeek++; updateWeekLabel(); loadSchedule(); } });

        updateWeekLabel();
        loadSchedule();
    }

    private void setupColumns() {
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        mondayColumn.setCellValueFactory(new PropertyValueFactory<>("monday"));
        tuesdayColumn.setCellValueFactory(new PropertyValueFactory<>("tuesday"));
        wednesdayColumn.setCellValueFactory(new PropertyValueFactory<>("wednesday"));
        thursdayColumn.setCellValueFactory(new PropertyValueFactory<>("thursday"));
        fridayColumn.setCellValueFactory(new PropertyValueFactory<>("friday"));
        saturdayColumn.setCellValueFactory(new PropertyValueFactory<>("saturday"));
        sundayColumn.setCellValueFactory(new PropertyValueFactory<>("sunday"));
    }

    private void updateWeekLabel() {
        weekLabel.setText("第 " + currentWeek + " 周");
    }

    private void loadSchedule() {
        String term = termSelector.getValue();
        if (term == null || term.isEmpty()) return;

        Map<String, String> params = new HashMap<>();
        params.put("term", term);
        params.put("week", String.valueOf(currentWeek));

        NetworkUtils.get("/class/getTeacherSchedule", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = res.getAsJsonArray("data");
                        Platform.runLater(() -> {
                            ObservableList<CourseRow> rows = buildScheduleRows(arr);
                            scheduleData.setAll(rows);
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("加载失败", "课表数据解析失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("加载失败", "获取课表失败"));
            }
        });
    }

    private ObservableList<CourseRow> buildScheduleRows(JsonArray courses) {
        String[][] grid = new String[TIME_SLOTS.length][7];
        for (int i = 0; i < TIME_SLOTS.length; i++) {
            for (int j = 0; j < 7; j++) grid[i][j] = "";
        }

        for (int i = 0; i < courses.size(); i++) {
            JsonObject c = courses.get(i).getAsJsonObject();
            String name = getStr(c, "name", "courseName");
            String classroom = getStr(c, "classroom", "location");
            String time = getStr(c, "time");

            if (time == null || time.isEmpty()) continue;
            int slot = parseTimeSlot(time);
            if (slot < 0) continue;

            for (int d = 0; d < 7; d++) {
                String dv = getStr(c, DAYS[d]);
                if (dv != null && !dv.isEmpty() && !"null".equals(dv)) {
                    grid[slot][d] = buildCell(name, classroom);
                }
            }

            String dayStr = getStr(c, "day");
            if (dayStr != null && !dayStr.isEmpty()) {
                int dayIdx = parseDay(dayStr);
                if (dayIdx >= 0 && dayIdx < 7) {
                    grid[slot][dayIdx] = buildCell(name, classroom);
                }
            }
        }

        ObservableList<CourseRow> rows = FXCollections.observableArrayList();
        for (int i = 0; i < TIME_SLOTS.length; i++) {
            CourseRow row = new CourseRow(TIME_SLOTS[i]);
            row.setMonday(blank(grid[i][0])); row.setTuesday(blank(grid[i][1]));
            row.setWednesday(blank(grid[i][2])); row.setThursday(blank(grid[i][3]));
            row.setFriday(blank(grid[i][4])); row.setSaturday(blank(grid[i][5]));
            row.setSunday(blank(grid[i][6]));
            rows.add(row);
        }
        return rows;
    }

    private String buildCell(String name, String classroom) {
        if (name == null) return "";
        return classroom != null && !classroom.isEmpty() ? name + "\n" + classroom : name;
    }

    private String blank(String s) { return (s == null || s.isEmpty()) ? null : s; }

    private int parseTimeSlot(String time) {
        if (time == null) return -1;
        try {
            int num = Integer.parseInt(time.replaceAll("[^0-9]", ""));
            if (num >= 1 && num <= 2) return 0;
            if (num >= 3 && num <= 4) return 1;
            if (num >= 5 && num <= 6) return 2;
            if (num >= 7 && num <= 8) return 3;
            if (num >= 9 && num <= 10) return 4;
        } catch (NumberFormatException ignored) {}
        if (time.contains("1") && (time.contains("2"))) return 0;
        if (time.contains("3") && (time.contains("4"))) return 1;
        if (time.contains("5") && (time.contains("6"))) return 2;
        if (time.contains("7") && (time.contains("8"))) return 3;
        if (time.contains("9") && (time.contains("10"))) return 4;
        return -1;
    }

    private int parseDay(String day) {
        if (day == null) return -1;
        return switch (day.trim()) {
            case "1", "一", "周一", "星期一" -> 0;
            case "2", "二", "周二", "星期二" -> 1;
            case "3", "三", "周三", "星期三" -> 2;
            case "4", "四", "周四", "星期四" -> 3;
            case "5", "五", "周五", "星期五" -> 4;
            case "6", "六", "周六", "星期六" -> 5;
            case "7", "日", "周日", "星期日", "天" -> 6;
            default -> -1;
        };
    }

    private String getStr(JsonObject obj, String... keys) {
        for (String k : keys) {
            if (obj.has(k) && !obj.get(k).isJsonNull()) return obj.get(k).getAsString();
        }
        return null;
    }
}
