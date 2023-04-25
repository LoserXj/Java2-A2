package com.example.clientfx;

import com.example.clientfx.pojo.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.awt.Toolkit;

/**
 * .聊天界面
 */
public class ChatClientView extends Application {
  /**
     *. 常用的常量
     */
  private String currentUser;
  private boolean finish = false;
  /**
     * .常用的常量
     */
  private Macro macro = new Macro();
  /**
     *. 用户socket
     */
  private Socket socket;
  /**
     * .读取来自服务器的消息
     */
  private ObjectInputStream ois;
  /**
   *. 向服务器发送消息
     */
  private ObjectOutputStream oos;
  /**
     *. 常用的常量
     */
  private ObjectOutputStream oos1;
  /**
     *. 常用的常量
     */
  private ObjectInputStream ois1;
    /**
     * UI主控制
     */
  private Stage primaryStage;

  /**
     *. 显示在线用户和群聊
     */
  private ListView<String> userList = new ListView<>();

  /**
     *. 聊天展示
     */
  private TextArea chatArea = new TextArea();

  /**
     *. 输入框
     */
  private TextField inputField = new TextField();

  /**
     * .当前聊天对象
     */
  private String chatUser = null;
  /**
     * .当前聊天对象
     */

  private Socket updateSocket;
   /**
     * .当前聊天对象
     */
  private MyChatController controller;

  private Stage onlineUserPageStage = new Stage();

  private ListView<String> onlineUserListView = new ListView<>();
  private ListView<String> groupChatListView = new ListView<>();

  private Button addGroupButton;

  private Label onlineUserCount =new Label();

  /**
     * 构造聊天界面
     * @param socket
     * @param ois
     * @param oos
     */

  public ChatClientView(Socket socket, ObjectInputStream ois , ObjectOutputStream oos, String currentUser, List<String> groupList){
        this.currentUser = currentUser;
        try{
            this.updateSocket = new Socket(macro.getAddr(),macro.getPort());
            this.socket = socket;
            this.oos= oos;
            this.ois = ois;
            this.oos1 = new ObjectOutputStream(this.updateSocket.getOutputStream());
            this.ois1 = new ObjectInputStream(this.updateSocket.getInputStream());
            for(String groupName:groupList){
                this.groupChatListView.getItems().add(groupName);
            }
            this.groupChatListView.setOnMouseClicked(mouseEvent -> {
                String selectedItem = this.groupChatListView.getSelectionModel().getSelectedItem();
                this.controller.setType("groupChat");
                this.controller.setGroupName(selectedItem);
                this.controller.setChatObj(selectedItem);
                Message requestGroupChat = new Message();
                requestGroupChat.setType("requestGroupRecode");
                Group_Msg gm = new Group_Msg();
                gm.setGroupName(selectedItem);
                gm.setUserName(this.currentUser);
                requestGroupChat.getGroupMsg().add(gm);
                try{
                    this.oos.writeObject(requestGroupChat);
                    this.oos.flush();
                }catch (Exception e){
                    e.printStackTrace();
                }
                Message requestMember  =new Message();
                requestMember.setType("groupMember");
                requestMember.setGroupName(selectedItem);
                try{
                    this.oos.writeObject(requestMember);
                    this.oos.flush();
                }catch (Exception e){
                  e.printStackTrace();
                }
            });
        }catch (Exception e){
            if(socket==null){
                showInfo("initializing chat error, which maybe in socket");
            }else {
                showInfo("initializing chat error, which maybe not in socket");
            }
        }
  }


  public ChatClientView(){
  }

  @Override
  public void start(Stage primaryStage)throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("chat.fxml"));
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.setTitle("Chatting Client");
        this.primaryStage =primaryStage;
        this.primaryStage.show();
        this.controller = loader.getController();
        this.controller.setOos(this.oos);
        this.controller.setOis(this.ois);
        this.controller.setCurrentUser(this.currentUser);
        this.controller.setChatContentView();
        this.controller.setCurrentUsername(this.currentUser);

        //onlineUserPage
        VBox root = new VBox();
        root.setSpacing(10);
        // 在线用户列表
        Label onlineUserLabel = new Label("群聊成员");
        VBox onlineUserBox = new VBox(onlineUserLabel, onlineUserListView);

        // 群聊列表
        Label groupChatLabel = new Label("已建立的群聊");
        VBox groupChatBox = new VBox(groupChatLabel, groupChatListView);

        HBox listViewBox = new HBox(20, onlineUserBox, groupChatBox);
        root.getChildren().add(listViewBox);
        addGroupButton = new Button("新建群聊");
        root.getChildren().add(addGroupButton);
        addGroupButton.setOnAction(event->{
            MultiSelectDialog dialog = new MultiSelectDialog(this.controller.getChatList().getItems().stream().toList());
            dialog.showAndWait();
        });

        this.onlineUserCount.setText("在线人数："+0);
        root.getChildren().add(this.onlineUserCount);
        this.onlineUserPageStage.setScene(new Scene(root, 400, 300));
        this.onlineUserPageStage.setTitle("在线用户");
        this.onlineUserPageStage.show();

        this.primaryStage.setOnCloseRequest(event->{
            try {
                release();
                this.onlineUserPageStage.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
  }

  public void communicate(){
        Timer timer = new Timer();
        new Thread(()->{
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                        Message requestUpdate = new Message();
                        requestUpdate.setType("updateRequest");
                        requestUpdate.setUserName(currentUser);
                        try{
                            oos1.writeObject(requestUpdate);
                            oos1.flush();
                            Serializable obj = (Serializable) ois1.readObject();
                            Message resp = (Message) obj;
                            if(resp.getType().equals("updateOnlineUser")){
                                new Thread(()->Platform.runLater(()->{
                                    ObservableList<String> userList = FXCollections.observableArrayList(resp.getUserList());
                                    //onlineUserListView.setItems(userList);
                                    controller.setChatList(userList);
                                    onlineUserCount.setText("在线人数："+resp.getUserList().size());
                                    controller.setCntOnline(resp.getUserList().size());
//                                    onlineUserListView.setOnMouseClicked(clickEvent->{
//                                        String selectedItem = onlineUserListView.getSelectionModel().getSelectedItem();
//                                        controller.setSendTo(selectedItem);
//                                        controller.setType("privateChat");
//                                        Message personalRecode = new Message();
//                                        personalRecode.setType("requestPrivateRecode");
//                                        personalRecode.setUserName(currentUser);
//                                        personalRecode.setSendTo(selectedItem);
//                                        System.out.println(personalRecode);
//                                        try {
//                                            oos.writeObject(personalRecode);
//                                            oos.flush();
//                                        } catch (IOException e) {
//                                            throw new RuntimeException(e);
//                                        }
//                                    }
//                                    );
                                })).start();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                }
            },0,5000);
        }).start();
        while(true){
            try {
                Serializable obj = (Serializable) this.ois.readObject();
                Message message = (Message) obj;
                if(this.finish){
                    break;
                }
                if(message!=null){
                    new Thread(()->parseMessage(message)).start();
                }
            }catch (Exception e){
                timer.cancel();
                if(!isSocketConnectable("",macro.getPort())){
                    System.out.println("remote server is close ");
                }
                break;
            }
        }
  }
  public  boolean isSocketConnectable(String host, int port) {
        try(Socket socket = new Socket()) {
            // 设置连接超时时间
            socket.connect(new InetSocketAddress(host, port), 2000);
            return true;
        } catch(IOException e) {
            return false;
        }
    }
  public void parseMessage(Message message){
        switch (message.getType()){
            case "privateChat":
                Toolkit.getDefaultToolkit().beep();
                handlePersonalChatRecode(message);
                break;
            case "groupChat":
                Toolkit.getDefaultToolkit().beep();
                handleGroupRecode(message);
                break;
            case "createGroup":
                Toolkit.getDefaultToolkit().beep();
                handleCreateGroup(message);
                break;
            case "groupMember":
                System.out.println("groupMember");
                handleGroupMember(message);
        }
    }
    public void handleGroupMember(Message resp){
        new Thread(()->{
            Platform.runLater(()->{
                ObservableList<String> userList = FXCollections.observableArrayList(resp.getUserList());
                System.out.println(userList.toString());
                onlineUserListView.setItems(userList);
            });
        }).start();
  }
  public void handleCreateGroup(Message resp){
        if(resp.isNeedReminded()){
            new Thread(()->showInfo("you have joined "+resp.getGroupName())).start();
        }
        if(resp.getStatusCode().equals("error")&&resp.getUserName().equals(this.currentUser)){
            showInfo(resp.getErrorInfo());
        }else if(resp.getStatusCode().equals("success")){
            new Thread(()->{
                Platform.runLater(()->{
                    ObservableList<String> groupList = FXCollections.observableArrayList(resp.getGroupList());
                    groupChatListView.setItems(groupList);
                    groupChatListView.setOnMouseClicked(evevt->{
                        String selectedItem = this.groupChatListView.getSelectionModel().getSelectedItem();
                        this.controller.setType("groupChat");
                        this.controller.setGroupName(selectedItem);
                        Message requestGroupChat = new Message();
                        requestGroupChat.setType("requestGroupRecode");
                        Group_Msg gm = new Group_Msg();
                        gm.setGroupName(selectedItem);
                        gm.setUserName(this.currentUser);
                        requestGroupChat.getGroupMsg().add(gm);
                        try{
                            this.oos.writeObject(requestGroupChat);
                            this.oos.flush();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        Message requestMember  =new Message();
                        requestMember.setType("groupMember");
                        requestMember.setGroupName(selectedItem);
                        try{
                            this.oos.writeObject(requestMember);
                            this.oos.flush();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    });
                });
            }).start();
        }
  }
  public void handleGroupRecode(Message resp){
        if(resp.isNeedReminded()){
            new Thread(()->showInfo("you have received new message from "+resp.getGroupName()+" group")).start();
        }
        List<Group_Msg> gmList = resp.getGroupMsg().stream().sorted(Comparator.comparing(Group_Msg::getTimeStamp)).toList();
        List<ShowMessage> smList = new ArrayList<>();
        for(int i=0;i<gmList.size();i++){
            Group_Msg gm= gmList.get(i);
            ShowMessage sm = new ShowMessage();
            sm.setContent(gm.getContent());
            sm.setTimeStamp(gm.getTimeStamp());
            sm.setSentBy(gm.getUserName());
            smList.add(sm);
        }
        new Thread(()->{
            Platform.runLater(()->{
                this.controller.showMsg(smList);
            });
        }).start();;
    }
  public void handlePersonalChatRecode(Message resp){
        if(resp.isNeedReminded()){
           new Thread(()->showInfo("receiv new message from "+resp.getUserName())).start();
        }
        List<Personal_Msg> pmList = resp.getPersonalMsg().stream().sorted(Comparator.comparing(Personal_Msg::getTimeStamp)).toList();
        List<ShowMessage> showMessageList = new ArrayList<>();
        for(int i=0;i<pmList.size();i++){
            Personal_Msg pm = pmList.get(i);
            ShowMessage sm = new ShowMessage();
            sm.setTimeStamp(pm.getTimeStamp());
            sm.setContent(pm.getContent());
            sm.setSentBy(pm.getSendBy());
            showMessageList.add(sm);
        }
        new Thread(()->{
            Platform.runLater(()->{
                this.controller.showMsg(showMessageList);
            });
        }).start();;
  }

  public void showInfo(String message){
        Platform.runLater(()->{
            Alert alert = new Alert(Alert.AlertType.INFORMATION,message);
            alert.showAndWait();
        });
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

  public static void main(String[] args) {
        launch(args);
  }

  class MultiSelectDialog extends Dialog<List<String>> {

        private final List<String> items;
        public MultiSelectDialog(List<String> items) {
            // 设置对话框标题和头部文本
            setTitle("创建群聊");
            setHeaderText("选择群成员：");

            // 在对话框中创建包含复选框的VBox容器
            VBox vbox = new VBox(10);
            vbox.setPadding(new Insets(10));

            // 创建复选框并将其添加到VBox容器中
            for (String item : items) {
                CheckBox checkBox = new CheckBox(item);
                vbox.getChildren().add(checkBox);
            }

            // 将VBox容器添加到对话框的内容区域中
            getDialogPane().setContent(vbox);

            // 设置按钮类型（确定和取消）
            ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

            // 当单击“OK”时返回所选物品列表，在此同时建立socket通信
            setResultConverter(dialogButton -> {
                if (dialogButton == okButtonType) {
                    System.out.println("ok button");
                    List<String> selectedItems = new ArrayList<>();
                    try {
                        for (Node node : vbox.getChildren()) {
                            if (node instanceof CheckBox) {
                                CheckBox checkBox = (CheckBox) node;
                                if (checkBox.isSelected()) {
                                    selectedItems.add(checkBox.getText());
                                }
                            }
                        }
                        if(selectedItems.size()>0){
                            System.out.println("this");
                            selectedItems.add(currentUser);
                            Message requestCreateGroup = new Message();
                            requestCreateGroup.setUserName(currentUser);
                            requestCreateGroup.setGroupList(selectedItems);
                            requestCreateGroup.setType("createGroup");
                            Collections.sort(selectedItems);
                            StringBuilder sb = new StringBuilder("");
                            if(selectedItems.size()>3){
                                sb.append(selectedItems.get(0)).append(", ").append(selectedItems.get(1)).append(", ").append(selectedItems.get(2)).append("... ").append("("+selectedItems.size()+")");

                            }else {
                                if(selectedItems.size()==3){
                                    sb.append(selectedItems.get(0)).append(", ").append(selectedItems.get(1)).append(", ").append(selectedItems.get(2)).append(" (3)");
                                }else {
                                    sb.append(selectedItems.get(0)).append(", ").append(selectedItems.get(1)).append(" (2)");
                                }
                            }
                            requestCreateGroup.setGroupName(sb.toString());
                            oos.writeObject(requestCreateGroup);
                            oos.flush();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return selectedItems;
                }
                return null;
            });
            // 保存项目列表
            this.items = items;
        }

        public List<String> getSelectedItems() {
            Optional<List<String>> result = showAndWait();
            return result.orElse(Collections.emptyList());
        }
    }
}
