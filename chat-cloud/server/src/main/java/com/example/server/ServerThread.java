package com.example.server;

import com.example.clientfx.pojo.ChatGroup;
import com.example.clientfx.pojo.Group_Msg;
import com.example.clientfx.pojo.Message;
import com.example.clientfx.pojo.Personal_Msg;
import com.example.common.pojo.Macro;
import com.example.common.pojo.User;
import com.example.server.dao.UserDao;
import com.example.server.dao.imp.UserDaoImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerThread extends Thread{
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    private Macro macro = new Macro();

    private HashMap<String,ObjectOutputStream> userMapOOS ;

    private HashMap<String,Socket> socketHashMap;

   @Autowired
   private UserDao userDao = new UserDaoImpl();

    public ServerThread(Socket socket,HashMap<String,ObjectOutputStream> objectOutputStreamHashMap,HashMap<String,Socket> socketHashMap ) {
        try{
            this.socket = socket;
            this.ois = new ObjectInputStream(socket.getInputStream());
            this.oos = new ObjectOutputStream(socket.getOutputStream());
            this.userMapOOS = objectOutputStreamHashMap;
            this.socketHashMap = socketHashMap;
            //开启线程监听下线的用户,每隔3s执行一次

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void run(){
        while (true) {
            try {
                Serializable obj = (Serializable) ois.readObject();
                Message message = (Message) obj;
                parseMessage(message);
            } catch (Exception e) {
                User user = userDao.selectByAndrAndPort(socket.getInetAddress().toString().substring(1), String.valueOf(socket.getPort()));
                if(user!=null){
                    userDao.updateOffline(user.getUserName());
                }
                System.out.println(socket.getInetAddress().toString()+" : "+socket.getPort()+" is error");
                break;
            }
        }
    }
    public void parseMessage(Message message) {
        switch(message.getType()){
            case "login":
                handleLoginMessage(message);
                break;
            case "register":
                handleRegister(message);
                break;
            case "updateRequest":
                handleOnlineUser(message);
                break;
            case "privateChat":
                handlePrivateChat(message);
                break;
            case "requestPrivateRecode":
                handleRequestPrivateRecode(message);
                break;
            case "requestGroupRecode":
                handleRequestGroupRecode(message);
                break;
            case "groupChat":
                handleGroupChat(message);
                break;
            case "createGroup":
                handleCreateGroup(message);
                break;
            case "groupMember":
                handleGroupMember(message);
                break;
        }

    }
    public void handleGroupMember(Message message){
        List<ChatGroup> cgList = userDao.selectGroupMember(message.getGroupName());
        List<User> userList = new ArrayList<>();
        for(ChatGroup cg:cgList){
            User user = userDao.selectUserById(cg.getMemberId());
            userList.add(user);
        }
        System.out.println(cgList.size()+message.getGroupName());
        Message resp = new Message();
        resp.setType("groupMember");
        for(User user:userList){
            resp.getUserList().add(user.getUserName());
        }
        sendRespMessage(resp);
    }
    public void handleCreateGroup(Message message) {
        if (userDao.selectGroupMember(message.getGroupName()).size() > 0) {
            Message resp = new Message();
            resp.setType("createGroup");
            resp.setStatusCode("error");
            resp.setErrorInfo("the groupName has exists");
            resp.setUserName(message.getUserName());
            sendRespMessage(resp);
        } else {
            List<String> memberList = message.getGroupList();
            for (String userName : memberList) {
                User user = userDao.selectUserByName(userName);
                userDao.createGroup(user.getUserId(), message.getGroupName());
            }
            for (String userName : memberList) {
                if (socketHashMap.containsKey(userName) && socketHashMap.get(userName).isConnected()&&!userName.equals(message.getUserName())) {
                    Message resp = new Message();
                    resp.setType("createGroup");
                    resp.setNeedReminded(true);
                    resp.setStatusCode("success");
                    resp.setUserName(message.getUserName());
                    resp.setGroupName(message.getGroupName());
                    resp.setUserName(message.getUserName());
                    User user = userDao.selectUserByName(userName);
                    List<ChatGroup> groups = userDao.getGroup(user.getUserId());
                    for (ChatGroup group : groups) {
                        resp.getGroupList().add(group.getGroupName());
                    }
                    try {
                        userMapOOS.get(userName).writeObject(resp);
                        userMapOOS.get(userName).flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    Message resp = new Message();
                    resp.setType("createGroup");
                    resp.setStatusCode("success");
                    resp.setUserName(message.getUserName());
                    resp.setGroupName(message.getGroupName());
                    User user = userDao.selectUserByName(userName);
                    List<ChatGroup> groups = userDao.getGroup(user.getUserId());
                    for (ChatGroup group : groups) {
                        resp.getGroupList().add(group.getGroupName());
                    }
                    sendRespMessage(resp);
                }
            }
        }
    }
    public void handleGroupChat(Message message){
        Group_Msg gm = message.getGroupMsg().get(0);
        System.out.println("GroupChat"+gm.toString());
        userDao.insertGroupMsg(gm);
        List<ChatGroup> cgList = userDao.selectGroupMember(gm.getGroupName());
        List<User> userList = new ArrayList<>();
        for(ChatGroup cg:cgList){
            User user = userDao.selectUserById(cg.getMemberId());
            if(!user.getUserName().equals(gm.getUserName())){
                userList.add(user);
            }
        }
        List<Group_Msg> gmList = userDao.groupMsgRecode(gm.getGroupName());
        Message respSentBy = new Message();
        respSentBy.setType("groupChat");
        respSentBy.setGroupMsg(gmList);
        respSentBy.setUserName(message.getUserName());
        sendRespMessage(respSentBy);
        for(User user:userList){
            if(user.getUserName().equals(message.getUserName())){
                continue;
            }
            new Thread(()->{
                Message respSendTo = new Message();
                respSendTo.setNeedReminded(true);
                respSendTo.setUserName(message.getUserName());
                respSendTo.setGroupName(message.getGroupName());
                respSendTo.setType("groupChat");
                respSendTo.setGroupMsg(gmList);
                try{
                    this.userMapOOS.get(user.getUserName()).writeObject(respSendTo);
                    this.userMapOOS.get(user.getUserName()).flush();
                }catch (Exception e){
                    System.out.println(user.getUserName()+" is offline, there is no need to update chat message for him");
                }
            }).start();
        }
    }
    public void handleRequestGroupRecode(Message message){
        Group_Msg gm = message.getGroupMsg().get(0);
        System.out.println("RequestGroupRecode"+gm.toString());
        List<Group_Msg> gmList = userDao.groupMsgRecode(gm.getGroupName());
        Message resp = new Message();
        resp.setType("groupChat");
        resp.setGroupMsg(gmList);
        sendRespMessage(resp);
    }
    public void handleRequestPrivateRecode(Message message){
        List<Personal_Msg> pmList1 = userDao.personalMsgRecode(message.getUserName(),message.getSendTo());
        List<Personal_Msg> pmList2 = userDao.personalMsgRecode(message.getSendTo(),message.getUserName());
        List<Personal_Msg> pmList = Stream.concat(pmList1.stream(),pmList2.stream()).toList();
        Message resp = new Message();
        resp.setType("privateChat");
        resp.setPersonalMsg(pmList);
        sendRespMessage(resp);
    }
    public void handlePrivateChat(Message message){
        Personal_Msg pm = message.getPersonalMsg().get(0);
        userDao.insertPersonalMsg(pm);
        List<Personal_Msg> pmList1 = userDao.personalMsgRecode(pm.getSendBy(),pm.getSendTo());
        List<Personal_Msg> pmList2 = userDao.personalMsgRecode(pm.getSendTo(),pm.getSendBy());
        List<Personal_Msg> pmList = Stream.concat(pmList1.stream(), pmList2.stream()).toList();
        Message respSentBy = new Message();
        respSentBy.setUserName(message.getUserName());
        respSentBy.setType("privateChat");
        respSentBy.setPersonalMsg(pmList);
        sendRespMessage(respSentBy);
        User sendTo =userDao.selectUserByName(pm.getSendTo());
        new Thread(()->{
            try{
                Message respSendTo = new Message();
                respSendTo.setType("privateChat");
                respSendTo.setPersonalMsg(pmList);
                respSendTo.setNeedReminded(true);
                respSendTo.setUserName(message.getUserName());
                userMapOOS.get(sendTo.getUserName()).writeObject(respSendTo);
                userMapOOS.get(sendTo.getUserName()).flush();
            }catch (Exception e){
                System.out.println(sendTo.getUserName()+" is offline, there is no need to update chat message for him");
            }
        }).start();
    }

    public void handleOnlineUser(Message message){
        List<User> userList = userDao.activateUser();
        Message resp = new Message();
        resp.setType("updateOnlineUser");
        for(User user:userList){
            if(!message.getUserName().equals(user.getUserName())){
                resp.getUserList().add(user.getUserName());
            }
        }
        try {
               sendRespMessage(resp);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void handleRegister(Message registerInfo)  {
        System.out.println("register request");
        User user = userDao.selectUserByName(registerInfo.getUserName());
        if(user!=null){
            Message resp = new Message();
            resp.setType("registerRequest");
            resp.setStatusCode("error");
            resp.setErrorInfo("that user already exists");
            sendRespMessage(resp);
        }else {
            Message resp = new Message();
            userDao.register(registerInfo.getUserName(),registerInfo.getPassword());
            resp.setType("registerRequest");
            resp.setStatusCode("success");
            sendRespMessage(resp);
        }
    }
    public void handleLoginMessage(Message loginInfo){
                System.out.println("login request");
                User user = userDao.selectUserByName(loginInfo.getUserName());
                String inputPass = loginInfo.getPassword();
                if(user==null||!user.getPassword().equals(inputPass)){
                    try{
                        String errorInfo = "the user name does not exist";
                        if(user!=null){
                            errorInfo ="password is wrong";
                        }
                        Message resp = new Message();
                        resp.setType("loginResponse");
                        resp.setStatusCode("error");
                        resp.setErrorInfo(errorInfo);
                        sendRespMessage(resp);
                    }catch (Exception e){
                        System.out.println("the user is offline");
                    }
                }else if(user.getState()==1){
                    Message resp = new Message();
                    resp.setType("loginResponse");
                    resp.setStatusCode("error");
                    resp.setErrorInfo("you can not login in repeatedly");
                    sendRespMessage(resp);
                } else {
                    try{
                        Message resp = new Message();
                        resp.setType("loginResponse");
                        resp.setStatusCode("success");
                        this.userMapOOS.put(user.getUserName(),this.oos);
                        this.socketHashMap.put(user.getUserName(),this.socket);
                        userDao.updateStatus(user.getUserName(),this.socket.getInetAddress().toString().substring(1), String.valueOf(this.socket.getPort()));
                        List<ChatGroup> groups = userDao.getGroup(user.getUserId());
                        for(ChatGroup chatGroup:groups){
                            resp.getGroupList().add(chatGroup.getGroupName());
                        }
                        sendRespMessage(resp);
                    }catch (Exception e){
                       e.printStackTrace();
                    }
                }
    }
    public boolean isConnect(String userName)  {
           User user = userDao.selectUserByName(userName);
           if(user!=null&&user.getState()==1){
               try {
                   this.socket.connect(new InetSocketAddress(user.getAddr(), Integer.parseInt(user.getPort())));
                   return socket.isConnected();
               }catch (Exception e){
                   System.out.println("socket error in testConnect");
               }
           }
           return true;
    }
    public void sendRespMessage(Message resp){
       try{
           oos.writeObject(resp);
           oos.flush();
       }catch (Exception e){
           e.printStackTrace();
       }
    }

    public void release(){
        try{
            if(this.ois!=null){
                this.ois.close();
             }
            if(this.oos!=null){
                this.oos.close();
            }
        }catch (Exception e){
            System.out.println("release resource error");
        }
    }
}
