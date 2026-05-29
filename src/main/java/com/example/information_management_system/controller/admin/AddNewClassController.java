package com.example.information_management_system.controller.admin;

import com.example.information_management_system.model.Section;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;
import com.example.information_management_system.util.StringUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class AddNewClassController {

    private final Gson gson = new Gson();
    private Section editingSection;

    @FXML private Label dialogTitle;
    @FXML private TextField classNameField;
    @FXML private TextField gradeField;
    @FXML private TextField majorField;
    @FXML private TextField counselorField;
    @FXML private Button btnSubmit;
    @FXML private Button btnCancel;

    @FXML
    public void initialize() {
        btnSubmit.setOnAction(e -> handleSubmit());
        btnCancel.setOnAction(e -> closeDialog());
    }

    public void setEditMode(Section section) {
        this.editingSection = section;
        if (dialogTitle != null) dialogTitle.setText("编辑班级");
        if (classNameField != null) classNameField.setText(section.getClassName());
        if (gradeField != null) gradeField.setText(section.getGrade());
        if (majorField != null) majorField.setText(section.getMajor());
    }

    private void handleSubmit() {
        String className = classNameField.getText().trim();
        String grade = gradeField.getText().trim();
        String major = majorField.getText().trim();
        String counselor = counselorField.getText().trim();

        if (StringUtil.isEmpty(className)) {
            ShowMessage.showWarningMessage("提示", "班级名称不能为空");
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("className", className);
        body.put("grade", grade);
        body.put("major", major);
        body.put("counselor", counselor);

        if (editingSection != null) {
            body.put("id", editingSection.getId());
        }

        String json = gson.toJson(body);
        String endpoint = editingSection != null ? "/section/updateSection" : "/section/addSection";

        btnSubmit.setDisable(true);
        NetworkUtils.post(endpoint, json, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        Platform.runLater(() -> {
                            ShowMessage.showInfoMessage("成功",
                                    editingSection != null ? "已成功更新" : "已成功添加");
                            closeDialog();
                        });
                    } else {
                        Platform.runLater(() -> {
                            ShowMessage.showErrorMessage("错误",
                                    res.has("msg") ? res.get("msg").getAsString() : "操作失败");
                            btnSubmit.setDisable(false);
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        ShowMessage.showErrorMessage("错误", "数据解析失败，请稍后重试");
                        btnSubmit.setDisable(false);
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    ShowMessage.showErrorMessage("错误", "网络请求失败，请检查连接");
                    btnSubmit.setDisable(false);
                });
            }
        });
    }

    private void closeDialog() {
        Stage stage = (Stage) btnSubmit.getScene().getWindow();
        stage.close();
    }
}
