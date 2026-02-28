package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.LoginRequest;
import service.RegisterRequest;
import service.RegisterResult;
import service.UserService;

public class UserHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserService userService) {
        this.userService=userService;
    }

    public void register(Context ctx) throws DataAccessException {
        var request=gson.fromJson(ctx.body(),RegisterRequest.class);
        var result=userService.register(request);
        ctx.status(200);
        ctx.result(gson.toJson(result));
    }

    public void login(Context ctx) throws DataAccessException {
        var request=gson.fromJson(ctx.body(),LoginRequest.class);
        var result=userService.login(request);
        ctx.status(200);
        ctx.result(gson.toJson(result));
    }

    public void logout(Context ctx) throws DataAccessException {
        var authToken=ctx.header("authorization");
        userService.logout(authToken);
        ctx.status(200);
    }
}
