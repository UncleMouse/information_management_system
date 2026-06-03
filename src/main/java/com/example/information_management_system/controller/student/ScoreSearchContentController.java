package com.example.information_management_system.controller.student;

import com.example.information_management_system.entity.Data;
import com.example.information_management_system.model.ScoreRecord;
import com.example.information_management_system.util.ExportUtils;
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
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreSearchContentController {

    private final Gson gson = new Gson();

    @FXML private ComboBox<String> termSelector;
    @FXML private TableView<ScoreRecord> scoreTable;
    @FXML private TableColumn<ScoreRecord, Integer> colIndex;
    @FXML private TableColumn<ScoreRecord, String> colCourseName;
    @FXML private TableColumn<ScoreRecord, Double> colPoint;
    @FXML private TableColumn<ScoreRecord, String> colType;
    @FXML private TableColumn<ScoreRecord, String> colTeacher;
    @FXML private TableColumn<ScoreRecord, String> colGrade;
    @FXML private TableColumn<ScoreRecord, Double> colGpa;
    @FXML private TableColumn<ScoreRecord, Integer> colRank;
    @FXML private TableColumn<ScoreRecord, Double> colRegular;
    @FXML private TableColumn<ScoreRecord, Double> colFinal;
    @FXML private Label avgGpaLabel;
    @FXML private Label totalCreditLabel;
    @FXML private Button exportBtn;

    private final ObservableList<ScoreRecord> scoreRecords = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        scoreTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupColumns();

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
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        javafx.collections.ObservableList<String> list = FXCollections.observableArrayList();
                        for (int i = 0; i < arr.size(); i++)
                            list.add(arr.get(i).getAsJsonObject().get("term").getAsString());
                        Platform.runLater(() -> {
                            termSelector.setItems(list);
                            Data.getInstance().getSemesterList().setAll(list);
                            if (termSelector.getValue() == null && !list.isEmpty()) {
                                termSelector.setValue(list.get(0));
                                loadScores();
                            }
                        });
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Exception ignored) {}
        });

        termSelector.setOnAction(e -> loadScores());
        scoreTable.setItems(scoreRecords);

        exportBtn.setOnAction(e -> handleExport());

        if (termSelector.getValue() != null) {
            loadScores();
        }
    }

    private void setupColumns() {
        colIndex.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getIndex()).asObject());
        colCourseName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCourseName()));
        colPoint.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getPoint()).asObject());
        colType.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getType()));
        colTeacher.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTeacher()));
        colGrade.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getGrade()));
        colGpa.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getGpa()).asObject());
        colRank.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getRank()).asObject());
        colRegular.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getRegular()).asObject());
        colFinal.setCellValueFactory(cell -> new javafx.beans.property.SimpleDoubleProperty(cell.getValue().getFinalScore()).asObject());
    }

    private void loadScores() {
        String term = termSelector.getValue();
        if (term == null || term.isEmpty()) return;

        Map<String, String> params = new HashMap<>();
        params.put("term", term);

        NetworkUtils.get("/grade/getGrade", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        Platform.runLater(() -> {
                            scoreRecords.clear();
                            double totalGpa = 0;
                            int gpaCount = 0;
                            double totalCredit = 0;

                            for (int i = 0; i < arr.size(); i++) {
                                JsonObject obj = arr.get(i).getAsJsonObject();
                                ScoreRecord record = new ScoreRecord();
                                record.setIndex(i + 1);
                                if (obj.has("id")) record.setId(obj.get("id").getAsInt());
                                // API: className=课程名, point=学分, type=类型, teacher=教师, grade=成绩(整数), rank=排名, regular=平时, finalScore=期末
                                if (obj.has("className")) record.setCourseName(obj.get("className").getAsString());
                                if (obj.has("point")) {
                                    double point = obj.get("point").getAsDouble();
                                    record.setPoint(point);
                                    totalCredit += point;
                                }
                                if (obj.has("type")) record.setType(obj.get("type").getAsString());
                                if (obj.has("teacher")) record.setTeacher(obj.get("teacher").getAsString());
                                if (obj.has("grade")) record.setGrade(String.valueOf(obj.get("grade").getAsInt()));
                                double gpa = obj.has("grade") ? Math.max(0, (obj.get("grade").getAsInt() - 50) / 10.0) : 0;
                                record.setGpa(gpa);
                                totalGpa += gpa;
                                gpaCount++;
                                if (obj.has("rank")) record.setRank(obj.get("rank").getAsInt());
                                if (obj.has("regular")) record.setRegular(obj.get("regular").getAsDouble());
                                if (obj.has("finalScore")) record.setFinalScore(obj.get("finalScore").getAsDouble());
                                scoreRecords.add(record);
                            }

                            if (gpaCount > 0) {
                                avgGpaLabel.setText(String.format("%.2f", totalGpa / gpaCount));
                            } else {
                                avgGpaLabel.setText("-");
                            }
                            totalCreditLabel.setText(String.format("%.1f", totalCredit));
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        avgGpaLabel.setText("错误");
                        totalCreditLabel.setText("错误");
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    avgGpaLabel.setText("数据加载失败");
                    totalCreditLabel.setText("数据加载失败");
                });
            }
        });
    }

    private void handleExport() {
        if (scoreRecords.isEmpty()) {
            ShowMessage.showWarningMessage("提示", "暂无数据");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出成绩单");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel 文件", "*.xlsx"));
        fileChooser.setInitialFileName("成绩单_" + (termSelector.getValue() != null ? termSelector.getValue() : "export") + ".xlsx");
        File file = fileChooser.showSaveDialog(scoreTable.getScene().getWindow());
        if (file == null) return;

        List<String> headers = List.of("序号", "课程名称", "学分", "类型", "教师", "成绩", "GPA", "排名", "平时成绩", "期末成绩");
        List<List<String>> data = new ArrayList<>();
        for (ScoreRecord record : scoreRecords) {
            List<String> row = new ArrayList<>();
            row.add(String.valueOf(record.getIndex()));
            row.add(record.getCourseName() != null ? record.getCourseName() : "");
            row.add(String.valueOf(record.getPoint()));
            row.add(record.getType() != null ? record.getType() : "");
            row.add(record.getTeacher() != null ? record.getTeacher() : "");
            row.add(record.getGrade() != null ? record.getGrade() : "");
            row.add(String.valueOf(record.getGpa()));
            row.add(String.valueOf(record.getRank()));
            row.add(String.valueOf(record.getRegular()));
            row.add(String.valueOf(record.getFinalScore()));
            data.add(row);
        }

        Platform.runLater(() -> {
            try {
                ExportUtils.exportToExcel(file.getAbsolutePath(), headers, data, "成绩单");
                ShowMessage.showInfoMessage("成功", "已成功导出至: " + file.getName());
            } catch (Exception e) {
                ShowMessage.showErrorMessage("错误", "网络请求失败，请检查连接");
            }
        });
    }
}
