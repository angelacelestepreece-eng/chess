package server;

import com.google.gson.Gson;
import datamodel.ErrorMessage;
import io.javalin.*;
import io.javalin.http.Context;
import model.UserData;
import service.ServiceException;
import service.UserService;

import java.security.Provider;

public class Server {

    private final Javalin server;
    private UserService userService = new UserService();

    public Server() {
        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", ctx -> ctx.result("{}"));
        server.post("user", this::register);
        server.post("session", this::login);

    }

    private void register(Context ctx) {
        var serializer = new Gson();
        try {
            var req = serializer.fromJson(ctx.body(), UserData.class);
            var res = userService.register(req);
            ctx.status(200).result(serializer.toJson(res));
        } catch (ServiceException e) {
            ctx.status(e.getStatusCode()).result(serializer.toJson(new ErrorMessage("Error: " + e.getMessage())));
        } catch (Exception e) {
            ctx.status(500).result(serializer.toJson(new ErrorMessage("Error: " + e.getMessage())));
        }
    }

    private void login(Context ctx) {
        var serializer = new Gson();
        try {
            var req = serializer.fromJson(ctx.body(), UserData.class);
            var res = userService.login(req);
            ctx.status(200).result(serializer.toJson(res));
        } catch (ServiceException e) {
            ctx.status(e.getStatusCode()).result(serializer.toJson(new ErrorMessage("Error: " + e.getMessage())));
        } catch (Exception e) {
            ctx.status(500).result(serializer.toJson(new ErrorMessage("Error: " + e.getMessage())));
        }
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
