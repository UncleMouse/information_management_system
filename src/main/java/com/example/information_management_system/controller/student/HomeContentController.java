package com.example.information_management_system.controller.student;

import com.example.information_management_system.entity.UserSession;
import com.example.information_management_system.util.NetworkUtils;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;

import java.util.HashMap;
import java.util.Map;

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

        currentTermLabel.setText("加载中…");
        selectedCourseCountLabel.setText("-");
        earnedCreditLabel.setText("-");
        gpaRankLabel.setText("-");

        goToSelectionBtn.setOnAction(e -> navigateTo("选课中心"));
        goToScoreBtn.setOnAction(e -> navigateTo("成绩查询"));
        goToScheduleBtn.setOnAction(e -> navigateTo("课表查询"));

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
                                emptyLabel.getStyleClass().add("home-empty-text");
                                emptyLabel.setStyle("-fx-font-size: 13px; -fx-padding: 16 0;");
                                courseListContainer.getChildren().add(emptyLabel);
                            } else {
                                int showCount = Math.min(arr.size(), 5);
                                for (int i = 0; i < showCount; i++) {
                                    JsonObject course = arr.get(i).getAsJsonObject();
                                    String name = getJsonString(course, "name", "courseName", "code");
                                    String teacher = getJsonString(course, "teacherName", "teacher");
                                    String type = getJsonString(course, "type");
                                    String classroom = getJsonString(course, "classroom");
                                    Label nameLbl = new Label(name);
                                    nameLbl.getStyleClass().add("home-course-title");
                                    nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                                    Label infoLbl = new Label(teacher + " | " + type + (classroom.isEmpty()?"":" | " + classroom));
                                    infoLbl.getStyleClass().add("home-course-sub");
                                    infoLbl.setStyle("-fx-font-size: 12px;");
                                    VBox card = new VBox(4);
                                    card.getStyleClass().add("home-course-card");
                                    card.setStyle("-fx-padding: 10 14; -fx-background-radius: 8; -fx-border-radius: 8;");
                                    card.getChildren().addAll(nameLbl, infoLbl);
                                    courseListContainer.getChildren().add(card);
                                }
                                if (arr.size() > 5) {
                                    Label moreLabel = new Label("... 等共 " + arr.size() + " 门课程");
                                    moreLabel.getStyleClass().add("home-course-sub");
                                    moreLabel.setStyle("-fx-font-size: 12px; -fx-padding: 4 0;");
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
                Platform.runLater(() -> selectedCourseCountLabel.setText("数据加载失败"));
            }
        });
    }

    private void loadGrades() {
        // 先获取当前学期，再查询成绩
        NetworkUtils.get("/term/getCurrentTerm", new NetworkUtils.Callback<String>() {
            @Override public void onSuccess(String tResult) {
                String term = null;
                try { JsonObject tr = gson.fromJson(tResult, JsonObject.class); if (tr.has("code") && tr.get("code").getAsInt()==200) term = tr.get("data").getAsString(); } catch (Exception ignored) {}
                String finalTerm = term;
                Map<String, String> params = new HashMap<>();
                if (finalTerm != null) params.put("term", finalTerm);
                NetworkUtils.get("/grade/getGrade", params, new NetworkUtils.Callback<String>() {
                    @Override public void onSuccess(String result) {
                        try {
                            JsonObject res = gson.fromJson(result, JsonObject.class);
                            if (res.has("code") && res.get("code").getAsInt() == 200) {
                                JsonArray arr = extractArray(res, "data");
                                Platform.runLater(() -> {
                                    double totalCredit = 0; double totalGpa = 0;
                                    for (int i = 0; i < arr.size(); i++) {
                                        JsonObject g = arr.get(i).getAsJsonObject();
                                        if (g.has("point")) totalCredit += g.get("point").getAsDouble();
                                        if (g.has("grade")) { int gr = g.get("grade").getAsInt(); totalGpa += Math.max(0, (gr - 50) / 10.0); }
                                    }
                                    earnedCreditLabel.setText(String.format("%.1f", totalCredit));
                                    gpaRankLabel.setText(arr.size() > 0 ? String.format("GPA: %.2f", totalGpa / arr.size()) : "暂无数据");
                                });
                            }
                        } catch (Exception e) { Platform.runLater(() -> earnedCreditLabel.setText("错误")); }
                    }
                    @Override public void onFailure(Exception e) { Platform.runLater(() -> earnedCreditLabel.setText("加载失败")); }
                });
            }
            @Override public void onFailure(Exception e) { Platform.runLater(() -> earnedCreditLabel.setText("加载失败")); }
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
                    Platform.runLater(() -> currentTermLabel.setText("数据加载失败"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> currentTermLabel.setText("数据加载失败"));
            }
        });
    }

    private void loadNotices() {
        NetworkUtils.get("/notice/getStudentNoticeList", new NetworkUtils.Callback<String>() {
            @Override public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = extractArray(res, "data");
                        Platform.runLater(() -> {
                            noticesContainer.getChildren().clear();
                            if (arr.size() == 0) {
                                Label empty = new Label("暂无公告");
                                empty.getStyleClass().add("home-empty-text");
                                empty.setStyle("-fx-font-size: 13px; -fx-padding: 12 0;");
                                noticesContainer.getChildren().add(empty);
                            } else {
                                for (int i = 0; i < arr.size(); i++) {
                                    JsonObject n = arr.get(i).getAsJsonObject();
                                    String title = n.has("title") ? n.get("title").getAsString() : "公告";
                                    String content = n.has("content") ? n.get("content").getAsString() : "";
                                    String time = n.has("createTime") ? n.get("createTime").getAsString() : "";
                                    Label t = new Label(title.isEmpty() ? "无标题" : title);
                                    t.getStyleClass().add("home-notice-title");
                                    t.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
                                    Label c = new Label(content.length() > 60 ? content.substring(0, 60) + "..." : content);
                                    c.getStyleClass().add("home-notice-content");
                                    c.setStyle("-fx-font-size: 12px; -fx-wrap-text: true;");
                                    VBox card = new VBox(4);
                                    card.getStyleClass().add("home-notice-card");
                                    card.setStyle("-fx-padding: 10 14; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;");
                                    card.getChildren().addAll(t, c);
                                    final String ft = title, fc = content;
                                    card.setOnMouseClicked(e -> showNoticeDialog(ft, fc));
                                    noticesContainer.getChildren().add(card);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> { noticesContainer.getChildren().clear(); noticesContainer.getChildren().add(new Label("加载失败")); });
                }
            }
            @Override public void onFailure(Exception e) {
                Platform.runLater(() -> { noticesContainer.getChildren().clear(); noticesContainer.getChildren().add(new Label("加载失败")); });
            }
        });
    }

    /** 触发侧边栏按钮点击，实现导航 + 高亮同步 */
    private void navigateTo(String btnText) {
        if (welcomeLabel == null || welcomeLabel.getScene() == null) return;
        VBox nav = (VBox) welcomeLabel.getScene().lookup("#navContainer");
        if (nav == null) return;
        for (javafx.scene.Node node : nav.getChildren()) {
            if (node instanceof Button btn && btnText.equals(btn.getText())) {
                Platform.runLater(() -> btn.fire());
                return;
            }
        }
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

    private void showNoticeDialog(String title, String content) {
        Dialog<Void> d = new Dialog<>();
        d.setTitle("查看公告");
        d.getDialogPane().getButtonTypes().add(new ButtonType("关闭", ButtonBar.ButtonData.CANCEL_CLOSE));
        VBox box = new VBox(10);
        box.setPrefWidth(500); box.setStyle("-fx-padding: 16;");
        Label t = new Label(title != null ? title : "");
        t.getStyleClass().add("dialog-title-dark");
        t.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-wrap-text: true;");
        TextArea ta = new TextArea(content != null ? content : "");
        ta.setEditable(false); ta.setWrapText(true); ta.setPrefRowCount(15);
        ta.getStyleClass().add("dialog-content-dark");
        ta.setStyle("-fx-font-size: 14px; -fx-border-radius: 6;");
        box.getChildren().addAll(t, new Separator(), ta);
        d.getDialogPane().setContent(box);
        d.showAndWait();
    }
}
