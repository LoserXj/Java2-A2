package com.example.clientfx.pojo;

import lombok.Data;

@Data
public class ShowMessage {
    private String sentBy;
    private String content;
    private Long timeStamp;
}
