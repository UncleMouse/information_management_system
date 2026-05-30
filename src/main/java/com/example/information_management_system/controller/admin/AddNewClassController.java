package com.example.information_management_system.controller.admin;

import com.example.information_management_system.model.Section;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.*;

public class AddNewClassController {

    private final Gson gson = new Gson();
    private Section editingSection;

    @FXML private Label dialogTitle;
    @FXML private TextField classNameField;
    @FXML private ComboBox<String> gradeCombo;
    @FXML private ComboBox<String> majorCombo;
    @FXML private Button btnSubmit;
    @FXML private Button btnCancel;

    @FXML
    public void initialize() {
        majorCombo.getItems().addAll("软件工程(0)", "数字媒体技术(1)", "大数据(2)", "AI(3)");
        majorCombo.getSelectionModel().selectFirst();
        int y = java.time.Year.now().getValue();
        for (int i = y - 3; i <= y + 1; i++) gradeCombo.getItems().add(String.valueOf(i));
        gradeCombo.getSelectionModel().selectLast();
        btnSubmit.setOnAction(e -> handleSubmit());
        btnCancel.setOnAction(e -> closeDialog());
    }

    public void setEditMode(Section section) {
        this.editingSection = section;
        if (dialogTitle != null) dialogTitle.setText("编辑班级");
        String clsName = section.getClassName();
        if (clsName != null && clsName.endsWith("班")) clsName = clsName.substring(0, clsName.length() - 1);
        if (classNameField != null) classNameField.setText(clsName != null ? clsName : "");
        if (gradeCombo != null && section.getGrade() != null) {
            String g = section.getGrade();
            gradeCombo.setValue(g.length() >= 4 ? g.substring(0, 4) : g);
        }
        if (majorCombo != null && section.getMajor() != null) {
            for (String item : majorCombo.getItems()) {
                if (item.contains(section.getMajor()) || section.getMajor().contains(item.substring(0, item.indexOf("(")))) {
                    majorCombo.setValue(item); break;
                }
            }
        }
    }

    private void handleSubmit() {
        String className = classNameField.getText().trim();
        if (className.endsWith("班")) className = className.substring(0, className.length() - 1);
        if (className.isEmpty()) { ShowMessage.showWarningMessage("提示", "班级名称不能为空"); return; }

        String major = majorCombo.getValue();
        int paren = major.indexOf("(");
        String majorCode = paren > 0 ? major.substring(paren + 1, major.indexOf(")")) : "0";

        Map<String, String> params = new HashMap<>();
        params.put("number", className);
        params.put("grade", gradeCombo.getValue() != null ? gradeCombo.getValue() : "");
        params.put("major", majorCode);
        if (editingSection != null) params.put("id", String.valueOf(editingSection.getId()));

        String endpoint = editingSection != null ? "/section/updateSection" : "/section/addSection";
        btnSubmit.setDisable(true);

        NetworkUtils.postWithQueryParams(endpoint, params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    Platform.runLater(() -> {
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            ShowMessage.showInfoMessage("成功", editingSection != null ? "已更新" : "已添加");
                            closeDialog();
                        } else {
                            ShowMessage.showErrorMessage("错误", res.has("msg")?res.get("msg").getAsString():"操作失败");
                            btnSubmit.setDisable(false);
                        }
                    });
                } catch (Exception e) { Platform.runLater(() -> { ShowMessage.showErrorMessage("错误", "解析失败"); btnSubmit.setDisable(false); }); }
            }
            @Override
            public void onFailure(Exception e) { Platform.runLater(() -> { ShowMessage.showErrorMessage("错误", e.getMessage()); btnSubmit.setDisable(false); }); }
        });
    }

    private void closeDialog() { ((Stage) btnSubmit.getScene().getWindow()).close(); }
}
