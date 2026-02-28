package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import service.LoginRequest;
import service.LoginResult;
import service.LogoutResult;
import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess=dataAccess;
    }

    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        if (request==null||request.username()==null||request.username().isBlank()||request.password()==null||request.password().isBlank()||request.email()==null||request.email().isBlank()) {
            throw new BadRequestException("bad request");
        }
        var existing=dataAccess.getUser(request.username());
        if (existing!=null) {
            throw new AlreadyTakenException("already taken");
        }
        var user=new UserData(request.username(),request.password(),request.email());
        dataAccess.createUser(user);
        var token=UUID.randomUUID().toString();
        var auth=new AuthData(token,user.username());
        dataAccess.createAuth(auth);
        return new RegisterResult(user.username(),token);
    }

    public LoginResult login(LoginRequest request)
            throws DataAccessException, BadRequestException, UnauthorizedException {
        if (request==null||request.username()==null||request.username().isBlank()||request.password()==null||request.password().isBlank()) {
            throw new BadRequestException("bad request");
        }
        var user=dataAccess.getUser(request.username());
        if (user==null) {
            throw new UnauthorizedException("unauthorized");
        }
        if (!user.password().equals(request.password())) {
            throw new UnauthorizedException("unauthorized");
        }
        var token=UUID.randomUUID().toString();
        var auth=new AuthData(token, user.username());
        dataAccess.createAuth(auth);
        return new LoginResult(user.username(),token);
    }

    public void logout(String authToken)
            throws DataAccessException, UnauthorizedException {
        if (authToken==null||authToken.isBlank()) {
            throw new UnauthorizedException("unauthorized");
        }
        var auth=dataAccess.getAuth(authToken);
        if (auth==null) {throw new UnauthorizedException("unauthorized");}
        dataAccess.deleteAuth(authToken);
    }
}
