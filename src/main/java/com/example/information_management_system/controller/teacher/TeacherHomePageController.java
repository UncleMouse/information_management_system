package com.example.information_management_system.controller.teacher;

import com.example.information_management_system.entity.UserSession;
import com.example.information_management_system.util.JsonUtil;
import com.example.information_management_system.util.NetworkUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Objects;

public class TeacherHomePageController {

    private final Gson gson = new Gson();

    @FXML private Label welcomeLabel;
    @FXML private Label courseCountLabel;
    @FXML private Label studentCountLabel;
    @FXML private Label pendingScoreLabel;
    @FXML private VBox recentCoursesBox;
    @FXML private Label teacherNameDisplay;

    @FXML
    public void initialize() {
        String username = UserSession.getInstance().getUsername();
        if (username == null || username.isEmpty()) {
            username = "教师";
        }
        if (welcomeLabel != null) {
            welcomeLabel.setText("欢迎回来，" + username);
        }
        if (teacherNameDisplay != null) {
            teacherNameDisplay.setText(username);
        }
        fetchDashboardData();
        fetchRecentCourses();
    }

    private void fetchDashboardData() {
        NetworkUtils.get("/class/list", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        int totalCourses = arr.size();
                        int totalStudents = 0;
                        int pendingScore = 0;
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject course = arr.get(i).getAsJsonObject();
                            if (course.has("peopleNum")) {
                                totalStudents += course.get("peopleNum").getAsInt();
                            }
                            String status = course.has("status") ? course.get("status").getAsString() : "";
                            if ("待录入".equals(status) || "已开课".equals(status)) {
                                pendingScore++;
                            }
                        }
                        final int finalTotalStudents = totalStudents;
                        final int finalPendingScore = pendingScore;
                        Platform.runLater(() -> {
                            if (courseCountLabel != null) courseCountLabel.setText(String.valueOf(totalCourses));
                            if (studentCountLabel != null) studentCountLabel.setText(String.valueOf(finalTotalStudents));
                            if (pendingScoreLabel != null) pendingScoreLabel.setText(String.valueOf(finalPendingScore));
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {
                System.err.println("获取仪表盘数据失败: " + e.getMessage());
            }
        });
    }

    private void fetchRecentCourses() {
        NetworkUtils.get("/class/list", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        ObservableList<String> courseNames = FXCollections.observableArrayList();
                        int count = Math.min(arr.size(), 5);
                        for (int i = 0; i < count; i++) {
                            JsonObject course = arr.get(i).getAsJsonObject();
                            String name = course.has("name") ? course.get("name").getAsString() :
                                    course.has("code") ? course.get("code").getAsString() : "未命名课程";
                            String termStr = course.has("term") ? " | " + course.get("term").getAsString() : "";
                            courseNames.add(name + termStr);
                        }
                        Platform.runLater(() -> {
                            if (recentCoursesBox != null) {
                                recentCoursesBox.getChildren().clear();
                                for (String name : courseNames) {
                                    Label label = new Label("  " + name);
                                    label.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155; -fx-padding: 6 0;");
                                    recentCoursesBox.getChildren().add(label);
                                }
                                if (courseNames.isEmpty()) {
                                    Label empty = new Label("  暂无课程数据");
                                    empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-padding: 6 0;");
                                    recentCoursesBox.getChildren().add(empty);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    if (recentCoursesBox != null) {
                        recentCoursesBox.getChildren().clear();
                        Label err = new Label("  数据加载失败");
                        err.setStyle("-fx-font-size: 14px; -fx-text-fill: #ef4444;");
                        recentCoursesBox.getChildren().add(err);
                    }
                });
            }
        });
    }

    @FXML
    private void handleInputScore() {
        try {
            switchContent("teacher/ScoreInputContent.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleApplyNewCourse() {
        try {
            switchContent("teacher/ApplyNewCourse.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewSchedule() {
        try {
            switchContent("teacher/CourseScheduleContent_teacher.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchContent(String fxmlPath) throws IOException {
        Parent view = FXMLLoader.load(
                Objects.requireNonNull(getClass().getResource(
                        "/com/example/information_management_system/" + fxmlPath))
        );
        if (recentCoursesBox != null && recentCoursesBox.getScene() != null) {
            Pane contentArea = (Pane)
                    recentCoursesBox.getScene().lookup("#contentArea");
            if (contentArea == null) {
                contentArea = (Pane)
                        recentCoursesBox.getScene().lookup(".content-area");
            }
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
            }
        }
    }
}
