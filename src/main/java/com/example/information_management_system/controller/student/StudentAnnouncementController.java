package com.example.information_management_system.controller.student;

import com.example.information_management_system.util.JsonUtil;
import com.example.information_management_system.util.NetworkUtils;
import com.example.information_management_system.util.ShowMessage;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class StudentAnnouncementController {

    private final Gson gson = new Gson();

    @FXML private TableView<AnnouncementItem> announcementTable;
    @FXML private TableColumn<AnnouncementItem, String> colId;
    @FXML private TableColumn<AnnouncementItem, String> colTitle;
    @FXML private TableColumn<AnnouncementItem, String> colCreator;
    @FXML private TableColumn<AnnouncementItem, String> colTime;
    @FXML private TableColumn<AnnouncementItem, Void> colAction;
    @FXML private TextField searchField;
    @FXML private Button searchBtn;

    private final ObservableList<AnnouncementItem> items = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colCreator.setCellValueFactory(new PropertyValueFactory<>("creatorName"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("publishTime"));

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("查看");
            {
                viewBtn.getStyleClass().add("btn-primary");
                viewBtn.setStyle("-fx-padding: 1 18; -fx-translate-y: -2; -fx-translate-x: 4;");
                viewBtn.setOnAction(e -> {
                    AnnouncementItem item = getTableView().getItems().get(getIndex());
                    ShowMessage.showInfoMessage(item.getTitle(),
                            item.getContent() != null ? item.getContent() : "暂无内容");
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : viewBtn);
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });

        announcementTable.setItems(items);
        searchBtn.setOnAction(e -> loadAnnouncements(searchField.getText().trim()));
        searchField.setOnAction(e -> loadAnnouncements(searchField.getText().trim()));
        loadAnnouncements("");
    }

    private void loadAnnouncements(String keyword) {
        NetworkUtils.get("/notice/getStudentNoticeList", new NetworkUtils.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JsonObject res = gson.fromJson(result, JsonObject.class);
                    if (res.has("code") && res.get("code").getAsInt() == 200) {
                        JsonArray arr = JsonUtil.extractArray(res, "data");
                        ObservableList<AnnouncementItem> list = FXCollections.observableArrayList();
                        for (int i = 0; i < arr.size(); i++) {
                            JsonObject obj = arr.get(i).getAsJsonObject();
                            String title = getStr(obj, "title");
                            if (!keyword.isEmpty() && !title.contains(keyword)) continue;
                            AnnouncementItem item = new AnnouncementItem();
                            item.setId(String.valueOf(i + 1));
                            item.setTitle(title);
                            item.setCreatorName(getStr(obj, "creatorName"));
                            item.setPublishTime(getStr(obj, "publishTime"));
                            item.setContent(getStr(obj, "content"));
                            list.add(item);
                        }
                        Platform.runLater(() -> items.setAll(list));
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "数据解析失败，请稍后重试"));
                }
            }
            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "数据加载失败: " + e.getMessage()));
            }
        });
    }

    private String getStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    public static class AnnouncementItem {
        private String id, title, creatorName, publishTime, content;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getCreatorName() { return creatorName; }
        public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
        public String getPublishTime() { return publishTime; }
        public void setPublishTime(String publishTime) { this.publishTime = publishTime; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
