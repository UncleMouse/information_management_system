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
    @FXML private TextField capacityField;
    @FXML private TextField weekStartField;
    @FXML private TextField weekEndField;
    @FXML private ComboBox<String> termComboBox;
    @FXML private ComboBox<String> examinationComboBox;
    @FXML private TextField regularRatioField;
    @FXML private TextField finalRatioField;
    @FXML private TextField collegeField;
    @FXML private TextField classNumField;
    @FXML private TextArea introTextArea;
    @FXML private Button submitButton;
    @FXML private Button cancelButton;
    @FXML private Label formErrorLabel;
    @FXML private Button backButton;

    private final Map<Object, Label> fieldErrors = new HashMap<>();

    @FXML
    public void initialize() {
        if (categoryComboBox != null) { categoryComboBox.getItems().setAll("必修","限选","任选"); categoryComboBox.setValue("任选"); }
        if (termComboBox != null) fetchTerms();
        if (examinationComboBox != null) { examinationComboBox.getItems().setAll("考试","考查"); examinationComboBox.setValue("考试"); }
        if (submitButton != null) submitButton.setOnAction(e -> handleSubmit());
        if (cancelButton != null) cancelButton.setOnAction(e -> navigateBack());
        if (backButton != null) backButton.setOnAction(e -> navigateBack());
        // 为必填字段添加内联错误标签
        addErr(courseNameField); addErr(categoryComboBox); addErr(creditField);
        addErr(capacityField); addErr(weekStartField); addErr(weekEndField);
        addErr(termComboBox); addErr(examinationComboBox); addErr(regularRatioField); addErr(finalRatioField);
    }

    private void addErr(Object field) {
        javafx.scene.Node node = (javafx.scene.Node) field;
        if (node == null || node.getParent() == null) return;
        Label err = new Label();
        err.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 10px; -fx-padding: 2 0 0 0;"); err.setVisible(false);
        if (node.getParent() instanceof javafx.scene.layout.VBox vb) vb.getChildren().add(err);
        fieldErrors.put(field, err);
    }

    private void setFieldErr(Object field, String msg) {
        Label err = fieldErrors.get(field);
        if (err == null || field == null) return;
        if (msg == null || msg.isEmpty()) { err.setText(""); err.setVisible(false);
            if (field instanceof javafx.scene.control.TextInputControl tf) tf.setStyle(""); }
        else { err.setText("⚠ " + msg); err.setVisible(true);
            if (field instanceof javafx.scene.control.TextInputControl tf) tf.setStyle("-fx-border-color: #ef4444;"); }
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
        String err = validateInputs();
        if (err != null) { setFormError(err); return; }
        setFormError(null);

        int weekStart = Integer.parseInt(weekStartField.getText().trim());
        int weekEnd = Integer.parseInt(weekEndField.getText().trim());
        String college = collegeField.getText().trim();
        if (college.isEmpty()) college = UserSession.getInstance().getCollege();
        if (college == null || college.isEmpty()) college = "软件学院";
        String classNum = classNumField.getText().trim();

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("name", courseNameField.getText().trim());
        bodyMap.put("category", categoryComboBox.getValue());
        bodyMap.put("point", Double.parseDouble(creditField.getText().trim()));
        bodyMap.put("capacity", Integer.parseInt(capacityField.getText().trim()));
        bodyMap.put("weekStart", weekStart);
        bodyMap.put("weekEnd", weekEnd);
        bodyMap.put("period", weekEnd - weekStart + 1);
        bodyMap.put("time", "1");
        bodyMap.put("term", termComboBox.getValue());
        bodyMap.put("examination", examinationComboBox.getValue().equals("考试") ? 1 : 0);
        bodyMap.put("regularRatio", Double.parseDouble(regularRatioField.getText().trim()) / 100.0);
        bodyMap.put("finalRatio", Double.parseDouble(finalRatioField.getText().trim()) / 100.0);
        bodyMap.put("intro", introTextArea.getText() != null ? introTextArea.getText().trim() : "");
        bodyMap.put("college", college);
        bodyMap.put("type", categoryComboBox.getValue());
        bodyMap.put("classNum", classNum.isEmpty() ? "" : classNum);

        String jsonBody = gson.toJson(bodyMap);

        NetworkUtils.post("/class/create", jsonBody, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        Platform.runLater(() -> {
                            ShowMessage.showInfoMessage("成功", "已成功添加");
                            navigateBack();
                        });
                    } else {
                        String msg = res.has("msg") ? res.get("msg").getAsString() : "申请失败";
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

    private String validateInputs() {
        fieldErrors.values().forEach(l -> { l.setText(""); l.setVisible(false); });
        fieldErrors.keySet().forEach(f -> { if (f instanceof TextField tf) tf.setStyle(""); });
        boolean hasErr = false;
        if (StringUtil.isEmpty(courseNameField.getText())) { setFieldErr(courseNameField, "必填"); hasErr = true; }
        if (categoryComboBox.getValue() == null) { setFieldErr(categoryComboBox, "必选"); hasErr = true; }
        if (StringUtil.isEmpty(creditField.getText())) { setFieldErr(creditField, "必填"); hasErr = true; }
        else try { if (Double.parseDouble(creditField.getText().trim()) <= 0) { setFieldErr(creditField, "须>0"); hasErr = true; } } catch (NumberFormatException e) { setFieldErr(creditField, "无效数字"); hasErr = true; }
        if (StringUtil.isEmpty(capacityField.getText())) { setFieldErr(capacityField, "必填"); hasErr = true; }
        else try { if (Integer.parseInt(capacityField.getText().trim()) <= 0) { setFieldErr(capacityField, "须>0"); hasErr = true; } } catch (NumberFormatException e) { setFieldErr(capacityField, "无效数字"); hasErr = true; }
        if (StringUtil.isEmpty(weekStartField.getText())) { setFieldErr(weekStartField, "必填"); hasErr = true; }
        if (StringUtil.isEmpty(weekEndField.getText())) { setFieldErr(weekEndField, "必填"); hasErr = true; }
        if (termComboBox.getValue() == null) { setFieldErr(termComboBox, "必选"); hasErr = true; }
        if (examinationComboBox.getValue() == null) { setFieldErr(examinationComboBox, "必选"); hasErr = true; }
        try {
            double r = Double.parseDouble(regularRatioField.getText().trim());
            double f = Double.parseDouble(finalRatioField.getText().trim());
            if (Math.abs(r + f - 100.0) > 0.01) { setFieldErr(regularRatioField, "和须=100"); hasErr = true; }
        } catch (NumberFormatException e) { setFieldErr(regularRatioField, "无效"); hasErr = true; }
        return hasErr ? "请修正标红字段" : null;
    }

    private void setFormError(String msg) {
        if (formErrorLabel != null) {
            if (msg == null || msg.isEmpty()) { formErrorLabel.setText(""); formErrorLabel.setVisible(false); }
            else { formErrorLabel.setText("⚠ " + msg); formErrorLabel.setVisible(true); }
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
