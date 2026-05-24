package com.example.information_management_system.controller.admin;

import com.example.information_management_system.model.Student;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;
import com.example.information_management_system.util.StringUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class AddNewStudentController {

    private final Gson gson = new Gson();
    private Student editingStudent;
    private Runnable onStudentAddedListener;

    @FXML private Label dialogTitle;
    @FXML private TextField sduidField;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> genderCombo;
    @FXML private ComboBox<String> majorCombo;
    @FXML private TextField gradeField;
    @FXML private ComboBox<String> classCombo;
    @FXML private Button btnSubmit;
    @FXML private Button btnCancel;

    @FXML
    public void initialize() {
        genderCombo.getItems().addAll("男", "女");
        genderCombo.getSelectionModel().selectFirst();

        majorCombo.getItems().addAll(
                "计算机科学与技术", "软件工程", "信息安全", "数据科学与大数据技术",
                "人工智能", "电子信息工程", "通信工程", "自动化",
                "数学与应用数学", "物理学", "化学", "生物科学",
                "经济学", "金融学", "会计学", "工商管理"
        );

        btnSubmit.setOnAction(e -> handleSubmit());
        btnCancel.setOnAction(e -> closeDialog());
    }

    public void setEditMode(Student student) {
        this.editingStudent = student;
        if (dialogTitle != null) dialogTitle.setText("编辑学生");
        if (sduidField != null) sduidField.setText(student.getSduid());
        if (nameField != null) nameField.setText(student.getName());
        if (genderCombo != null) genderCombo.setValue(student.getGender());
        if (majorCombo != null) majorCombo.setValue(student.getMajor());
        if (gradeField != null) gradeField.setText(student.getGrade());
        if (classCombo != null) classCombo.setValue(student.getClassName());
    }

    public void setOnStudentAddedListener(Runnable listener) {
        this.onStudentAddedListener = listener;
    }

    private void handleSubmit() {
        String sduid = sduidField.getText().trim();
        String name = nameField.getText().trim();
        String gender = genderCombo.getValue();
        String major = majorCombo.getValue();
        String grade = gradeField.getText().trim();
        String className = classCombo.getValue();

        if (StringUtil.isEmpty(sduid) || StringUtil.isEmpty(name)) {
            ShowMessage.showWarningMessage("提示", "学号和姓名不能为空");
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("sduid", sduid);
        body.put("name", name);
        body.put("gender", gender);
        body.put("major", major);
        body.put("grade", grade);
        if (className != null && !className.isEmpty()) {
            body.put("className", className);
        }

        if (editingStudent != null) {
            body.put("id", editingStudent.getId());
        }

        String json = gson.toJson(body);
        String endpoint = editingStudent != null ? "/admin/updateUser" : "/admin/addUser";

        btnSubmit.setDisable(true);
        NetworkUtils.post(endpoint, json, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        Platform.runLater(() -> {
                            ShowMessage.showInfoMessage("成功",
                                    editingStudent != null ? "学生信息已更新" : "学生已添加");
                            if (onStudentAddedListener != null) {
                                onStudentAddedListener.run();
                            }
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
                        ShowMessage.showErrorMessage("错误", "解析响应失败");
                        btnSubmit.setDisable(false);
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    ShowMessage.showErrorMessage("错误", "请求失败: " + e.getMessage());
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
