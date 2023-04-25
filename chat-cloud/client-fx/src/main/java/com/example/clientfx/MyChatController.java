package com.example.clientfx;

import com.example.clientfx.pojo.Group_Msg;
import com.example.clientfx.pojo.Message;
import com.example.clientfx.pojo.Personal_Msg;
import com.example.clientfx.pojo.ShowMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.Data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Data
public class MyChatController {
    @FXML
    public Label ChatObj;
    @FXML
    private  ListView<String> chatList;

    @FXML
    private ListView<ShowMessage> chatContentList = new ListView<>();

    @FXML
    private TextArea inputArea;

    @FXML
    private Label currentUsername;

    @FXML
    private Label currentOnlineCnt;

    private Socket socket;

    private ObjectOutputStream oos;

    private ObjectInputStream ois;

    private String currentUser;


    private String sendTo;

    private String type="";

    private String groupName="";

    public void setChatObj(String name){
        Platform.runLater(()->{
            ChatObj.setText("聊天对象："+sendTo);
        });
    }
    public void setType(String type){
        this.type = type;
    }
    public void setGroupName(String groupName){
        this.groupName = groupName;
    }
    public void setSendTo(String sendTo){
        setChatObj(sendTo);
        this.sendTo = sendTo;
    }
    public void setCurrentUsername(String userName){
        Platform.runLater(()->{
            this.currentUsername.setText("当前用户："+userName);
        });
    }
    public void setChatList(ObservableList<String> userList){
        Platform.runLater(()->{
            this.chatList.setItems(userList);
            this.chatList.setOnMouseClicked(clickEvent-> {
                String selectedItem = this.chatList.getSelectionModel().getSelectedItem();
                this.sendTo = selectedItem;
                this.type = "privateChat";
                Message personalRecode = new Message();
                personalRecode.setType("requestPrivateRecode");
                personalRecode.setUserName(currentUser);
                personalRecode.setSendTo(selectedItem);
                System.out.println(personalRecode);
                try {
                    oos.writeObject(personalRecode);
                    oos.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    public void setCntOnline(Integer cnt){
        this.currentOnlineCnt.setText("在线人数："+String.valueOf(cnt));
    }
//    public void showMsg(ShowMessage showMessage){
//        this.chatContentList.getItems().add(showMessage);
//    }
    public void setChatContentView(){
        this.chatContentList.setCellFactory(new MessageCellFactory());
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    public void setOis(ObjectInputStream ois) {
        this.ois = ois;
    }
    public void setOos(ObjectOutputStream oos) {
        this.oos = oos;
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    @FXML
    public void initialize() {
        // 初始化控件和样式等方法
        // ...
    }
    @FXML
    public void createPrivateChat() {
        // 处理创建私聊室操作的逻辑
        // ...
    }

    @FXML
    public void createGroupChat() {

    }

    @FXML
    public void doSendMessage() {
        String text = inputArea.getText();
        String[] ms = text.split("\\n");
        System.out.println(ms.length);
        new Thread(()->{
            if (text != null && !text.isEmpty()) {
                // 处理发送消息到服务器的逻辑
                // ...
                if(this.type.equals("privateChat")){
                    Message request = new Message();
                    request.setType(this.type);
                    request.setUserName(this.currentUser);
                    Personal_Msg pm = new Personal_Msg();
                    pm.setSendBy(this.currentUser);
                    pm.setSendTo(this.sendTo);
                    pm.setContent(text);
                    pm.setTimeStamp(System.currentTimeMillis());
                    request.getPersonalMsg().add(pm);
                    sendMsg(request);
                }else if(this.type.equals("groupChat")){
                    Message request = new Message();
                    request.setType(this.type);
                    request.setGroupName(this.groupName);
                    Group_Msg gm = new Group_Msg();
                    gm.setUserName(this.currentUser);
                    gm.setContent(text);
                    gm.setTimeStamp(System.currentTimeMillis());
                    gm.setGroupName(this.groupName);
                    System.out.println(gm.toString());
                    request.getGroupMsg().add(gm);
                    sendMsg(request);
                }
                System.out.println("send chat msg to server");
                inputArea.clear();
            }
        }).start();

    }
    public void showMsg(List<ShowMessage> smList){
        ObservableList<ShowMessage> observableItems = FXCollections.observableArrayList(smList);
        this.chatContentList.getItems().clear();
        this.chatContentList.setItems(observableItems);
        System.out.println("finish show");
    }
    public void sendMsg(Message message){
        try{
            this.oos.writeObject(message);
            this.oos.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 用来显示聊天列表、在线用户列表和收到消息时的弹窗窗口（或者其他 UI 控制器）
     * @param messages 收到的未读消息
     */
    public void updateUI(List<Message> messages) {
        // 更新 UI 样式和数据
        // ...
    }

    public void updateChatList(List<String> onlineList){
        this.chatList = new ListView<>();
        for(String onlineUser:onlineList){
            this.chatList.getItems().add(onlineUser);
        }
    }
    public void test(){
        Platform.runLater(()->{
            List<String> list = new ArrayList<>();
            list.add("1");
            list.add("2");
            ObservableList<String> userList = FXCollections.observableArrayList(list);
            chatList.setItems(userList);
        });
    }


    private class MessageCellFactory implements Callback<ListView<ShowMessage>, ListCell<ShowMessage>> {
        @Override
        public ListCell<ShowMessage> call(ListView<ShowMessage> param) {
            return new ListCell<ShowMessage>() {

                @Override
                public void updateItem(ShowMessage msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(msg.getSentBy());
                    Label msgLabel = new Label(msg.getContent());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (currentUser.equals(msg.getSentBy())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }
}
