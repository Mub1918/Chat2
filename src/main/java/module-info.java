module com.example.chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    exports com.example.chat.client;
    opens com.example.chat.client to javafx.fxml;
}