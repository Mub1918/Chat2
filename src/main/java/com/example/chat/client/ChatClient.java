package com.example.chat.client;

import javafx.application.Platform;

import java.io.*;
import java.net.Socket;

public class ChatClient {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private ChatController chatController;
    private String login;


    public ChatClient(ChatController chatController) {
        this.chatController = chatController;

    }



    public void openConnection() {
        try {
            socket = new Socket("localhost", 8191);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            Thread t = new Thread(() -> {
                try {
                    waitAuth();
                    readMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        closeConnection();
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Что-то с потоком при ожидании, при отправки сообщения серверу о " +
                                "закрытии соединения", e);
                    }
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка подключения к серверу", e);
        }

    }

    private void closeConnection() throws InterruptedException {
        sendMessage("/end");
        System.out.println("Закрываем соединение с сервером");
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void readMessages() throws IOException {
        while (true){
            try {
                String msg = in.readUTF();
                if("/end".equalsIgnoreCase(msg)){
                    chatController.setAuth(false);
                    break;
                }
                if(msg.startsWith("/info")){
                    msg = msg.substring(6);
                    String[] users = msg.split("\\s+"); // nick1 nick3 nick2
                    chatController.infoUsers(users);
                    continue;
                }
                chatController.addedMessage(msg);
                messagesHistory(msg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void waitAuth() {
        while (true) {
            try {
                String msgServer = in.readUTF();
                if (msgServer.startsWith("/authOK")) {
                    String[] parts = msgServer.split(" ");
                    String nick = parts[1];
                    chatController.setAuth(true);
                    chatController.addedMessage("Успешная авторизация под ником - " + nick);
                    break;
                }else{
                    Platform.runLater(() -> {
                        chatController.showNotification(msgServer);
                    });
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void messagesHistory( String message){
        File file = new File("history_" + login + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(message);
            writer.newLine();}
            catch(IOException e){
            e.printStackTrace();
            }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
