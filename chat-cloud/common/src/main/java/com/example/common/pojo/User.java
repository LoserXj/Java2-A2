package com.example.common.pojo;

import lombok.Data;

@Data
public class User {
    private String userName;
    private String password;
    private Integer userId;
    private Integer state;
    private String addr;
    private String port;
}
