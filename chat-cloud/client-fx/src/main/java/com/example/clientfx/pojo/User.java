package com.example.clientfx.pojo;

import lombok.Data;

@Data
public class User {
    private String userName;
    private String password;
    private Integer userId;
    private Integer status;
    private String addr;
    private String port;
}
