package com.example.clientfx.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class Message implements Serializable {
    private String type;
    private String userName;
    private String password;
    private String sendTo;
    private List<String> userList = new ArrayList<>();
    private List<String> groupList = new ArrayList<>();
    private String statusCode;
    private String errorInfo;
    private List<Group_Msg> GroupMsg = new ArrayList<>();
    private List<Personal_Msg> PersonalMsg = new ArrayList<>();
    private String groupName;
    private boolean needReminded = false;
    public Message(){
    }

}
