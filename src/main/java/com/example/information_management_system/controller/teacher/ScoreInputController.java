package com.example.information_management_system.controller.teacher;

import com.example.information_management_system.model.CourseForScoreInput;
import com.example.information_management_system.model.ScoreEntry;
import com.example.information_management_system.util.ExportUtils;
import com.example.information_management_system.util.JsonUtil;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;
import com.example.information_management_system.util.StringUtil;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;

import java.io.File;
import java.util.*;

public class ScoreInputController {

    private final Gson gson = new Gson();

    @FXML private ComboBox<String> courseComboBox;
    @FXML private TextField searchField;
    @FXML private TableView<ScoreEntry> scoreTable;
    @FXML private TableColumn<ScoreEntry, String> sduidColumn;
    @FXML private TableColumn<ScoreEntry, String> nameColumn;
    @FXML private TableColumn<ScoreEntry, String> classNameColumn;
    @FXML private TableColumn<ScoreEntry, Double> regularScoreColumn;
    @FXML private TableColumn<ScoreEntry, Double> finalScoreColumn;
    @FXML private TableColumn<ScoreEntry, Double> totalScoreColumn;
    @FXML private TableColumn<ScoreEntry, String> remarksColumn;
    @FXML private Button saveButton;
    @FXML private Button publishButton;
    @FXML private Button exportButton;

    private ObservableList<ScoreEntry> scoreList = FXCollections.observableArrayList();
    private Map<String, Integer> courseNameToId = new HashMap<>();
    private Map<Integer, String> courseIdToTerm = new HashMap<>();
    private Map<Integer, double[]> courseIdToRatio = new HashMap<>();
    private String selectedCourseCode;

    @FXML
    public void initialize() {
        scoreTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTableColumns();
        scoreTable.setItems(scoreList);
        scoreTable.setEditable(true);

        if (courseComboBox != null) {
            courseComboBox.setOnAction(e -> onCourseSelected());
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, val) -> filterScores());
        }
        if (saveButton != null) {
            saveButton.setOnAction(e -> handleSaveScores());
        }
        if (publishButton != null) {
            publishButton.setOnAction(e -> handlePublishScores());
        }
        if (exportButton != null) {
            exportButton.setOnAction(e -> handleExportExcel());
        }

        fetchTeacherClasses();
    }

    private void setupTableColumns() {
        sduidColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSduid()));
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        classNameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getClassName()));
        regularScoreColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getRegularScore()).asObject());
        regularScoreColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        regularScoreColumn.setOnEditCommit(event -> {
            ScoreEntry entry = event.getRowValue();
            entry.setRegularScore(event.getNewValue());
            double[] ratio = courseIdToRatio.getOrDefault(entry.getCourseId(), new double[]{0.3, 0.7});
            entry.setTotalScore(event.getNewValue() * ratio[0] + entry.getFinalScore() * ratio[1]);
            scoreTable.refresh();
        });
        finalScoreColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getFinalScore()).asObject());
        finalScoreColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        finalScoreColumn.setOnEditCommit(event -> {
            ScoreEntry entry = event.getRowValue();
            entry.setFinalScore(event.getNewValue());
            double[] ratio = courseIdToRatio.getOrDefault(entry.getCourseId(), new double[]{0.3, 0.7});
            entry.setTotalScore(entry.getRegularScore() * ratio[0] + event.getNewValue() * ratio[1]);
            scoreTable.refresh();
        });
        totalScoreColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getTotalScore()).asObject());
        remarksColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRemarks()));
        remarksColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        remarksColumn.setOnEditCommit(event -> {
            ScoreEntry entry = event.getRowValue();
            entry.setRemarks(event.getNewValue());
        });
    }

    private void fetchTeacherClasses() {
        NetworkUtils.get("/class/list", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        ObservableList<String> courseNames = FXCollections.observableArrayList();
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject obj = arr.get(i).getAsJsonObject();
                            String status = JsonUtil.safeGetString(obj, "status");
                            if (!"APPROVED".equalsIgnoreCase(status)) continue;
                            String name = obj.has("name") ? obj.get("name").getAsString() : "";
                            int id = obj.has("id") ? obj.get("id").getAsInt() : 0;
                            courseNames.add(name);
                            courseNameToId.put(name, id);
                            if (obj.has("term")) courseIdToTerm.put(id, obj.get("term").getAsString());
                            double rr = obj.has("regularRatio") ? obj.get("regularRatio").getAsDouble() : 0.3;
                            double fr = obj.has("finalRatio") ? obj.get("finalRatio").getAsDouble() : 0.7;
                            courseIdToRatio.put(id, new double[]{rr, fr});
                        }
                        Platform.runLater(() -> {
                            if (courseComboBox != null) {
                                courseComboBox.setItems(courseNames);
                                if (!courseNames.isEmpty()) courseComboBox.setValue(courseNames.get(0));
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() ->
                        ShowMessage.showErrorMessage("错误", "数据加载失败"));
            }
        });
    }

    private void onCourseSelected() {
        String selected = courseComboBox.getValue();
        if (selected == null) return;
        selectedCourseCode = selected;
        fetchScoresForCourse(selected);
    }

    private void fetchScoresForCourse(String courseName) {
        Integer courseId = courseNameToId.get(courseName);
        if (courseId == null) {
            courseId = 0;
        }

        Map<String, String> params = new HashMap<>();
        params.put("courseName", courseName);
        if (courseId > 0) {
            params.put("courseId", String.valueOf(courseId));
        }

        final int cid = courseId;
        NetworkUtils.get("/class/" + cid + "/students", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        ObservableList<ScoreEntry> list = FXCollections.observableArrayList();
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject obj = arr.get(i).getAsJsonObject();
                            ScoreEntry entry = new ScoreEntry();
                            entry.setCourseId(cid);
                            if (obj.has("sduid")) entry.setSduid(obj.get("sduid").getAsString());
                            if (obj.has("username")) entry.setName(obj.get("username").getAsString());
                            if (obj.has("number")) entry.setClassName(obj.get("number").getAsString());
                            else if (obj.has("sectionNumber")) entry.setClassName(String.valueOf(obj.get("sectionNumber").getAsInt()));
                            if (obj.has("id")) entry.setStudentId(obj.get("id").getAsInt());
                            if (obj.has("regular")) entry.setRegularScore(obj.get("regular").getAsDouble());
                            if (obj.has("finalScore")) entry.setFinalScore(obj.get("finalScore").getAsDouble());
                            if (obj.has("grade")) entry.setTotalScore(obj.get("grade").getAsDouble());
                            entry.setStatus("");
                            entry.setRemarks("");
                            entry.setCourseName(courseName);
                            list.add(entry);
                        }
                        Platform.runLater(() -> scoreList.setAll(list));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() ->
                        ShowMessage.showErrorMessage("错误", "数据加载失败"));
            }
        });
    }

    private void filterScores() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            scoreTable.setItems(scoreList);
            return;
        }
        ObservableList<ScoreEntry> filtered = FXCollections.observableArrayList();
        for (ScoreEntry entry : scoreList) {
            if ((entry.getName() != null && entry.getName().toLowerCase().contains(query))
                    || (entry.getSduid() != null && entry.getSduid().toLowerCase().contains(query))
                    || (entry.getClassName() != null && entry.getClassName().toLowerCase().contains(query))) {
                filtered.add(entry);
            }
        }
        scoreTable.setItems(filtered);
    }

    private void handleSaveScores() {
        if (scoreList.isEmpty()) { ShowMessage.showWarningMessage("提示", "没有成绩数据可保存"); return; }

        final int[] done = {0}; final int[] total = {0}; final boolean[] hasErr = {false};
        for (ScoreEntry entry : scoreList) {
            if (entry.getRegularScore() == 0 && entry.getFinalScore() == 0 && entry.getTotalScore() == 0) continue;
            total[0]++;
            Integer cid = courseNameToId.get(entry.getCourseName());
            if (cid == null) cid = courseNameToId.get(selectedCourseCode);
            Map<String, String> params = new HashMap<>();
            params.put("studentId", String.valueOf(entry.getStudentId()));
            if (cid != null) {
                params.put("courseId", String.valueOf(cid));
                String term = courseIdToTerm.get(cid);
                if (term != null) params.put("term", term);
            }
            params.put("regular", String.valueOf((int) entry.getRegularScore()));
            params.put("finalScore", String.valueOf((int) entry.getFinalScore()));
            params.put("grade", String.valueOf((int) entry.getTotalScore()));
            params.put("rank", "0");
            NetworkUtils.postWithQueryParams("/grade/setGrade", params, new NetworkUtils.Callback<String>() {
                @Override public void onSuccess(String r) { try { JsonObject j = gson.fromJson(r, JsonObject.class); if (j.has("code") && j.get("code").getAsInt()!=200) hasErr[0]=true; } catch(Exception ignored){} done[0]++; checkDone(); }
                @Override public void onFailure(Exception e) { hasErr[0] = true; done[0]++; checkDone(); }
                private void checkDone() { if (done[0] >= total[0]) Platform.runLater(() -> {
                    if (total[0] == 0) ShowMessage.showWarningMessage("提示", "没有需要保存的成绩");
                    else ShowMessage.showInfoMessage("成功", hasErr[0] ? "部分保存失败" : "已保存 "+total[0]+" 条");
                    fetchScoresForCourse(selectedCourseCode);
                });}
            });
        }
    }

    private void handlePublishScores() {
        if (selectedCourseCode == null || selectedCourseCode.isEmpty()) {
            ShowMessage.showWarningMessage("提示", "请先选择课程"); return;
        }
        boolean confirmed = ShowMessage.showConfirmMessage("确认", "确定要发布该课程的成绩吗？");
        if (!confirmed) return;

        Integer courseId = courseNameToId.get(selectedCourseCode);
        Map<String, String> params = new HashMap<>();
        if (courseId != null) params.put("courseId", String.valueOf(courseId));
        NetworkUtils.post("/grade/releaseGrade", params, "", new NetworkUtils.Callback<String>() {
            @Override public void onSuccess(String result) {
                Platform.runLater(() -> ShowMessage.showInfoMessage("成功", "已成功发布"));
            }
            @Override public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "网络请求失败，请检查连接"));
            }
        });
    }

    private void handleExportExcel() {
        if (scoreList.isEmpty()) {
            ShowMessage.showWarningMessage("提示", "没有数据可导出");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出成绩Excel");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel文件", "*.xlsx"));
        fileChooser.setInitialFileName("成绩表_" + (selectedCourseCode != null ? selectedCourseCode : "export") + ".xlsx");
        File file = fileChooser.showSaveDialog(scoreTable.getScene().getWindow());

        if (file != null) {
            List<String> headers = Arrays.asList("学号", "姓名", "班级", "平时成绩", "期末成绩", "总成绩", "状态", "备注");
            List<List<String>> data = new ArrayList<>();
            for (ScoreEntry entry : scoreList) {
                List<String> row = Arrays.asList(
                        entry.getSduid(),
                        entry.getName(),
                        entry.getClassName(),
                        String.valueOf(entry.getRegularScore()),
                        String.valueOf(entry.getFinalScore()),
                        String.valueOf(entry.getTotalScore()),
                        entry.getStatus() != null ? entry.getStatus() : "",
                        entry.getRemarks() != null ? entry.getRemarks() : ""
                );
                data.add(row);
            }
            ExportUtils.exportToExcel(file.getAbsolutePath(), headers, data, "成绩表");
            ShowMessage.showInfoMessage("成功", "已成功导出至: " + file.getName());
        }
    }
}
