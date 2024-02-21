package com.example.chat.client;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.WindowEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ChatController {

    @FXML
    private ListView<String> users;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passField;
    @FXML
    private VBox vBox;
    @FXML
    private TextArea textArea;
    @FXML
    private HBox sendBox;
    @FXML
    private TextField textField;
    @FXML
    private HBox hBox;

    private ChatClient client;

    public ChatController() {
        client = new ChatClient(this);
        client.openConnection();
    }

    public void authButton(ActionEvent actionEvent) {
        client.sendMessage("/auth " + loginField.getText() + " " + passField.getText());
    }

    public void sendButton(ActionEvent actionEvent) {
        String text = textField.getText();
        if (text.trim().isEmpty()) {

            return;
        }
        client.sendMessage(text);
        textField.clear();
        textField.requestFocus();
    }

    public void setAuth(boolean isAuth) {
        hBox.setVisible(!isAuth);
        vBox.setVisible(isAuth);
    }

    public void addedMessage(String message) {
        textArea.appendText(message + "\n");
    }

    public void infoUsers(String[] nicks) {
        users.getItems().clear();
        users.getItems().addAll(nicks);
    }


    public synchronized void showNotification(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, new ButtonType("Ой", ButtonBar.ButtonData.OK_DONE), new ButtonType("Выйти", ButtonBar.ButtonData.CANCEL_CLOSE));
        alert.setTitle("Ошибка подключения");
        Optional<ButtonType> buttonType = alert.showAndWait();
        Boolean isExit = buttonType.map(btn -> btn.getButtonData().isCancelButton()).orElse(false);
        if (isExit) {
            System.exit(0);
        }
    }

    public void selectUsers(MouseEvent mouseEvent) {
        if(mouseEvent.getClickCount() == 2){
            String message = textField.getText();
            String nick = users.getSelectionModel().getSelectedItem();
            textField.setText("/w " + nick + " " + message);
            textField.requestFocus();
            textField.selectEnd();
        }
    }
}