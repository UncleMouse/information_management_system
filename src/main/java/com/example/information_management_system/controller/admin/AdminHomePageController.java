package com.example.information_management_system.controller.admin;

import com.example.information_management_system.util.JsonUtil;
import com.example.information_management_system.util.NetworkUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        NetworkUtils.get("/class/pending", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        final int[] pending = {0};
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject obj = arr.get(i).getAsJsonObject();
                            String s = JsonUtil.safeGetString(obj, "status");
                            if ("PENDING".equalsIgnoreCase(s) || "待审核".equals(s)) pending[0]++;
                        }
                        Platform.runLater(() -> {
                            totalCoursesLabel.setText(String.valueOf(arr.size()));
                            pendingCoursesLabel.setText(String.valueOf(pending[0]));
                        });
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> { totalCoursesLabel.setText("--"); pendingCoursesLabel.setText("--"); });
                }
            }
            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> { totalCoursesLabel.setText("--"); pendingCoursesLabel.setText("--"); });
            }
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
        NetworkUtils.get("/notice/getAdminNoticeList", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        List<NoticeItem> items = new ArrayList<>();
                        int limit = Math.min(arr.size(), 5);
                        for (int i = 0; i < limit; i++) {
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
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
            Label contentLabel = new Label(item.content.length() > 80 ? item.content.substring(0, 80) + "..." : item.content);
            contentLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
            Label timeLabel = new Label(item.time);
            timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
            VBox card = new VBox(6);
            card.setStyle("-fx-background-color: #ffffff; -fx-padding: 12 16; -fx-background-radius: 6; -fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-cursor: hand;");
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
        t.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-wrap-text: true;");
        Label i = new Label("发布时间: " + time);
        i.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        TextArea ta = new TextArea(content);
        ta.setEditable(false); ta.setWrapText(true); ta.setPrefRowCount(15);
        ta.setStyle("-fx-font-size: 14px; -fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 6;");
        box.getChildren().addAll(t, i, new Separator(), ta);
        d.getDialogPane().setContent(box);
        d.showAndWait();
    }

    private void showEmptyNotices() {
        noticesContainer.getChildren().clear();
        Label empty = new Label("暂无最近通知");
        empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-padding: 20;");
        noticesContainer.getChildren().add(empty);
    }

    private static class NoticeItem {
        String title, content, time;
        NoticeItem(String t, String c, String tm) { title = t; content = c; time = tm; }
    }
}
