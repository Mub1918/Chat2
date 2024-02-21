package com.example.chat.server;

import com.example.chat.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Set;

public class ClientHandler {

    private final Server server;
    private final Socket socket;
    private final AuthServer authServer;
    private final DataInputStream in;
    private final DataOutputStream out;
    private String nick;

    public String getNick() {
        return nick;
    }

    public ClientHandler(Server server, Socket socket, AuthServer authServer) {

        try {
            this.server = server;
            this.socket = socket;
            this.authServer = authServer;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            nick = "";
            Thread thread = new Thread(() -> {
                try {
                    authentication();
                    readMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Закрываем соединение");
                    closeConnection();
                }
            });
            thread.start();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка подключения клиента", e);
        }


    }

    private void closeConnection() {
        server.unsubscribe(this);
        server.broadcastMsg(nick +" вышел из чата");
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readMessage() throws IOException {
            while (true) {
                String message = in.readUTF(); //привет всем
                System.out.println(message);
                if(message.startsWith("/")){
                    if("/end".equals(message)){
                        System.out.println(message);
                        sendMessage("/end");
                        break;
                    }
                    if(message.startsWith("/w")){// /w baltazar message to message       **** ddd
                        String[] tokens = message.split("\\s+"); // * *, *    *, *\n    *
                        String nick = tokens[1];
                        String msg = message.substring(4 + nick.length());
                        server.sendMsgToClient(this,nick,msg);
                    }
                    continue;
                }
                server.broadcastMsg(nick + ": " + message);
            }
    }

    private void authentication() throws IOException {
            while (true) {
                String message = in.readUTF();// /auth login1 pass1 -> split [/auth,login1,pass1]
                if (Command.isCommand(message)) {
                    Command command = Command.getCommand(message);
                    String[] dataUser = command.parse(message);
                    if (command == Command.AUTH) {
                        String login = dataUser[0];
                        String pass = dataUser[1];
                        String nick = authServer.getNickByLoginAndPassword(login, pass);
                        if (nick != null) {
                            if (!server.isNickBusy(nick)) {
                                this.nick = nick;
                                server.subscribe(this);
                                sendMessage(Command.AUTHOK, nick);
                                server.broadcastMsg(nick + " зашел в чат");
                                server.infoClients();
                                return;
                            } else {
                                sendMessage(Command.ERROR, "Учетная запись занята");
                            }
                        } else {
                            sendMessage(Command.ERROR,"Неверные логин/пароль");
                        }
                    } else {
                        throw new RuntimeException("Неверный формат данных");
                    }
                }
            }
    }

    private void sendMessage(Command command, String... message) {
        sendMessage(command.collectMessage(message));
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка отправления сообщения", e);
        }
    }

    public void sendUsers(Set<String> infoClients) {
        System.out.println(infoClients);
        StringBuilder builder = new StringBuilder();
        infoClients.forEach((user) -> builder.append(user).append(" "));
        String users = builder.toString();
        sendMessage(Command.INFO, users);
    }
}
