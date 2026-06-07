package com.example.information_management_system.controller.teacher;

import com.example.information_management_system.entity.Data;
import com.example.information_management_system.model.CourseRow;
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
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;

import java.util.*;

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
            "第1-2节\n8:00-9:50", "第3-4节\n10:10-12:00", "第5-6节\n14:00-15:50",
            "第7-8节\n16:10-18:00", "第9-10节\n19:00-20:50"
    };
    private static final String[] DAYS = {"monday","tuesday","wednesday","thursday","friday","saturday","sunday"};

    private static final Color[] PASTEL_COLORS = {
        Color.web("#fef3c7"), Color.web("#dbeafe"), Color.web("#d1fae5"),
        Color.web("#fce7f3"), Color.web("#e0e7ff"), Color.web("#ccfbf1"),
        Color.web("#fef9c3"), Color.web("#dcfce7"), Color.web("#ede9fe"),
        Color.web("#ffedd5"), Color.web("#e0f2fe"), Color.web("#fae8ff"),
        Color.web("#ecfccb"), Color.web("#fee2e2"), Color.web("#cffafe"),
    };

    private int currentWeek = 1;
    private ObservableList<CourseRow> scheduleData = FXCollections.observableArrayList();
    private static final Color DARK_CELL_BG = Color.web("#1e293b");

    private Background defaultBg() {
        return new Background(new BackgroundFill(
            com.example.information_management_system.util.ThemeManager.isDark() ? DARK_CELL_BG : Color.WHITE,
            null, null));
    }

    @FXML
    public void initialize() {
        scheduleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        final double[] ratios = {1.0, 1.3, 1.3, 1.3, 1.3, 1.3, 1.3, 1.3};
        final double totalRatio = Arrays.stream(ratios).sum();
        scheduleTable.widthProperty().addListener((obs, oldW, newW) -> {
            double w = newW.doubleValue() - 2;
            for (int i = 0; i < ratios.length && i < scheduleTable.getColumns().size(); i++)
                scheduleTable.getColumns().get(i).setPrefWidth(w * ratios[i] / totalRatio);
        });
        scheduleTable.fixedCellSizeProperty().bind(
            scheduleTable.heightProperty().subtract(28).divide(5).map(v -> Math.max(40, v.doubleValue()))
        );
        setupColumns();
        scheduleTable.setItems(scheduleData);

        if (!Data.getInstance().getSemesterList().isEmpty()) {
            termSelector.setItems(Data.getInstance().getSemesterList());
            String ct = Data.getInstance().getCurrentTerm();
            termSelector.setValue(ct != null && !ct.isEmpty() ? ct : termSelector.getItems().get(0));
        }
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
                                loadSchedule();
                            }
                        });
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Exception ignored) {}
        });

        termSelector.setOnAction(e -> loadSchedule());
        btnPrevWeek.setOnAction(e -> { if (currentWeek > 1) { currentWeek--; updateWeekLabel(); loadSchedule(); } });
        btnNextWeek.setOnAction(e -> { if (currentWeek < 20) { currentWeek++; updateWeekLabel(); loadSchedule(); } });

        updateWeekLabel();
        loadSchedule();
    }

    @SuppressWarnings("unchecked")
    private void setupColumns() {
        timeColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTime()));
        for (int d = 0; d < 7; d++) {
            setupDayColumn((TableColumn<CourseRow, String>) scheduleTable.getColumns().get(d + 1), d);
        }
    }

    private void setupDayColumn(TableColumn<CourseRow, String> col, int dayIdx) {
        col.setCellValueFactory(cell -> new SimpleStringProperty(
            switch(dayIdx) {
                case 0 -> cell.getValue().getMonday(); case 1 -> cell.getValue().getTuesday();
                case 2 -> cell.getValue().getWednesday(); case 3 -> cell.getValue().getThursday();
                case 4 -> cell.getValue().getFriday(); case 5 -> cell.getValue().getSaturday();
                default -> cell.getValue().getSunday();
            }));
        col.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText(null); setStyle(""); setBackground(defaultBg()); return;
                }
                String[] parts = item.split("\\|\\|");
                setText(parts.length > 0 ? parts[0] : "");
                if (parts.length > 1) setBackground(new Background(new BackgroundFill(Color.web(parts[1]), null, null)));
                else setBackground(defaultBg());
                setStyle("");
            }
        });
    }

    private void updateWeekLabel() { weekLabel.setText("第 " + currentWeek + " 周"); }

    private void loadSchedule() {
        String term = termSelector.getValue();
        if (term == null || term.isEmpty()) return;
        Map<String, String> params = new HashMap<>();
        params.put("term", term);
        NetworkUtils.get("/class/getClassSchedule/" + currentWeek, params, new NetworkUtils.Callback<String>() {
            @Override public void onSuccess(String result) {
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
                    Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "数据解析失败"));
                }
            }
            @Override public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "数据加载失败"));
            }
        });
    }

    private ObservableList<CourseRow> buildScheduleRows(JsonArray courses) {
        Map<String, String> colorMap = new HashMap<>();
        int colorIdx = 0;
        String[][] gridText = new String[TIME_SLOTS.length][7];
        String[][] gridColor = new String[TIME_SLOTS.length][7];
        for (int i = 0; i < TIME_SLOTS.length; i++) for (int j = 0; j < 7; j++) { gridText[i][j] = ""; gridColor[i][j] = ""; }

        for (int i = 0; i < courses.size(); i++) {
            JsonObject c = courses.get(i).getAsJsonObject();
            String name = getStr(c, "name", "courseName");
            String classroom = getStr(c, "classroom", "location");
            String time = getStr(c, "time");
            if (time == null || time.isEmpty() || name == null) continue;

            String hex = colorMap.get(name);
            if (hex == null) { hex = toHex(PASTEL_COLORS[colorIdx % PASTEL_COLORS.length]); colorIdx++; colorMap.put(name, hex); }

            // 解析全局 slot 编号 (0-24)，转换为 (day, 节次) 对
            boolean placed = false;
            for (String part : time.split(",")) {
                try {
                    int globalSlot = Integer.parseInt(part.trim());
                    int dayIdx = globalSlot / 5;     // 0=周一, 1=周二, ...
                    int slot = globalSlot % 5;        // 节次 0-4
                    if (dayIdx < 0 || dayIdx > 6 || slot < 0 || slot > 4) continue;
                    gridText[slot][dayIdx] = name + " @" + classroom;
                    gridColor[slot][dayIdx] = hex;
                    placed = true;
                } catch (NumberFormatException ignored) {}
            }
            if (!placed) {
                int slot = -1, dayIdx = -1;
                if (time.matches("\\d{2,3}")) { int t = Integer.parseInt(time); if (t > 100) { dayIdx = (t/100)-1; slot = parseTimeSlot(String.valueOf(t%100)); } else slot = parseTimeSlot(time); }
                else slot = parseTimeSlot(time);
                if (slot >= 0) {
                    boolean hasDay = false;
                    for (int d = 0; d < 7; d++) {
                        if (getStr(c, DAYS[d]) != null) { gridText[slot][d] = name+" @"+classroom; gridColor[slot][d] = hex; hasDay = true; }
                    }
                    String dayStr = getStr(c, "day");
                    if (dayStr != null && !dayStr.isEmpty() && dayIdx < 0) dayIdx = parseDay(dayStr);
                    if (dayIdx >= 0 && dayIdx < 7) { gridText[slot][dayIdx] = name+" @"+classroom; gridColor[slot][dayIdx] = hex; hasDay = true; }
                    if (!hasDay) { gridText[slot][0] = name+" @"+classroom; gridColor[slot][0] = hex; }
                }
            }
        }

        ObservableList<CourseRow> rows = FXCollections.observableArrayList();
        for (int i = 0; i < TIME_SLOTS.length; i++) {
            CourseRow row = new CourseRow(TIME_SLOTS[i]);
            String[] cells = new String[7];
            for (int d = 0; d < 7; d++) cells[d] = gridText[i][d].isEmpty() ? null : (gridText[i][d]+"||"+gridColor[i][d]);
            row.setMonday(cells[0]); row.setTuesday(cells[1]); row.setWednesday(cells[2]);
            row.setThursday(cells[3]); row.setFriday(cells[4]); row.setSaturday(cells[5]); row.setSunday(cells[6]);
            rows.add(row);
        }
        return rows;
    }

    private String toHex(Color c) { return String.format("#%02x%02x%02x", (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255)); }

    private int parseTimeSlot(String time) { if (time == null) return -1; for (String part : time.split(",")) { try { int n = Integer.parseInt(part.trim()); if (n>=1&&n<=2) return 0; if (n>=3&&n<=4) return 1; if (n>=5&&n<=6) return 2; if (n>=7&&n<=8) return 3; if (n>=9&&n<=10) return 4; } catch (NumberFormatException ignored) {} } return -1; }

    private int parseDay(String day) { if (day == null) return -1; return switch(day.trim()) { case "1","一","周一","星期一"->0;case "2","二","周二","星期二"->1;case "3","三","周三","星期三"->2;case "4","四","周四","星期四"->3;case "5","五","周五","星期五"->4;case "6","六","周六","星期六"->5;case "7","日","周日","星期日","天"->6;default->-1;}; }

    private String getStr(JsonObject obj, String... keys) { for (String k:keys) if (obj.has(k)&&!obj.get(k).isJsonNull()) return obj.get(k).getAsString(); return null; }
}
