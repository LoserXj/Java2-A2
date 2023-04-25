package com.example.clientfx.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class Personal_Msg implements Serializable {
    private String sendBy;
    private String sendTo;
    private String content;
    private long timeStamp;
}
