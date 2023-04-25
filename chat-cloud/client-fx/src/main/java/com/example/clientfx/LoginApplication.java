package com.example.clientfx;

import com.example.clientfx.pojo.Macro;
import com.example.clientfx.pojo.Message;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;


public class LoginApplication extends Application {

    private Stage primaryStage;
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

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

        Button registerButton = new Button("注册");
        registerButton.setOnAction(event->{
            try{
                String userName = usernameTextField.getText();
                String password = passwordField.getText();
                this.socket = new Socket(macro.getAddr(),macro.getPort());
                this.oos = new ObjectOutputStream(socket.getOutputStream());
                this.ois = new ObjectInputStream(socket.getInputStream());
                Message message = new Message();
                message.setType("register");
                message.setUserName(userName);
                message.setPassword(password);
                oos.writeObject(message);
                oos.flush();
                System.out.println("send register request");
                try{
                    Serializable obj = (Serializable) ois.readObject();
                    Message respInfo =(Message) obj;
                    if(respInfo.getStatusCode().equals("success")&&respInfo.getType().equals("registerRequest")){
                        showInfoError("register success");
                    }else {
                        showInfoError(respInfo.getErrorInfo());
                    }
                }catch (Exception e){
                    showInfoError("error in server");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        gridPane.add(registerButton,1,3);
        // 创建登录按钮
        Button loginButton = new Button("登录");
        loginButton.setOnAction(event -> {
            //获取输入的数据
            String userName = usernameTextField.getText();
            String password = passwordField.getText();
            try{
                this.socket = new Socket(macro.getAddr(),macro.getPort());
                this.oos = new ObjectOutputStream(socket.getOutputStream());
                this.ois = new ObjectInputStream(socket.getInputStream());
                Message message = new Message();
                message.setUserName(userName);
                message.setPassword(password);
                message.setType("login");
                oos.writeObject(message);
                oos.flush();
                System.out.println("send login request");
                try{
                    Serializable obj = (Serializable) ois.readObject();
                    Message resp = (Message) obj;
                    if(parseResp(resp)){
                        showInfo("login success");
                        System.out.println("login success and close login page");
                        Stage stage = new Stage();
                        this.primaryStage.close();
                        ChatClientView chatClientView = new ChatClientView(socket,ois,oos,userName,resp.getGroupList());
                        chatClientView.start(stage);
                        new Thread(chatClientView::communicate).start();
                    }else {
                        showInfoError(resp.getErrorInfo()+"error in login segment");
                    }
                }catch (Exception e){
                   e.printStackTrace();
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
    public void showInfo(String info){
        Alert alert = new Alert(Alert.AlertType.INFORMATION,info);
        alert.showAndWait();
    }
    public void connectError(){
       showInfo("can not connect to server");
    }

    public void release()throws Exception{
        if(socket!=null){
            socket.close();
        }
        if(ois!=null){
            ois.close();
        }
        if(oos!=null){
            oos.close();
        }
        if(primaryStage!=null){
            primaryStage.close();
        }
    }
    public void showInfoError(String errorInfo){
        Alert alert = new Alert(Alert.AlertType.INFORMATION,errorInfo);
        alert.showAndWait();
    }
    public boolean parseResp(Message resp){
        return resp.getStatusCode().equals("success");
    }

    public static void main(String[] args) {
        launch(args);
    }

}
