package com.example.client.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.common.pojo.Macro;
import com.example.common.pojo.Message;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;



public class LoginApplication extends Application {
    private Stage primaryStage;
    private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;

    private Macro macro = new Macro();

    @Override
    public void start(Stage primaryStage)throws Exception{
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(5);
        gridPane.setVgap(5);

        // 创建用户名标签及输入框
        Label usernameLabel = new Label("请输入用户名：");
        TextField usernameTextField = new TextField();
        gridPane.add(usernameLabel, 0, 0);
        gridPane.add(usernameTextField, 1, 0);

        // 创建密码标签及输入框
        Label passwordLabel = new Label("请输入密码：");
        PasswordField passwordField = new PasswordField();
        gridPane.add(passwordLabel, 0, 1);
        gridPane.add(passwordField, 1, 1);

        // 创建登录按钮
        Button loginButton = new Button("登录");
        loginButton.setOnAction(event -> {
            //获取输入的数据
            String userName = usernameTextField.getText();
            String password = passwordField.getText();
            try{
                this.socket = new Socket(macro.getAddr(),macro.getPort());
                this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.pw = new PrintWriter(socket.getOutputStream());
                String content = userName+macro.getDelimiter()+password;
                Message message = new Message("login",content);
                message.setUserName(userName);
                String joStr = JSON.toJSONString(message);
                System.out.println(message);
                System.out.println(joStr);
                pw.println(joStr);
                pw.flush();
                try{
                    String str = br.readLine();
                    JSONObject json =JSONObject.parseObject(str);
                    Message resp = JSON.toJavaObject(json,Message.class);
                    System.out.println(resp);
                    if(parseResp(resp)){
                        showInfo("login success");
                        System.out.println("login success");
                        this.primaryStage.hide();
                    }else {
                        this.primaryStage.hide();
                        Application.launch(LoginApplication.class);
                        showInfoError(resp);
                    }
                }catch (Exception e){
                    connectError();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        gridPane.add(loginButton, 1, 2);

        // 创建场景并显示
        Scene scene = new Scene(gridPane, 300, 150);
        primaryStage.setTitle("用户登录");
        primaryStage.setScene(scene);
        this.primaryStage=primaryStage;
        this.primaryStage.show();
    }
    public void release()throws Exception{
        if(socket!=null){
            socket.close();
        }
        if(br!=null){
            br.close();
        }
        if(pw!=null){
            pw.close();
        }
        if(primaryStage!=null){
            primaryStage.close();
        }
    }

    public void showInfo(String info){
        Alert alert = new Alert(Alert.AlertType.INFORMATION,info);
        alert.showAndWait();
    }
    public void connectError(){
        showInfo("can not connect to server");
        try {
            release();
        }catch (Exception ne){
            ne.printStackTrace();
        }
    }

    public void showInfoError(
            Message resp){
        Alert alert = new Alert(Alert.AlertType.INFORMATION,resp.getErrorInfo());
        alert.showAndWait();
    }
    public boolean parseResp(Message resp){
        return resp.getType().equals("response") && resp.getStatus().equals("success");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
