package com.example.information_management_system.controller.teacher;

import com.example.information_management_system.entity.Data;
import com.example.information_management_system.entity.UserSession;
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
            fetchClassrooms();
        }
        if (termComboBox != null) {
            fetchTerms();
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

    private void handleSubmit() {
        if (!validateInputs()) return;

        int weekStart = Integer.parseInt(weekStartField.getText().trim());
        int weekEnd = Integer.parseInt(weekEndField.getText().trim());
        String college = UserSession.getInstance().getCollege();

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("name", courseNameField.getText().trim());
        bodyMap.put("category", categoryComboBox.getValue());
        bodyMap.put("point", Double.parseDouble(creditField.getText().trim()));
        bodyMap.put("classroom", classroomComboBox.getValue());
        bodyMap.put("capacity", Integer.parseInt(capacityField.getText().trim()));
        bodyMap.put("weekStart", weekStart);
        bodyMap.put("weekEnd", weekEnd);
        bodyMap.put("period", weekEnd - weekStart + 1);      // 课时 = 周数
        bodyMap.put("time", timeField.getText().trim());
        bodyMap.put("term", termComboBox.getValue());
        bodyMap.put("examination", examinationComboBox.getValue().equals("考试") ? 1 : 0);
        bodyMap.put("regularRatio", Double.parseDouble(regularRatioField.getText().trim()) / 100.0);
        bodyMap.put("finalRatio", Double.parseDouble(finalRatioField.getText().trim()) / 100.0);
        bodyMap.put("intro", introTextArea.getText() != null ? introTextArea.getText().trim() : "");
        bodyMap.put("college", college != null ? college : "软件学院");
        // 尝试送整数序号：0=必修 1=限选 2=任选
        bodyMap.put("type", mapCourseTypeToOrdinal(categoryComboBox.getValue()));
        bodyMap.put("classNum", "");   // DB class_num NOT NULL，暂填空值，审批时分配

        String jsonBody = gson.toJson(bodyMap);
        String requestUrl = NetworkUtils.BaseUrl + "/class/create";
        System.out.println("========== 申请新课 请求开始 ==========");
        System.out.println("URL: " + requestUrl);
        System.out.println("Body: " + jsonBody);
        System.out.println("========================================");

        NetworkUtils.post("/class/create", jsonBody, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                System.out.println("========== 申请新课 响应成功 ==========");
                System.out.println("Response: " + result);
                System.out.println("========================================");
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    int code = res.has("code") ? res.get("code").getAsInt() : -1;
                    System.out.println("code: " + code);
                    if (code == 200) {
                        Platform.runLater(() -> {
                            ShowMessage.showInfoMessage("成功", "已成功添加");
                            navigateBack();
                        });
                    } else {
                        String msg = res.has("msg") ? res.get("msg").getAsString() : "申请失败";
                        System.out.println("业务错误 msg: " + msg);
                        Platform.runLater(() -> ShowMessage.showErrorMessage("错误", msg));
                    }
                } catch (Exception e) {
                    System.out.println("JSON解析异常: " + e.getMessage());
                    e.printStackTrace();
                    Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "响应处理失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("========== 申请新课 请求失败 ==========");
                System.out.println("异常类型: " + e.getClass().getName());
                System.out.println("异常信息: " + e.getMessage());
                e.printStackTrace();
                System.out.println("========================================");
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

    private int mapCourseTypeToOrdinal(String label) {
        if (label == null) return 2;
        switch (label) {
            case "必修": return 0;
            case "限选": return 1;
            default: return 2;
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
