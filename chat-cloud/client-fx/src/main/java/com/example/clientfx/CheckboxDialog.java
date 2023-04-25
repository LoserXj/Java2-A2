package com.example.clientfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CheckboxDialog <T>{
    private final Dialog<List<T>> dialog = new Dialog<>();
    private final List<CheckBox> checkBoxList = new ArrayList<>();

    public CheckboxDialog(String title, String headerText, List<T> items) {
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);

        // Create the ListView of CheckBoxes
        VBox vBox = new VBox();
        ObservableList<T> observableList = FXCollections.observableArrayList(items);
        for (T item : items) {
            CheckBox checkBox = new CheckBox(item.toString());
            checkBox.setUserData(item);
            checkBoxList.add(checkBox);
            vBox.getChildren().add(checkBox);
        }

        // Set the result converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                // Collect all selected items
                List<T> selectedItems = new ArrayList<>();
                for (CheckBox cb : checkBoxList) {
                    if (cb.isSelected()) {
                        selectedItems.add((T) cb.getUserData());
                    }
                }
                return selectedItems;
            }
            return null;
        });

        // Add OK button and set the result when the button is clicked
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
        Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        ((Node) okButton).setDisable(true);
        for (CheckBox cb : checkBoxList) {
            cb.setOnAction(e -> okButton.setDisable(checkBoxList.stream().noneMatch(CheckBox::isSelected)));
        }

        // Add the ListView to the dialog pane
        dialog.getDialogPane().setContent(vBox);

    }

    public Optional<List<T>> showAndWait() {
        return dialog.showAndWait();
    }

    public static void main(String[] args) {

    }
}
