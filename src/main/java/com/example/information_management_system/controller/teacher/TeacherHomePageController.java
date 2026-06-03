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
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class TeacherHomePageController {

    private final Gson gson = new Gson();

    @FXML private Label welcomeLabel;
    @FXML private Label courseCountLabel;
    @FXML private Label studentCountLabel;
    @FXML private Label pendingScoreLabel;
    @FXML private Label currentTermLabel;
    @FXML private VBox recentCoursesBox;
    @FXML private VBox noticesBox;

    @FXML
    public void initialize() {
        String username = UserSession.getInstance().getUsername();
        if (username == null || username.isEmpty()) { username = "教师"; }
        if (welcomeLabel != null) welcomeLabel.setText("欢迎回来，" + username);
        fetchDashboardData();
        fetchRecentCourses();
        loadNotices();
        loadCurrentTerm();
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
                                    String[] parts = name.split(" \\| ");
                                    Label nl = new Label(parts[0]);
                                    nl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
                                    VBox card = new VBox(4);
                                    card.setStyle("-fx-background-color: #eef2ff; -fx-padding: 10 14; -fx-background-radius: 8; -fx-border-color: #c7d2fe; -fx-border-radius: 8;");
                                    card.getChildren().add(nl);
                                    if (parts.length > 1) { Label tl = new Label(parts[1].trim()); tl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;"); card.getChildren().add(tl); }
                                    recentCoursesBox.getChildren().add(card);
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

    private void loadCurrentTerm() {
        NetworkUtils.get("/term/getCurrentTerm", new NetworkUtils.Callback<String>() {
            @Override public void onSuccess(String result) {
                try { JsonObject r = gson.fromJson(result, JsonObject.class); if (r.has("code") && r.get("code").getAsInt()==200) Platform.runLater(() -> currentTermLabel.setText(r.get("data").getAsString())); }
                catch (Exception ignored) {}
            }
            @Override public void onFailure(Exception ignored) {}
        });
    }

    private void loadNotices() {
        NetworkUtils.get("/notice/getTeacherNoticeList", new NetworkUtils.Callback<String>() {
            @Override public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        Platform.runLater(() -> {
                            if (noticesBox != null) noticesBox.getChildren().clear();
                            if (arr.size() == 0) {
                                Label empty = new Label("  暂无公告");
                                empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
                                if (noticesBox != null) noticesBox.getChildren().add(empty);
                            } else {
                                for (int i = 0; i < Math.min(arr.size(), 4); i++) {
                                    JsonObject n = arr.get(i).getAsJsonObject();
                                    String t = n.has("title") ? n.get("title").getAsString() : "公告";
                                    String c = n.has("content") ? n.get("content").getAsString() : "";
                                    Label tl = new Label(t.isEmpty() ? "无标题" : t);
                                    tl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1e293b;");
                                    Label cl = new Label(c.length() > 50 ? c.substring(0, 50) + "..." : c);
                                    cl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-wrap-text: true;");
                                    VBox card = new VBox(4);
                                    card.setStyle("-fx-background-color: #fff; -fx-padding: 10 14; -fx-background-radius: 8; -fx-border-color: #e8ecf0; -fx-border-radius: 8; -fx-cursor: hand;");
                                    final String ft = t, fc = c;
                                    card.setOnMouseClicked(e -> {
                                        Dialog<Void> d = new Dialog<>();
                                        d.setTitle(ft);
                                        d.getDialogPane().getButtonTypes().add(new ButtonType("关闭", ButtonBar.ButtonData.CANCEL_CLOSE));
                                        TextArea ta = new TextArea(fc);
                                        ta.setEditable(false); ta.setWrapText(true); ta.setPrefRowCount(12);
                                        ta.setStyle("-fx-font-size: 14px;");
                                        d.getDialogPane().setContent(ta);
                                        d.showAndWait();
                                    });
                                    card.getChildren().addAll(tl, cl);
                                    if (noticesBox != null) noticesBox.getChildren().add(card);
                                }
                            }
                        });
                    }
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Exception ignored) {}
        });
    }

    @FXML
    private void handleInputScore() {
        try {
            switchContent("成绩录入");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleApplyNewCourse() {
        try {
            switchContent("课程管理");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewSchedule() {
        try {
            switchContent("课表查看");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchContent(String btnText) {
        if (recentCoursesBox == null || recentCoursesBox.getScene() == null) return;
        VBox nav = (VBox) recentCoursesBox.getScene().lookup("#navContainer");
        if (nav == null) return;
        for (javafx.scene.Node node : nav.getChildren()) {
            if (node instanceof Button btn && btnText.equals(btn.getText())) {
                Platform.runLater(() -> btn.fire());
                return;
            }
        }
    }
}
