package com.example.clientfx.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChatGroup implements Serializable {
    private int id;
    private String groupName;
    private int memberId;
}
