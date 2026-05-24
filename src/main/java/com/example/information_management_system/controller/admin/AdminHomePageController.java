package com.example.information_management_system.controller.admin;

import com.example.information_management_system.util.NetworkUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
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
        fetchCount("/admin/studentList", totalStudentsLabel);
        fetchCount("/admin/teacherList", totalTeachersLabel);
        fetchCount("/class/admin/courseList", totalCoursesLabel);
        fetchCount("/section/list", totalClassesLabel);

        activeStudentsLabel.setText("--");
        activeTeachersLabel.setText("--");
        pendingCoursesLabel.setText("--");
        currentSemesterLabel.setText("--");
    }

    private void fetchCount(String url, Label label) {
        NetworkUtils.get(url, new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = extractArray(res, "data");
                        Platform.runLater(() -> label.setText(String.valueOf(arr.size())));
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> label.setText("--"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> label.setText("--"));
            }
        });
    }

    private JsonArray extractArray(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull()) return new JsonArray();
        if (el.isJsonArray()) return el.getAsJsonArray();
        if (el.isJsonObject()) {
            JsonObject dataObj = el.getAsJsonObject();
            if (dataObj.has("records")) return dataObj.getAsJsonArray("records");
            if (dataObj.has("data")) return dataObj.getAsJsonArray("data");
            if (dataObj.has("list")) return dataObj.getAsJsonArray("list");
        }
        return new JsonArray();
    }

    private void loadRecentNotices() {
        NetworkUtils.get("/notice/list", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = extractArray(res, "data");
                        List<NoticeItem> items = new ArrayList<>();
                        int limit = Math.min(arr.size(), 5);
                        for (int i = 0; i < limit; i++) {
                            JsonObject obj = arr.get(i).getAsJsonObject();
                            String title = obj.has("title") ? obj.get("title").getAsString() : "无标题";
                            String content = obj.has("content") ? obj.get("content").getAsString() : "";
                            String time = obj.has("publishTime") ? obj.get("publishTime").getAsString() : "";
                            items.add(new NoticeItem(title, content, time));
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
            Label titleLabel = new Label(item.title);
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
            Label contentLabel = new Label(item.content.length() > 80 ? item.content.substring(0, 80) + "..." : item.content);
            contentLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
            Label timeLabel = new Label(item.time);
            timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
            VBox card = new VBox(6);
            card.getStyleClass().add("notice-item");
            card.setStyle("-fx-background-color: #ffffff; -fx-padding: 12 16; "
                    + "-fx-background-radius: 6; -fx-border-color: #e2e8f0; -fx-border-radius: 6;");
            card.getChildren().addAll(titleLabel, contentLabel, timeLabel);
            noticesContainer.getChildren().add(card);
        }
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
