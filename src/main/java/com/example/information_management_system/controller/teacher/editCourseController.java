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
    @FXML private TextField collegeField;
    @FXML private TextField classNumField;
    @FXML private TextArea introTextArea;
    @FXML private Button submitButton;
    @FXML private Button cancelButton;
    @FXML private Button backButton;
    @FXML private Label pageTitleLabel;

    private int currentCourseId;

    @FXML
    public void initialize() {
        if (categoryComboBox != null) categoryComboBox.getItems().setAll("必修", "限选", "任选");
        if (classroomComboBox != null) fetchClassrooms();
        if (termComboBox != null) fetchTerms();
        if (examinationComboBox != null) examinationComboBox.getItems().setAll("考试", "考查");
        if (submitButton != null) submitButton.setOnAction(e -> handleUpdate());
        if (cancelButton != null) cancelButton.setOnAction(e -> navigateBack());
        if (backButton != null) backButton.setOnAction(e -> navigateBack());
    }

    public void setCourseData(Course course) {
        this.currentCourseId = course.getId();
        if (pageTitleLabel != null) pageTitleLabel.setText("编辑课程 - " + course.getTeacherName());
        fetchCourseDetail(course.getId());
    }

    private void fetchCourseDetail(int courseId) {
        NetworkUtils.get("/class/detail/" + courseId, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonObject obj = res.getAsJsonObject("data");
                        Platform.runLater(() -> fillForm(obj));
                    }
                } catch (Exception ignored) {}
            }
            @Override
            public void onFailure(Exception ignored) {}
        });
    }

    private void fillForm(JsonObject obj) {
        if (courseNameField != null && obj.has("name"))
            courseNameField.setText(safeStr(obj, "name"));
        if (categoryComboBox != null && obj.has("type"))
            categoryComboBox.setValue(safeStr(obj, "type"));
        if (creditField != null && obj.has("point"))
            creditField.setText(String.valueOf(obj.get("point").getAsDouble()));
        if (classroomComboBox != null && obj.has("classroom")) {
            String cr = safeStr(obj, "classroom");
            if (!cr.isEmpty() && !classroomComboBox.getItems().contains(cr))
                classroomComboBox.getItems().add(0, cr);
            classroomComboBox.setValue(cr);
        }
        if (capacityField != null && obj.has("capacity"))
            capacityField.setText(String.valueOf(obj.get("capacity").getAsInt()));
        if (weekStartField != null && obj.has("weekStart"))
            weekStartField.setText(String.valueOf(obj.get("weekStart").getAsInt()));
        if (weekEndField != null && obj.has("weekEnd"))
            weekEndField.setText(String.valueOf(obj.get("weekEnd").getAsInt()));
        if (timeField != null)
            timeField.setText(safeStr(obj, "time"));
        if (termComboBox != null && obj.has("term")) {
            String tr = safeStr(obj, "term");
            if (!tr.isEmpty() && !termComboBox.getItems().contains(tr))
                termComboBox.getItems().add(0, tr);
            termComboBox.setValue(tr);
        }
        if (examinationComboBox != null) {
            int exam = obj.has("examination") ? obj.get("examination").getAsInt() : 1;
            examinationComboBox.setValue(exam == 1 ? "考试" : "考查");
        }
        if (regularRatioField != null && obj.has("regularRatio"))
            regularRatioField.setText(String.valueOf((int)(obj.get("regularRatio").getAsDouble() * 100)));
        if (finalRatioField != null && obj.has("finalRatio"))
            finalRatioField.setText(String.valueOf((int)(obj.get("finalRatio").getAsDouble() * 100)));
        if (collegeField != null)
            collegeField.setText(safeStr(obj, "college"));
        if (classNumField != null)
            classNumField.setText(safeStr(obj, "classNum"));
        if (introTextArea != null)
            introTextArea.setText(safeStr(obj, "intro"));
    }

    private String safeStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    private void handleUpdate() {
        if (!validateInputs()) return;

        String college = collegeField.getText().trim();
        if (college.isEmpty()) college = UserSession.getInstance().getCollege();
        if (college == null || college.isEmpty()) college = "软件学院";
        String classNum = classNumField.getText().trim();
        int weekStart = !StringUtil.isEmpty(weekStartField.getText()) ? Integer.parseInt(weekStartField.getText().trim()) : 0;
        int weekEnd = !StringUtil.isEmpty(weekEndField.getText()) ? Integer.parseInt(weekEndField.getText().trim()) : 0;

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("name", courseNameField.getText().trim());
        bodyMap.put("category", categoryComboBox.getValue());
        bodyMap.put("type", categoryComboBox.getValue());
        bodyMap.put("point", Double.parseDouble(creditField.getText().trim()));
        if (classroomComboBox.getValue() != null) bodyMap.put("classroom", classroomComboBox.getValue());
        bodyMap.put("capacity", Integer.parseInt(capacityField.getText().trim()));
        bodyMap.put("weekStart", weekStart);
        bodyMap.put("weekEnd", weekEnd);
        if (weekEnd > weekStart) bodyMap.put("period", weekEnd - weekStart + 1);
        bodyMap.put("time", "1");
        if (termComboBox.getValue() != null) bodyMap.put("term", termComboBox.getValue());
        bodyMap.put("examination", examinationComboBox.getValue().equals("考试") ? 1 : 0);
        bodyMap.put("regularRatio", Double.parseDouble(regularRatioField.getText().trim()) / 100.0);
        bodyMap.put("finalRatio", Double.parseDouble(finalRatioField.getText().trim()) / 100.0);
        bodyMap.put("college", college);
        bodyMap.put("classNum", classNum.isEmpty() ? "" : classNum);
        if (introTextArea.getText() != null && !introTextArea.getText().trim().isEmpty())
            bodyMap.put("intro", introTextArea.getText().trim());

        String jsonBody = gson.toJson(bodyMap);
        submitButton.setDisable(true);

        NetworkUtils.post("/class/update/" + currentCourseId, jsonBody, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    Platform.runLater(() -> {
                        if (res.has("code") && res.get("code").getAsInt() == 200) {
                            ShowMessage.showInfoMessage("成功", "已成功更新");
                            navigateBack();
                        } else {
                            ShowMessage.showErrorMessage("错误", res.has("msg")?res.get("msg").getAsString():"更新失败");
                            submitButton.setDisable(false);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> { ShowMessage.showErrorMessage("错误", "响应处理失败"); submitButton.setDisable(false); });
                }
            }
            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> { ShowMessage.showErrorMessage("错误", "网络请求失败，请检查连接"); submitButton.setDisable(false); });
            }
        });
    }

    private boolean validateInputs() {
        if (StringUtil.isEmpty(courseNameField.getText())) { ShowMessage.showWarningMessage("提示", "请输入课程名称"); return false; }
        if (categoryComboBox.getValue() == null) { ShowMessage.showWarningMessage("提示", "请选择课程分类"); return false; }
        if (StringUtil.isEmpty(creditField.getText())) { ShowMessage.showWarningMessage("提示", "请输入学分"); return false; }
        if (StringUtil.isEmpty(capacityField.getText())) { ShowMessage.showWarningMessage("提示", "请输入容量"); return false; }
        return true;
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
                        for (int i = 0; i < arr.size(); i++)
                            list.add(arr.get(i).getAsJsonObject().get("location").getAsString());
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
        } catch (IOException e) { e.printStackTrace(); }
    }

    private Pane findContentArea() {
        if (submitButton != null && submitButton.getScene() != null)
            return (Pane) submitButton.getScene().lookup("#contentArea");
        return null;
    }
}
