package com.example.information_management_system.controller.admin;

import com.example.information_management_system.model.Student;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class AddNewStudentController {

    private static final Map<String, String> MAJOR_ENUM_MAP = new HashMap<>();
    static {
        MAJOR_ENUM_MAP.put("软件工程", "MAJOR_0");
        MAJOR_ENUM_MAP.put("数字媒体技术", "MAJOR_1");
        MAJOR_ENUM_MAP.put("数据科学与大数据技术", "MAJOR_2");
        MAJOR_ENUM_MAP.put("人工智能", "MAJOR_3");
    }

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

        if (sduid == null || sduid.isEmpty() || name == null || name.isEmpty()) {
            ShowMessage.showWarningMessage("提示", "学号和姓名不能为空");
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("SDUId", sduid);
        params.put("username", name);
        params.put("sex", gender != null ? gender : "男");
        // major 必须填有效的枚举值，null 会报400
        String majorValue = MAJOR_ENUM_MAP.getOrDefault(major, "MAJOR_0");
        params.put("major", majorValue);
        params.put("password", "123456");
        if (grade != null && !grade.isEmpty()) params.put("grade", grade);
        params.put("permission", "2");
        params.put("college", "软件学院");
        params.put("ethnic", "汉族");
        params.put("nation", "中国");
        params.put("PoliticsStatus", "群众");
        params.put("email", sduid + "@sdu.edu.cn");
        params.put("phone", "");
        if (className != null && !className.isEmpty()) {
            params.put("section", className);
        }

        if (editingStudent != null) {
            params.put("id", String.valueOf(editingStudent.getId()));
        }

        String endpoint = editingStudent != null ? "/admin/updateUser" : "/admin/addUser";

        btnSubmit.setDisable(true);
        NetworkUtils.postWithQueryParams(endpoint, params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        Platform.runLater(() -> {
                            ShowMessage.showInfoMessage("成功",
                                    editingStudent != null ? "已成功更新" : "已成功添加");
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
                        ShowMessage.showErrorMessage("错误", "数据解析失败: " + e.getMessage());
                        btnSubmit.setDisable(false);
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    ShowMessage.showErrorMessage("错误", "网络请求失败: " + e.getMessage());
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
