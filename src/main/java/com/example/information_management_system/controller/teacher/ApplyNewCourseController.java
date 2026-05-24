package com.example.information_management_system.controller.teacher;

import com.example.information_management_system.entity.Data;
import com.example.information_management_system.entity.UserSession;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;
import com.example.information_management_system.util.StringUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ApplyNewCourseController {

    private final Gson gson = new Gson();

    @FXML private TextField courseNameField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField creditField;
    @FXML private ComboBox<String> classroomComboBox;
    @FXML private TextField capacityField;
    @FXML private TextField weekStartField;
    @FXML private TextField weekEndField;
    @FXML private TextField timeField;
    @FXML private ComboBox<String> termComboBox;
    @FXML private ComboBox<String> examinationComboBox;
    @FXML private TextField regularRatioField;
    @FXML private TextField finalRatioField;
    @FXML private TextArea introTextArea;
    @FXML private Button submitButton;
    @FXML private Button cancelButton;
    @FXML private Button backButton;

    @FXML
    public void initialize() {
        if (categoryComboBox != null) {
            categoryComboBox.getItems().setAll("必修", "限选", "任选");
            categoryComboBox.setValue("任选");
        }
        if (classroomComboBox != null) {
            classroomComboBox.setItems(Data.getInstance().getClassRoomList());
        }
        if (termComboBox != null) {
            termComboBox.setItems(Data.getInstance().getSemesterList());
        }
        if (examinationComboBox != null) {
            examinationComboBox.getItems().setAll("考试", "考查");
            examinationComboBox.setValue("考试");
        }
        if (submitButton != null) {
            submitButton.setOnAction(e -> handleSubmit());
        }
        if (cancelButton != null) {
            cancelButton.setOnAction(e -> navigateBack());
        }
        if (backButton != null) {
            backButton.setOnAction(e -> navigateBack());
        }
    }

    private void handleSubmit() {
        if (!validateInputs()) return;

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("name", courseNameField.getText().trim());
        bodyMap.put("category", categoryComboBox.getValue());
        bodyMap.put("point", Double.parseDouble(creditField.getText().trim()));
        bodyMap.put("classroom", classroomComboBox.getValue());
        bodyMap.put("capacity", Integer.parseInt(capacityField.getText().trim()));
        bodyMap.put("weekStart", Integer.parseInt(weekStartField.getText().trim()));
        bodyMap.put("weekEnd", Integer.parseInt(weekEndField.getText().trim()));
        bodyMap.put("time", timeField.getText().trim());
        bodyMap.put("term", termComboBox.getValue());
        bodyMap.put("examination", examinationComboBox.getValue());
        bodyMap.put("regularRatio", Double.parseDouble(regularRatioField.getText().trim()));
        bodyMap.put("finalRatio", Double.parseDouble(finalRatioField.getText().trim()));
        bodyMap.put("intro", introTextArea.getText() != null ? introTextArea.getText().trim() : "");
        bodyMap.put("teacherName", UserSession.getInstance().getUsername());
        bodyMap.put("college", UserSession.getInstance().getCollege());

        String jsonBody = gson.toJson(bodyMap);

        NetworkUtils.post("/class/createCourse", jsonBody, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        Platform.runLater(() -> {
                            ShowMessage.showInfoMessage("成功", "新课申请已提交！");
                            navigateBack();
                        });
                    } else {
                        String msg = res.has("msg") ? res.get("msg").getAsString() : "申请失败";
                        Platform.runLater(() -> ShowMessage.showErrorMessage("申请失败", msg));
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "响应处理失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("申请失败", e.getMessage()));
            }
        });
    }

    private boolean validateInputs() {
        if (StringUtil.isEmpty(courseNameField.getText())) {
            ShowMessage.showWarningMessage("提示", "请输入课程名称");
            return false;
        }
        if (categoryComboBox.getValue() == null) {
            ShowMessage.showWarningMessage("提示", "请选择课程分类");
            return false;
        }
        if (StringUtil.isEmpty(creditField.getText())) {
            ShowMessage.showWarningMessage("提示", "请输入学分");
            return false;
        }
        try {
            double credit = Double.parseDouble(creditField.getText().trim());
            if (credit <= 0) {
                ShowMessage.showWarningMessage("提示", "学分必须大于0");
                return false;
            }
        } catch (NumberFormatException e) {
            ShowMessage.showWarningMessage("提示", "请输入有效的学分");
            return false;
        }
        if (classroomComboBox.getValue() == null) {
            ShowMessage.showWarningMessage("提示", "请选择教室");
            return false;
        }
        if (StringUtil.isEmpty(capacityField.getText())) {
            ShowMessage.showWarningMessage("提示", "请输入容量");
            return false;
        }
        try {
            int cap = Integer.parseInt(capacityField.getText().trim());
            if (cap <= 0) {
                ShowMessage.showWarningMessage("提示", "容量必须大于0");
                return false;
            }
        } catch (NumberFormatException e) {
            ShowMessage.showWarningMessage("提示", "请输入有效的容量");
            return false;
        }
        if (StringUtil.isEmpty(weekStartField.getText()) || StringUtil.isEmpty(weekEndField.getText())) {
            ShowMessage.showWarningMessage("提示", "请输入起止周");
            return false;
        }
        if (StringUtil.isEmpty(timeField.getText())) {
            ShowMessage.showWarningMessage("提示", "请输入上课时间");
            return false;
        }
        if (termComboBox.getValue() == null) {
            ShowMessage.showWarningMessage("提示", "请选择学期");
            return false;
        }
        if (examinationComboBox.getValue() == null) {
            ShowMessage.showWarningMessage("提示", "请选择考试方式");
            return false;
        }
        try {
            double regular = Double.parseDouble(regularRatioField.getText().trim());
            double finalRatio = Double.parseDouble(finalRatioField.getText().trim());
            if (Math.abs(regular + finalRatio - 100.0) > 0.01) {
                ShowMessage.showWarningMessage("提示", "平时成绩和期末成绩比例之和应为100");
                return false;
            }
        } catch (NumberFormatException e) {
            ShowMessage.showWarningMessage("提示", "请输入有效的成绩比例");
            return false;
        }
        return true;
    }

    private void navigateBack() {
        try {
            StackPane contentArea = findContentArea();
            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(
                        Objects.requireNonNull(getClass().getResource(
                                "/com/example/information_management_system/teacher/CourseManagementContent.fxml"))
                );
                Parent view = loader.load();
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private StackPane findContentArea() {
        if (submitButton != null && submitButton.getScene() != null) {
            return (StackPane) submitButton.getScene().lookup("#contentArea");
        }
        return null;
    }
}
