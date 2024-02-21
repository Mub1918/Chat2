package com.example.chat.server;

import java.io.Closeable;
import java.io.IOException;

public interface AuthServer extends Closeable {
    String getNickByLoginAndPassword(String login, String password);

    void start();

    @Override
    void close() throws IOException;
}
