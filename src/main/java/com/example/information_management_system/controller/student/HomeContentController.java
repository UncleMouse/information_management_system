package com.example.information_management_system.controller.student;

import com.example.information_management_system.entity.UserSession;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class HomeContentController {

    private final Gson gson = new Gson();

    @FXML private Label welcomeLabel;
    @FXML private Label selectedCourseCountLabel;
    @FXML private Label earnedCreditLabel;
    @FXML private Label currentTermLabel;
    @FXML private Label gpaRankLabel;
    @FXML private VBox courseListContainer;
    @FXML private Button goToSelectionBtn;
    @FXML private Button goToScoreBtn;
    @FXML private Button goToScheduleBtn;
    @FXML private VBox noticesContainer;

    @FXML
    public void initialize() {
        String username = UserSession.getInstance().getUsername();
        welcomeLabel.setText(username != null ? "欢迎回来，" + username : "欢迎回来");

        currentTermLabel.setText("加载中...");
        selectedCourseCountLabel.setText("-");
        earnedCreditLabel.setText("-");
        gpaRankLabel.setText("-");

        goToSelectionBtn.setOnAction(e -> navigateTo("courseSelectionBtn"));
        goToScoreBtn.setOnAction(e -> navigateTo("scoreSearchBtn"));
        goToScheduleBtn.setOnAction(e -> navigateTo("scheduleBtn"));

        loadDashboardData();
    }

    private void loadDashboardData() {
        loadSelectedCourses();
        loadGrades();
        loadCurrentTerm();
        loadNotices();
    }

    private void loadSelectedCourses() {
        NetworkUtils.get("/course-selection/results", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = extractArray(res, "data");
                        Platform.runLater(() -> {
                            int count = arr.size();
                            selectedCourseCountLabel.setText(String.valueOf(count));
                            courseListContainer.getChildren().clear();
                            if (arr.size() == 0) {
                                Label emptyLabel = new Label("暂无已选课程");
                                emptyLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px; -fx-padding: 16 0;");
                                courseListContainer.getChildren().add(emptyLabel);
                            } else {
                                int showCount = Math.min(arr.size(), 5);
                                for (int i = 0; i < showCount; i++) {
                                    JsonObject course = arr.get(i).getAsJsonObject();
                                    Label courseLabel = new Label();
                                    String name = getJsonString(course, "name", "courseName", "code");
                                    String teacher = getJsonString(course, "teacherName", "teacher");
                                    String type = getJsonString(course, "type");
                                    courseLabel.setText(name + " | " + teacher + " | " + type);
                                    courseLabel.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 13px; -fx-padding: 4 0;");
                                    courseListContainer.getChildren().add(courseLabel);
                                }
                                if (arr.size() > 5) {
                                    Label moreLabel = new Label("... 等共 " + arr.size() + " 门课程");
                                    moreLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px; -fx-padding: 4 0;");
                                    courseListContainer.getChildren().add(moreLabel);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> selectedCourseCountLabel.setText("错误"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> selectedCourseCountLabel.setText("加载失败"));
            }
        });
    }

    private void loadGrades() {
        NetworkUtils.get("/grade/getGrade", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = extractArray(res, "data");
                        Platform.runLater(() -> {
                            double totalCredit = 0;
                            double totalGpa = 0;
                            int gpaCount = 0;
                            for (int i = 0; i < arr.size(); i++) {
                                JsonObject grade = arr.get(i).getAsJsonObject();
                                if (grade.has("point")) {
                                    totalCredit += grade.get("point").getAsDouble();
                                }
                                if (grade.has("gpa") && !grade.get("gpa").isJsonNull()) {
                                    totalGpa += grade.get("gpa").getAsDouble();
                                    gpaCount++;
                                }
                            }
                            earnedCreditLabel.setText(String.format("%.1f", totalCredit));
                            if (gpaCount > 0) {
                                gpaRankLabel.setText(String.format("GPA: %.2f", totalGpa / gpaCount));
                            } else {
                                gpaRankLabel.setText("暂无数据");
                            }
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> earnedCreditLabel.setText("错误"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> earnedCreditLabel.setText("加载失败"));
            }
        });
    }

    private void loadCurrentTerm() {
        NetworkUtils.get("/term/getCurrentTerm", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        String term = res.get("data").getAsString();
                        Platform.runLater(() -> currentTermLabel.setText(term));
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> currentTermLabel.setText("加载失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> currentTermLabel.setText("加载失败"));
            }
        });
    }

    private void loadNotices() {
        NetworkUtils.get("/notice/getStudentNoticeList", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = extractArray(res, "data");
                        Platform.runLater(() -> {
                            noticesContainer.getChildren().clear();
                            if (arr.size() == 0) {
                                Label emptyLabel = new Label("暂无公告");
                                emptyLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
                                noticesContainer.getChildren().add(emptyLabel);
                            } else {
                                int showCount = Math.min(arr.size(), 4);
                                for (int i = 0; i < showCount; i++) {
                                    JsonObject notice = arr.get(i).getAsJsonObject();
                                    String title = notice.has("title") ? notice.get("title").getAsString() : "公告";
                                    String content = notice.has("content") ? notice.get("content").getAsString() : "";
                                    Label noticeLabel = new Label(title);
                                    noticeLabel.setStyle("-fx-text-fill: #4f6ef7; -fx-font-size: 13px; -fx-padding: 4 0; -fx-cursor: hand;");
                                    noticeLabel.setOnMouseClicked(e -> {
                                        if (!content.isEmpty()) ShowMessage.showInfoMessage(title, content);
                                    });
                                    noticesContainer.getChildren().add(noticeLabel);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        noticesContainer.getChildren().clear();
                        noticesContainer.getChildren().add(new Label("公告加载失败"));
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> {
                    noticesContainer.getChildren().clear();
                    noticesContainer.getChildren().add(new Label("公告加载失败"));
                });
            }
        });
    }

    private void navigateTo(String target) {
        try {
            String path = "/com/example/information_management_system/student/"
                    + switch (target) {
                        case "scheduleBtn" -> "CourseScheduleContent.fxml";
                        case "scoreSearchBtn" -> "ScoreSearchContent.fxml";
                        default -> "CourseSelectionContent.fxml";
                    };
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    Objects.requireNonNull(getClass().getResource(path)));
            javafx.scene.layout.VBox content = (VBox) loader.load();

            javafx.scene.layout.VBox area = (VBox) getSceneRoot().lookup("#contentArea");
            if (area != null) {
                area.getChildren().clear();
                area.getChildren().add(content);
            }
        } catch (Exception e) {
            ShowMessage.showErrorMessage("导航失败", "无法加载页面");
        }
    }

    private javafx.scene.Parent getSceneRoot() {
        return welcomeLabel.getScene().getRoot();
    }

    private JsonArray extractArray(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull()) return new JsonArray();
        if (el.isJsonArray()) return el.getAsJsonArray();
        if (el.isJsonObject()) {
            JsonObject dataObj = el.getAsJsonObject();
            if (dataObj.has("records")) return dataObj.getAsJsonArray("records");
            if (dataObj.has("data")) return dataObj.getAsJsonArray("data");
        }
        return new JsonArray();
    }

    private String getJsonString(JsonObject obj, String... keys) {
        for (String key : keys) {
            if (obj.has(key) && !obj.get(key).isJsonNull()) return obj.get(key).getAsString();
        }
        return "";
    }
}
