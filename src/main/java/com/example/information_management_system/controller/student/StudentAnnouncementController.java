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
import javafx.scene.layout.VBox;

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
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colCreator.setCellValueFactory(new PropertyValueFactory<>("creatorName"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("publishTime"));

        colId.setCellFactory(col -> new TableCell<AnnouncementItem, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
                setStyle("-fx-alignment: CENTER;");
            }
        });
        colCreator.setCellFactory(col -> new TableCell<AnnouncementItem, String>() {
            @Override protected void updateItem(String item, boolean empty) { super.updateItem(item, empty); setText(empty||item==null?null:item); setStyle("-fx-alignment: CENTER;"); }
        });
        colTime.setCellFactory(col -> new TableCell<AnnouncementItem, String>() {
            @Override protected void updateItem(String item, boolean empty) { super.updateItem(item, empty); setText(empty||item==null?null:fmt(item)); setStyle("-fx-alignment: CENTER;"); }
        });
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("查看");
            {
                viewBtn.setStyle("-fx-background-color: #4f6ef7; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 6 18; -fx-background-radius: 6; -fx-cursor: hand;");
                viewBtn.setOnAction(e -> viewNotice(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) { super.updateItem(v, empty); setGraphic(empty ? null : viewBtn); setStyle("-fx-alignment: CENTER;"); }
        });

        announcementTable.setItems(items);
        searchBtn.setOnAction(e -> loadAnnouncements(searchField.getText().trim()));
        searchField.setOnAction(e -> loadAnnouncements(searchField.getText().trim()));
        loadAnnouncements("");
    }

    private String fmt(String t) { return t != null ? t.replace("T", " ") : ""; }

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
                    Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "数据解析失败"));
                }
            }
            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> ShowMessage.showErrorMessage("错误", "数据加载失败: " + e.getMessage()));
            }
        });
    }

    private void viewNotice(AnnouncementItem item) {
        Dialog<Void> d = new Dialog<>();
        d.setTitle("查看公告");
        d.getDialogPane().getButtonTypes().add(new ButtonType("关闭", ButtonBar.ButtonData.CANCEL_CLOSE));
        VBox box = new VBox(10);
        box.setPrefWidth(500); box.setStyle("-fx-padding: 16;");
        Label t = new Label(item.getTitle());
        t.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-wrap-text: true;");
        Label i = new Label("发布者: " + (item.getCreatorName() != null ? item.getCreatorName() : "未知")
                + "    时间: " + (item.getPublishTime() != null ? item.getPublishTime().replace("T", " ") : ""));
        i.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        TextArea ta = new TextArea(item.getContent() != null ? item.getContent() : "");
        ta.setEditable(false); ta.setWrapText(true); ta.setPrefRowCount(15);
        ta.setStyle("-fx-font-size: 14px; -fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 6;");
        box.getChildren().addAll(t, i, new Separator(), ta);
        d.getDialogPane().setContent(box);
        d.showAndWait();
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
