package com.example.clientfx.test;

import javafx.application.Platform;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ChatClient extends Application {

    // 聊天界面组件
    private ListView<String> userListView = new ListView<String>();
    private TextArea messageArea = new TextArea();
    private TextField messageField = new TextField();
    private PrintWriter writer;

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();
        root.setLeft(userListView);
        root.setCenter(messageArea);
        root.setBottom(messageField);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("聊天室");
        primaryStage.show();

        // 连接到服务器
        final String serverAddress = "127.0.0.1";
        final int serverPort = 9999;
        Socket socket = new Socket(serverAddress, serverPort);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);

        // 初始化用户列表
        updateUserList(reader.readLine());

        // 启动接收消息的线程
        Thread receiveThread = new Thread(() -> {
            try {
                while (true) {
                    String message = reader.readLine();
                    if (message == null || message.isEmpty()) {
                        break;
                    }
                    handleRemoteMessage(message);
                }
            } catch (IOException e) {
                System.out.println("Error receiving message: " + e);
            }
        });
        receiveThread.start();

        // 处理用户输入
        messageField.setOnAction(event -> {
            String message = messageField.getText();
            writer.println(message);
            messageField.clear();
            handleLocalMessage(message);
        });
    }

    /**
     * 刷新在线用户列表
     */
    private void updateUserList(String userList) {
        Platform.runLater(() -> {
            userListView.getItems().clear();
            userListView.getItems().addAll(userList.split(","));
        });
    }

    /**
     * 处理接收到的远程消息
     */
    private void handleRemoteMessage(String message) {
        Platform.runLater(() -> messageArea.appendText(">> " + message + "\n"));
    }

    /**
     * 处理发送的本地消息
     */
    private void handleLocalMessage(String message) {
        Platform.runLater(() -> messageArea.appendText("Me: " + message + "\n"));
    }

    public static void main(String[] args) {
       start();
    }
    public static void start(){
        launch();
    }

}
