package com.example.clientfx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class OnlineUserPage extends Application {

    private ListView<String> onlineUserListView = new ListView<>();
    private ListView<String> groupChatListView = new ListView<>();

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox();
        root.setSpacing(10);

        // 在线用户列表
        Label onlineUserLabel = new Label("在线用户");
        VBox onlineUserBox = new VBox(onlineUserLabel, onlineUserListView);

        // 群聊列表
        Label groupChatLabel = new Label("已建立的群聊");
        VBox groupChatBox = new VBox(groupChatLabel, groupChatListView);

        HBox listViewBox = new HBox(20, onlineUserBox, groupChatBox);
        root.getChildren().add(listViewBox);

        // 添加新群聊的按钮
        Button addGroupButton = new Button("创建新群聊");
        addGroupButton.setOnAction(event -> {
//            String groupName = "新群聊";
//            if (!groupChatListView.getItems().contains(groupName)) {
//                groupChatListView.getItems().add(groupName);
//            }
        });
        root.getChildren().add(addGroupButton);

        primaryStage.setScene(new Scene(root, 400, 300));
        primaryStage.setTitle("在线用户");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
