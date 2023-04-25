package com.example.common.pojo;

import lombok.Data;

import java.io.Serializable;
import java.sql.Time;
import java.util.List;

@Data
public class Message implements Serializable {
    private String type;
    private String userName;
    private String password;
    private String content;
    private Long  time;
    private List<String> userList;
    private String statusCode;
    private String errorInfo;

    public Message(){
    }
    public Message(String type,String content){
        this.type = type;
        this.content = content;
        this.time = System.currentTimeMillis();
    }


}
