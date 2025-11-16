package ui;

import java.util.Arrays;

import datamodel.LoginResult;
import datamodel.RegistrationResult;
import exception.ResponseException;
import server.ServerFacade;


public class PreLoginClient extends StandardClient {
    private String visitorName = null;
    private State state = State.SIGNEDOUT;
    private final String serverUrl;

    public PreLoginClient(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void run() {
        runLoop("♕ Welcome to 240 chess. Type Help to get started. ♕");
    }


    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "login" -> login(params);
                case "register" -> register(params);
                case "quit" -> "quit";
                case "help" -> help();
                case "" -> "";
                default -> "Unknown command. Type 'help' for options.";
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String login(String... params) throws ResponseException {
        if (params.length >= 2) {
            String username = params[0];
            String password = params[1];
            ServerFacade server = new ServerFacade(serverUrl);
            LoginResult result = server.login(username, password);
            new PostLoginClient(result.username(), serverUrl, server).run();
            return "quit";
        }
        throw new ResponseException(ResponseException.Code.ClientError, "Expected: <USERNAME> <PASSWORD>");
    }

    public String register(String... params) throws ResponseException {
        if (params.length >= 3) {
            String username = params[0];
            String password = params[1];
            String email = params[2];
            ServerFacade server = new ServerFacade(serverUrl);
            RegistrationResult result = server.register(username, password, email);
            new PostLoginClient(result.username(), serverUrl, server).run();
            return "quit";
        }
        throw new ResponseException(ResponseException.Code.ClientError, "Expected: <USERNAME> <PASSWORD> <EMAIL>");
    }


    public String help() {
        return """
                register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                login <USERNAME> <PASSWORD> - to play chess
                quit - playing chess
                help - with possible commands
                """;

    }
}
