package com.example.information_management_system.controller.admin;

import com.example.information_management_system.util.JsonUtil;
import com.example.information_management_system.util.NetworkUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class AdminHomePageController {

    private final Gson gson = new Gson();

    @FXML private Label totalStudentsLabel;
    @FXML private Label totalTeachersLabel;
    @FXML private Label totalCoursesLabel;
    @FXML private Label totalClassesLabel;
    @FXML private Label activeStudentsLabel;
    @FXML private Label activeTeachersLabel;
    @FXML private Label pendingCoursesLabel;
    @FXML private Label currentSemesterLabel;
    @FXML private VBox noticesContainer;

    @FXML
    public void initialize() {
        loadDashboardStats();
        loadRecentNotices();
    }

    private void loadDashboardStats() {
        fetchStudents();
        fetchTeachers();
        fetchCoursesAndPending();
        fetchSectionCount();
        fetchCurrentSemester();
    }

    private void fetchStudents() {
        NetworkUtils.get("/admin/student/list", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        int total = arr.size();
                        int active = 0;
                        if (res.has("data") && res.get("data").isJsonObject()) {
                            JsonObject d = res.getAsJsonObject("data");
                            if (d.has("total")) total = d.get("total").getAsInt();
                        }
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject obj = arr.get(i).getAsJsonObject();
                            String s = JsonUtil.safeGetString(obj, "status");
                            if ("在读".equals(s) || "STUDYING".equalsIgnoreCase(s)) active++;
                        }
                        final int t = total, a = active;
                        Platform.runLater(() -> {
                            totalStudentsLabel.setText(String.valueOf(t));
                            activeStudentsLabel.setText(String.valueOf(a));
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> { totalStudentsLabel.setText("--"); activeStudentsLabel.setText("--"); });
                }
            }
            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> { totalStudentsLabel.setText("--"); activeStudentsLabel.setText("--"); });
            }
        });
    }

    private void fetchTeachers() {
        NetworkUtils.get("/admin/getTeacherList", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        int total = arr.size();
                        if (res.has("data") && res.get("data").isJsonObject()) {
                            JsonObject d = res.getAsJsonObject("data");
                            if (d.has("total")) total = d.get("total").getAsInt();
                            else if (d.has("page") && d.has("total")) total = d.get("total").getAsInt();
                        }
                        final int t = total;
                        Platform.runLater(() -> {
                            totalTeachersLabel.setText(String.valueOf(t));
                            activeTeachersLabel.setText(String.valueOf(t)); // 教师默认全部在职
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> { totalTeachersLabel.setText("--"); activeTeachersLabel.setText("--"); });
                }
            }
            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> { totalTeachersLabel.setText("--"); activeTeachersLabel.setText("--"); });
            }
        });
    }

    private void fetchCoursesAndPending() {
        // 并行查两个接口，list=全部课程数，pending=待审核数
        final int[] done = {0};
        final int[] totalVal = {0};
        final int[] pendingVal = {0};

        Map<String, String> clParams = new HashMap<>();
        clParams.put("pageSize", "200");
        NetworkUtils.get("/class/list", clParams, new NetworkUtils.Callback<String>() {
            @Override public void onSuccess(String r) {
                try { JsonObject res = gson.fromJson(r, JsonObject.class); if (res.has("code") && res.get("code").getAsInt()==200) totalVal[0] = JsonUtil.extractArray(res, "data").size(); }
                catch (Exception ignored) {}
                checkDone(done, totalVal, pendingVal);
            }
            @Override public void onFailure(Exception e) { checkDone(done, totalVal, pendingVal); }
        });
        NetworkUtils.get("/class/pending", new NetworkUtils.Callback<String>() {
            @Override public void onSuccess(String r) {
                try { JsonObject res = gson.fromJson(r, JsonObject.class); if (res.has("code") && res.get("code").getAsInt()==200) pendingVal[0] = JsonUtil.extractArray(res, "data").size(); }
                catch (Exception ignored) {}
                checkDone(done, totalVal, pendingVal);
            }
            @Override public void onFailure(Exception e) { checkDone(done, totalVal, pendingVal); }
        });
    }

    private void checkDone(int[] done, int[] totalVal, int[] pendingVal) {
        synchronized (done) { done[0]++; if (done[0] < 2) return; }
        Platform.runLater(() -> {
            totalCoursesLabel.setText(String.valueOf(totalVal[0]));
            pendingCoursesLabel.setText(String.valueOf(pendingVal[0]));
        });
    }

    private void fetchSectionCount() {
        Map<String, String> p = new java.util.HashMap<>();
        p.put("page", "1");
        p.put("size", "100");
        NetworkUtils.get("/section/getSectionListAll", p, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        Platform.runLater(() -> totalClassesLabel.setText(String.valueOf(arr.size())));
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> totalClassesLabel.setText("--"));
                }
            }
            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> totalClassesLabel.setText("--"));
            }
        });
    }

    private void fetchCurrentSemester() {
        NetworkUtils.get("/term/getCurrentTerm", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        String term = "未设置";
                        if (res.has("data") && !res.get("data").isJsonNull()) {
                            if (res.get("data").isJsonPrimitive()) {
                                term = res.get("data").getAsString();
                            } else if (res.get("data").isJsonObject()) {
                                term = JsonUtil.safeGetString(res.getAsJsonObject("data"), "term");
                            }
                        }
                        final String t = term;
                        Platform.runLater(() -> currentSemesterLabel.setText(t));
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> currentSemesterLabel.setText("--"));
                }
            }
            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> currentSemesterLabel.setText("--"));
            }
        });
    }

    private void loadRecentNotices() {
        Map<String, String> params = new java.util.HashMap<>();
        params.put("Status", "1");  // 1=已发布公告
        NetworkUtils.get("/notice/getAdminNoticeList", params, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        List<NoticeItem> items = new ArrayList<>();
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject obj = arr.get(i).getAsJsonObject();
                            items.add(new NoticeItem(
                                JsonUtil.safeGetString(obj, "title"),
                                JsonUtil.safeGetString(obj, "content"),
                                JsonUtil.safeGetString(obj, "publishTime").replace("T", " ")
                            ));
                        }
                        Platform.runLater(() -> renderNotices(items));
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> showEmptyNotices());
                }
            }
            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> showEmptyNotices());
            }
        });
    }

    private void renderNotices(List<NoticeItem> items) {
        noticesContainer.getChildren().clear();
        if (items.isEmpty()) { showEmptyNotices(); return; }
        for (NoticeItem item : items) {
            Label titleLabel = new Label(item.title.isEmpty() ? "无标题" : item.title);
            titleLabel.getStyleClass().add("home-notice-title");
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            Label contentLabel = new Label(item.content.length() > 80 ? item.content.substring(0, 80) + "..." : item.content);
            contentLabel.getStyleClass().add("home-notice-content");
            contentLabel.setStyle("-fx-font-size: 12px;");
            Label timeLabel = new Label(item.time);
            timeLabel.getStyleClass().add("home-course-sub");
            timeLabel.setStyle("-fx-font-size: 11px;");
            VBox card = new VBox(6);
            card.getStyleClass().add("home-notice-card");
            card.setStyle("-fx-padding: 12 16; -fx-background-radius: 6; -fx-border-radius: 6; -fx-cursor: hand;");
            card.setOnMouseClicked(e -> showNoticeDetail(item.title, item.content, "", item.time));
            card.getChildren().addAll(titleLabel, contentLabel, timeLabel);
            noticesContainer.getChildren().add(card);
        }
    }

    private void showNoticeDetail(String title, String content, String creator, String time) {
        Dialog<Void> d = new Dialog<>();
        d.setTitle("查看公告");
        d.getDialogPane().getButtonTypes().add(new ButtonType("关闭", ButtonBar.ButtonData.CANCEL_CLOSE));
        VBox box = new VBox(10);
        box.setPrefWidth(500); box.setStyle("-fx-padding: 16;");
        Label t = new Label(title);
        t.getStyleClass().add("dialog-title-dark");
        t.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-wrap-text: true;");
        Label i = new Label("发布时间: " + time);
        i.getStyleClass().add("dialog-info-dark");
        i.setStyle("-fx-font-size: 12px;");
        TextArea ta = new TextArea(content);
        ta.setEditable(false); ta.setWrapText(true); ta.setPrefRowCount(15);
        ta.getStyleClass().add("dialog-content-dark");
        ta.setStyle("-fx-font-size: 14px; -fx-border-radius: 6;");
        box.getChildren().addAll(t, i, new Separator(), ta);
        d.getDialogPane().setContent(box);
        d.showAndWait();
    }

    private void showEmptyNotices() {
        noticesContainer.getChildren().clear();
        Label empty = new Label("暂无最近通知");
        empty.getStyleClass().add("home-empty-text");
        empty.setStyle("-fx-font-size: 14px; -fx-padding: 20;");
        noticesContainer.getChildren().add(empty);
    }

    private static class NoticeItem {
        String title, content, time;
        NoticeItem(String t, String c, String tm) { title = t; content = c; time = tm; }
    }
}
