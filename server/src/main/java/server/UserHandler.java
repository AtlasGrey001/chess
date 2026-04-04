package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.*;
import service.exceptions.AlreadyTakenException;
import service.exceptions.BadRequestException;
import service.exceptions.UnauthorizedException;
import service.requests.LoginRequest;
import service.requests.LogoutRequest;
import service.requests.RegisterRequest;

public class UserHandler {
    private final UserService userService;
    private final Gson gson=new Gson();

    public UserHandler(UserService userService) {
        this.userService=userService;
    }

    public void register(Context ctx) {
        try {
            var request=gson.fromJson(ctx.body(),RegisterRequest.class);
            if (request==null ||
                    request.username()==null || request.username().isBlank() ||
                    request.password()==null || request.password().isBlank() ||
                    request.email()==null || request.email().isBlank()) {
                ctx.status(400).json(new ErrorResponse("Bad Request"));
                return;
            }
            var result=userService.register(request);
            ctx.status(200).json(result);

        } catch (AlreadyTakenException e) {
            ctx.status(403).json(new ErrorResponse("Already Taken"));
        } catch (BadRequestException e) {
            ctx.status(400).json(new ErrorResponse("Bad Request"));
        } catch (DataAccessException e) {
            ctx.status(500).json(new ErrorResponse(e.getMessage()));
        }
    }

    public void login(Context ctx) {
        try {
            var request=gson.fromJson(ctx.body(),LoginRequest.class);
            if (request==null ||
                    request.username()==null || request.username().isBlank() ||
                    request.password()==null || request.password().isBlank()) {
                ctx.status(400).json(new ErrorResponse("Bad Request"));
                return;
            }
            var result=userService.login(request);
            ctx.status(200).json(result);

        } catch (UnauthorizedException e) {
            ctx.status(401).json(new ErrorResponse("Unauthorized"));
        } catch (BadRequestException e) {
            ctx.status(400).json(new ErrorResponse("Bad Request"));
        } catch (DataAccessException e) {
            ctx.status(500).json(new ErrorResponse(e.getMessage()));
        }
    }

    public void logout(Context ctx) {
        try {
            var rawAuth=ctx.header("authorization");
            var auth = rawAuth == null ? null : rawAuth.replace("\"", "").trim();
            if (auth==null || auth.isBlank()) {
                ctx.status(401).json(new ErrorResponse("Unauthorized"));
                return;
            }

            var request=new LogoutRequest(auth);
            userService.logout(request);
            ctx.status(200).json(new Object());
        } catch (UnauthorizedException e) {
            ctx.status(401).json(new ErrorResponse("Unauthorized"));
        } catch (DataAccessException e) {
            ctx.status(500).json(new ErrorResponse(e.getMessage()));
        }
    }
}
