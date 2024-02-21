package com.example.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    Map<String, ClientHandler> clients;
    public Server(){

    }

    public void start() {
        try(ServerSocket serverSocket = new ServerSocket(8191);
            AuthServer authServer = new BaseAuthServer();) {
            clients = new HashMap<>();
            while(true) {
                System.out.println("Сервер ожидает подключения клиента...");
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket, authServer);
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isNickBusy(String nick) {

        return clients.containsKey(nick);
        /* for (ClientHandler client : clients) {
            if(client.getNick().equals(nick)){
                return true;
            }
        }*/
        //
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.put(clientHandler.getNick(), clientHandler);
    }

    public synchronized void broadcastMsg(String message) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
    }

    public synchronized void infoClients(){
        for (ClientHandler client : clients.values()) {
            client.sendUsers(clients.keySet());
        }
    }

    public synchronized void sendMsgToClient(ClientHandler from, String nickTo, String msg) {
        for(ClientHandler o : clients.values()) {
            if(o.getNick().equals(nickTo)) {
                o.sendMessage("от "+ from.getNick() +": "+ msg);
                from.sendMessage("клиенту "+ nickTo +": "+ msg);
                return;
            }
        }
        from.sendMessage("Участника с ником "+ nickTo+" нет в чат-комнате"); }

    public synchronized void unsubscribe(ClientHandler o) {
        clients.remove(o.getNick());
        }
}
