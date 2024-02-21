package com.example.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Command {
    AUTH("/auth") {// /auth nick1 pass1 -> ["/auth", "nick1", "pass1"]
        @Override
        public String[] parse(String commandText) {
            String[] params = commandText.split(COMMAND_DELIMITER);
            String login = params[1];
            String password = params[2];
            return new String[]{login, password};
        }
    },
    AUTHOK("/authOK"){// ["/authOK", "nick1"]
        @Override
        public String[] parse(String commandText) {
            return new String[]{commandText.split(COMMAND_DELIMITER)[1]};
        }
    },
    PRIVATE_MESSAGE("/w"){ // /w nick1 Длинное         сообщение de     ce   w -> ["/w", "nick1", "Длинное сообщениес пробелами"]
        @Override
        public String[] parse(String commandText) {//['/','w',' ',
            String[] split = commandText.split(COMMAND_DELIMITER, 3);
            //String nick = split[1];
            //String message = commandText.substring(4 + nick.length());
            return new String[]{split[1],split[2]};
        }
    },
    END("/end"){
        @Override
        public String[] parse(String commandText) {
            return new String[0];
        }
    }, // /end
    INFO("/info"){ // /info nick1 nick2 nick4
        @Override
        public String[] parse(String commandText) {
            String[] params = commandText.split(COMMAND_DELIMITER);
            String[] nicks = new String[params.length-1];
            for (int i = 0; i < params.length; i++) {
                if (!params[0].equals(params[i])){
                    nicks[i-1] = params[i];
                }
            }
            return nicks;
        }
    },
    ERROR("/error") { // /error Сообщение об ошибке
        @Override
        public String[] parse(String commandText) {
            String[] split = commandText.split(COMMAND_DELIMITER, 2);
            //String nick = split[1];
            //String message = commandText.substring(4 + nick.length());
            return new String[]{split[1]};
        }
    };

    private String command;

    private String[] params = new String[0];

    static final String COMMAND_DELIMITER = "\\s+";

    private static Map<String, Command> commandMap = Stream.of(Command.values()).collect(Collectors.toMap(Command::getCommand, Function.identity()));

    /*Map.of(
    "/auth", Command.AUTH,
    "/authOK", Command.AUTHOK,
    "/w", Command.PRIVATE_MESSAGE,
    "/end", Command.END,
    "/info", Command.INFO);*/
            /*new HashMap<>(){{
        commandMap.put("/auth", Command.AUTH);
        commandMap.put("/authOK", Command.AUTHOK);
        commandMap.put("/w", Command.PRIVATE_MESSAGE);
        commandMap.put("/end", Command.END);
        commandMap.put("/info", Command.INFO);
    }};*/
    Command(String command) {
        this.command = command;
    }

    public static boolean isCommand(String message) {
        return message.startsWith("/");
    }

    public String collectMessage(String[] message) {
        String command = this.getCommand();
        String join = String.join(COMMAND_DELIMITER, message);
        return command + " " + join;

    }

    public String[] getParams() {
        return params;
    }

    public String getCommand() {
        return command;
    }

    public static Command getCommand(String message) {
        message = message.trim();
        if (!isCommand(message)) {
            throw new RuntimeException("'" + message + "' not is command");
        }
        int index = message.indexOf(" ");
        String cmd = index > 0 ? message.substring(0, index) : message;

        return commandMap.get(cmd);
        /*for (Command value : Command.values()) {
            if(value.getCommand().equals(cmd)){
                return value;
            }
        }
        return null;*/
    }

    public abstract String[] parse(String commandText);

    //{ name: "Игорь",
    //  age : 21
    //}
}
