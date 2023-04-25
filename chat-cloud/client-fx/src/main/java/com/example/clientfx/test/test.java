package com.example.clientfx.test;

import com.example.clientfx.CheckboxDialog;
import com.example.clientfx.pojo.Macro;
import com.example.clientfx.pojo.Message;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.*;

public class test extends Application {

    private int count = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();
        TextArea textArea = new TextArea();
        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> primaryStage.close());

        root.setCenter(textArea);
        root.setBottom(closeButton);

        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Close Confirmation");
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            if (count == 0) {
                count++;
                event.consume(); // 阻止默认的关闭操作
                Alert alert = new Alert(
                        Alert.AlertType.CONFIRMATION,
                        "Are you sure you want to close this application? Any unsaved changes will be lost.");
                alert.setHeaderText(null);
                alert.showAndWait()
                        .filter(response -> response == ButtonType.OK)
                        .ifPresent(response -> primaryStage.close());
                count--;
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
    }
