package com.example.information_management_system.controller.teacher;

import com.example.information_management_system.entity.Data;
import com.example.information_management_system.entity.UserSession;
import com.example.information_management_system.model.Course;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;
import com.example.information_management_system.util.StringUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class editCourseController {

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
    @FXML private Label pageTitleLabel;

    private Course currentCourse;

    @FXML
    public void initialize() {
        if (categoryComboBox != null) {
            categoryComboBox.getItems().setAll("必修", "限选", "任选");
        }
        if (classroomComboBox != null) {
            fetchClassrooms();
        }
        if (termComboBox != null) {
            fetchTerms();
        }
        if (examinationComboBox != null) {
            examinationComboBox.getItems().setAll("考试", "考查");
        }
        if (submitButton != null) {
            submitButton.setOnAction(e -> handleUpdate());
        }
        if (cancelButton != null) {
            cancelButton.setOnAction(e -> navigateBack());
        }
        if (backButton != null) {
            backButton.setOnAction(e -> navigateBack());
        }
    }

    public void setCourseData(Course course) {
        this.currentCourse = course;
        if (pageTitleLabel != null) {
            pageTitleLabel.setText("编辑课程 - " + course.getCode());
        }
        if (courseNameField != null && course.getCode() != null) {
            courseNameField.setText(course.getCode());
        }
        if (categoryComboBox != null && course.getType() != null) {
            categoryComboBox.setValue(course.getType());
        }
        if (creditField != null) {
            creditField.setText(String.valueOf(course.getCredit()));
        }
        if (capacityField != null) {
            capacityField.setText(String.valueOf(course.getPeopleNum()));
        }
        if (course.getTerm() != null && termComboBox != null) {
            termComboBox.setValue(course.getTerm());
        }
    }

    private void fetchClassrooms() {
        NetworkUtils.get("/Teacher/getClassRoom", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = res.getAsJsonArray("data");
                        javafx.collections.ObservableList<String> list = javafx.collections.FXCollections.observableArrayList();
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject obj = arr.get(i).getAsJsonObject();
                            if (obj.has("location")) list.add(obj.get("location").getAsString());
                        }
                        Platform.runLater(() -> {
                            classroomComboBox.setItems(list);
                            Data.getInstance().getClassRoomList().setAll(list);
                        });
                    }
                } catch (Exception ignored) {}
            }
            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> classroomComboBox.setItems(Data.getInstance().getClassRoomList()));
            }
        });
    }

    private void fetchTerms() {
        NetworkUtils.get("/term/getTermList", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = res.getAsJsonArray("data");
                        javafx.collections.ObservableList<String> list = javafx.collections.FXCollections.observableArrayList();
                        for (int i = 0; i < arr.size(); i++)
                            list.add(arr.get(i).getAsJsonObject().get("term").getAsString());
                        Platform.runLater(() -> {
                            termComboBox.setItems(list);
                            Data.getInstance().getSemesterList().setAll(list);
                        });
                    }
                } catch (Exception ignored) {}
            }
            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> termComboBox.setItems(Data.getInstance().getSemesterList()));
            }
        });
    }

    private void handleUpdate() {
        if (!validateInputs()) return;

        String college = UserSession.getInstance().getCollege();
        int weekStart = !StringUtil.isEmpty(weekStartField.getText()) ? Integer.parseInt(weekStartField.getText().trim()) : 0;
        int weekEnd = !StringUtil.isEmpty(weekEndField.getText()) ? Integer.parseInt(weekEndField.getText().trim()) : 0;

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("name", courseNameField.getText().trim());
        bodyMap.put("category", categoryComboBox.getValue());
        bodyMap.put("type", mapTypeEnum(categoryComboBox.getValue()));
        bodyMap.put("point", Double.parseDouble(creditField.getText().trim()));
        if (classroomComboBox.getValue() != null) {
            bodyMap.put("classroom", classroomComboBox.getValue());
        }
        bodyMap.put("capacity", Integer.parseInt(capacityField.getText().trim()));
        bodyMap.put("weekStart", weekStart);
        bodyMap.put("weekEnd", weekEnd);
        if (weekEnd > weekStart) bodyMap.put("period", weekEnd - weekStart + 1);
        if (!StringUtil.isEmpty(timeField.getText())) {
            bodyMap.put("time", timeField.getText().trim());
        }
        if (termComboBox.getValue() != null) {
            bodyMap.put("term", termComboBox.getValue());
        }
        if (examinationComboBox.getValue() != null) {
            bodyMap.put("examination", examinationComboBox.getValue().equals("考试") ? 1 : 0);
        }
        if (!StringUtil.isEmpty(regularRatioField.getText())) {
            bodyMap.put("regularRatio", Double.parseDouble(regularRatioField.getText().trim()) / 100.0);
        }
        if (!StringUtil.isEmpty(finalRatioField.getText())) {
            bodyMap.put("finalRatio", Double.parseDouble(finalRatioField.getText().trim()) / 100.0);
        }
        bodyMap.put("college", college != null ? college : "软件学院");
        bodyMap.put("classNum", "");   // DB class_num NOT NULL，暂填空值
        if (introTextArea.getText() != null && !introTextArea.getText().trim().isEmpty()) {
            bodyMap.put("intro", introTextArea.getText().trim());
        }

        String jsonBody = gson.toJson(bodyMap);

        NetworkUtils.put("/class/update/" + currentCourse.getId(), jsonBody, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        Platform.runLater(() -> {
                            ShowMessage.showInfoMessage("成功", "已成功更新");
                            navigateBack();
                        });
                    } else {
                        String msg = res.has("msg") ? res.get("msg").getAsString() : "更新失败";
                        Platform.runLater(() -> ShowMessage.showErrorMessage("错误", msg));
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "响应处理失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "网络请求失败，请检查连接"));
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
        return true;
    }

    private String mapTypeEnum(String label) {
        if (label == null) return "ELECTIVE";
        switch (label) {
            case "必修": return "REQUIRED";
            case "限选": return "RESTRICTED_ELECTIVE";
            default: return "ELECTIVE";
        }
    }

    private void navigateBack() {
        try {
            Pane contentArea = findContentArea();
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

    private Pane findContentArea() {
        if (submitButton != null && submitButton.getScene() != null) {
            return (Pane) submitButton.getScene().lookup("#contentArea");
        }
        return null;
    }
}
