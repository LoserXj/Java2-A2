package com.example.clientfx;

import com.example.clientfx.pojo.Macro;
import com.example.clientfx.pojo.Message;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

public class RegisterApp extends Application {
    private Macro macro = new Macro();
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    private Stage primaryStage;

    public RegisterApp(Socket socket,ObjectInputStream ois,ObjectOutputStream oos){
        this.socket = socket;
        this.ois = ois;
        this.oos= oos;
    }
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
        Button registerButton = new Button("注册");
        registerButton.setOnAction(event->{
            String userName = usernameTextField.getText();
            String password = passwordField.getText();
            try{
                this.ois = new ObjectInputStream(this.socket.getInputStream());
                this.oos = new ObjectOutputStream(this.socket.getOutputStream());
                Message message = new Message();
                message.setType("register");
                message.setUserName(userName);
                message.setPassword(password);
                oos.writeObject(message);
                oos.flush();
                try{
                    Serializable obj = (Serializable) ois.readObject();
                    Message respInfo =(Message) obj;
                    if(respInfo.getStatusCode().equals("success")&&respInfo.getType().equals("registerRequest")){
                        showInfoError("register success");
                        Stage stage = new Stage();
                        LoginApplication loginApp = new LoginApplication();
                        release();
                        loginApp.start(stage);
                    }else {
                        showInfoError(respInfo.getErrorInfo());
                    }
                }catch (Exception e){
                   showInfoError("error in server");
                }
            }catch (Exception e){
                showInfoError("error in server");
            }
        });
        gridPane.add(registerButton, 1, 2);
        // 创建场景并显示
        Scene scene = new Scene(gridPane, 300, 150);
        primaryStage.setTitle("用户注册");
        primaryStage.setScene(scene);
        this.primaryStage=primaryStage;
        this.primaryStage.show();
    }
    public void showInfoError(String errorInfo){
        Alert alert = new Alert(Alert.AlertType.INFORMATION,errorInfo);
        alert.showAndWait();
    }
    public void release(){
        try {
            if (this.socket != null) {
                this.socket.close();
            }
            if(this.ois!=null){
                this.ois.close();
            }
            if(this.oos!=null){
                this.oos.close();
            }
            if(this.primaryStage!=null){
                this.primaryStage.close();
            }
        }catch (Exception e){
            showInfoError("error in close resources");
        }
    }
}
