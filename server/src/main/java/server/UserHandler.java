package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.*;

public class UserHandler {
    private final UserService userService;
    private final Gson gson=new Gson();

    public UserHandler(UserService userService) {
        this.userService=userService;
    }

    public void register(Context ctx) throws DataAccessException, BadRequestException, AlreadyTakenException {
        var request=gson.fromJson(ctx.body(),RegisterRequest.class);
        var result=userService.register(request);
        ctx.status(200);
        ctx.json(result);
    }

    public void login(Context ctx) throws DataAccessException, BadRequestException, UnauthorizedException {
        var request=gson.fromJson(ctx.body(),LoginRequest.class);
        var result=userService.login(request);
        ctx.status(200);
        ctx.json(result);
    }

    public void logout(Context ctx) throws DataAccessException, UnauthorizedException {
        var authToken=ctx.header("authorization");
        var request=new LogoutRequest(authToken);
        userService.logout(request);
        ctx.status(200);
        ctx.result("{}");
    }
}
