package com.example.client;

import com.example.common.pojo.User;
import lombok.Data;

import java.io.*;
import java.net.Socket;

@Data
public class Client  {
    private Socket socket;
    private String userName;
    private BufferedReader br;
    private PrintWriter pw;

    /**
     * @param userName: 设置user的姓名
     * @param socket:保存用户连接的socket
     * @throws IOException
     */
    public Client(String userName,Socket socket)throws IOException {
        this.userName = userName;
        this.socket = socket;
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        pw = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
    }
}
