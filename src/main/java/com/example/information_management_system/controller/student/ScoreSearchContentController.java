package com.example.information_management_system.controller.student;

import com.example.information_management_system.entity.Data;
import com.example.information_management_system.model.ScoreRecord;
import com.example.information_management_system.util.ExportUtils;
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
        setupColumns();

        termSelector.setItems(Data.getInstance().getSemesterList());
        String currentTerm = Data.getInstance().getCurrentTerm();
        if (currentTerm != null && !currentTerm.isEmpty()) {
            termSelector.setValue(currentTerm);
        } else if (!termSelector.getItems().isEmpty()) {
            termSelector.setValue(termSelector.getItems().get(0));
        }

        termSelector.setOnAction(e -> loadScores());
        scoreTable.setItems(scoreRecords);

        exportBtn.setOnAction(e -> handleExport());

        if (termSelector.getValue() != null) {
            loadScores();
        }
    }

    private void setupColumns() {
        colIndex.setCellValueFactory(new PropertyValueFactory<>("index"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colPoint.setCellValueFactory(new PropertyValueFactory<>("point"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colTeacher.setCellValueFactory(new PropertyValueFactory<>("teacher"));
        colGrade.setCellValueFactory(new PropertyValueFactory<>("grade"));
        colGpa.setCellValueFactory(new PropertyValueFactory<>("gpa"));
        colRank.setCellValueFactory(new PropertyValueFactory<>("rank"));
        colRegular.setCellValueFactory(new PropertyValueFactory<>("regular"));
        colFinal.setCellValueFactory(new PropertyValueFactory<>("finalScore"));
    }

    private void loadScores() {
        String term = termSelector.getValue();
        if (term == null || term.isEmpty()) return;

        Map<String, String> params = new HashMap<>();
        params.put("term", term);

        NetworkUtils.get("/grade/getStudentGrades", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = res.getAsJsonArray("data");
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
                                if (obj.has("courseName")) record.setCourseName(obj.get("courseName").getAsString());
                                if (obj.has("point")) {
                                    double point = obj.get("point").getAsDouble();
                                    record.setPoint(point);
                                    totalCredit += point;
                                }
                                if (obj.has("type")) record.setType(obj.get("type").getAsString());
                                if (obj.has("teacher")) record.setTeacher(obj.get("teacher").getAsString());
                                if (obj.has("grade")) record.setGrade(obj.get("grade").getAsString());
                                if (obj.has("gpa") && !obj.get("gpa").isJsonNull()) {
                                    double gpa = obj.get("gpa").getAsDouble();
                                    record.setGpa(gpa);
                                    totalGpa += gpa;
                                    gpaCount++;
                                }
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
                    avgGpaLabel.setText("加载失败");
                    totalCreditLabel.setText("加载失败");
                });
            }
        });
    }

    private void handleExport() {
        if (scoreRecords.isEmpty()) {
            ShowMessage.showWarningMessage("导出失败", "没有成绩数据可导出");
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
                ShowMessage.showInfoMessage("导出成功", "成绩单已导出到: " + file.getAbsolutePath());
            } catch (Exception e) {
                ShowMessage.showErrorMessage("导出失败", e.getMessage());
            }
        });
    }
}
