package com.example.chat.server;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BaseAuthServer implements AuthServer{

      private Connection connection;

    public BaseAuthServer(){
      start();
      createTable();
    }





    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT nick FROM users WHERE  login = ? AND password = ?");){
            preparedStatement.setString(1, login);
            preparedStatement.setString(2,password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                return resultSet.getString("nick");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void start() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:chatdb.db");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTable(){
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE users IF NOT EXISTS(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "login TEXT NOT NULL," +
                    "password TEXT NOT NULL," +
                    "nick TEXT NOT NULL )");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void close()  {
        if (connection!=null){
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
