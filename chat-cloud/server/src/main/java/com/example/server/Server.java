package com.example.server;

import com.example.clientfx.pojo.Macro;
import com.example.clientfx.pojo.Message;
import com.example.common.pojo.User;
import com.example.server.dao.UserDao;
import com.example.server.dao.imp.UserDaoImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Time;
import java.util.*;

public class Server {
  private static Macro macro = new Macro();

  private static Socket testSocket;

  private static Socket updateSocket;

  private static HashMap<String,ObjectOutputStream> userMapOOS = new HashMap<>();

  private static HashMap<String,Socket> socketHashMap = new HashMap<>();
  @Autowired
  private static UserDao userDao = new UserDaoImpl();
  public static void Server()throws Exception{
        ServerSocket serverSocket = new ServerSocket(macro.getPort());
        testSocket = new Socket();
        updateSocket = new Socket();
        userDao.init();
        System.out.println("server is running......");
        while(true){
            Socket socket = serverSocket.accept();
            System.out.println("receive a request from "+socket.getInetAddress().getHostAddress()+":"+socket.getPort());
            ServerThread serverThread = new ServerThread(socket,userMapOOS,socketHashMap);
            serverThread.start();
        }
  }
  public static void updateOnlineUser(){
        new Thread(()->{
            System.out.println("start a thread to update online user");
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    List<User> userList = userDao.activateUser();
                    for(User user:userList){
                        try {
                            Socket userSocket = new Socket(user.getAddr(),Integer.parseInt(user.getPort()));
                            ObjectOutputStream oos = new ObjectOutputStream(userSocket.getOutputStream());
                            Message resp = new Message();
                            resp.setType("updateOnlineUser");
                            for(User user1:userList){
                                if(!user1.getUserName().equals(user.getUserName())){
                                    resp.getUserList().add(user1.getUserName());
                                }
                            }
                            oos.writeObject(resp);
                            oos.flush();
                            oos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            timer.schedule(task,3000,3000);
        }).start();
    }
  public static boolean isConnect(String userName)  {
        User user = userDao.selectUserByName(userName);
        if(user!=null&&user.getState()==1){
            try {
                testSocket.connect(new InetSocketAddress(user.getAddr(), Integer.parseInt(user.getPort())));
                return !testSocket.isClosed();
            }catch (Exception e){
               return false;
            }
        }
        return true;
    }

  public static void main(String[] args)throws Exception {
       Server();
  }
}
