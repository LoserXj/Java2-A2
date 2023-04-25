package com.example.clientfx.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class Group_Msg implements Serializable {
    private String userName;
    private String groupName;
    private String content;
    private long timeStamp;
}
