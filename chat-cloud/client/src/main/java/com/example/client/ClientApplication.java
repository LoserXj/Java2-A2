package com.example.client;

import com.example.client.View.LoginApplication;
import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) {
        Application.launch(LoginApplication.class,args);
        SpringApplication.run(ClientApplication.class, args);
    }

}
