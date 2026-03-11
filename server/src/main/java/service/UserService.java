package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;
import service.exceptions.AlreadyTakenException;
import service.exceptions.BadRequestException;
import service.exceptions.UnauthorizedException;
import service.requests.LoginRequest;
import service.requests.LogoutRequest;
import service.requests.RegisterRequest;
import service.results.LoginResult;
import service.results.RegisterResult;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess=dataAccess;
    }

    public RegisterResult register(RegisterRequest request)
            throws DataAccessException, AlreadyTakenException, BadRequestException {
        if (request==null ||
                request.username()==null || request.username().isBlank() ||
                request.username().equalsIgnoreCase("null") ||
                !request.username().matches("^[A-Za-z0-9_]+$") ||
                request.password()==null || request.password().isBlank() ||
                request.password().equalsIgnoreCase("null") ||
                request.email()==null || request.email().isBlank() ||
                request.email().equalsIgnoreCase("null")) {
            throw new BadRequestException("Bad Request");
        }
        var existing=dataAccess.getUser(request.username());
        if (existing!=null) {
            throw new AlreadyTakenException("Already Taken");
        }

        String hash=BCrypt.hashpw(request.password(),BCrypt.gensalt());
        var user=new UserData(request.username(),hash,request.email());
        dataAccess.createUser(user);

        var token=UUID.randomUUID().toString();
        var auth=new AuthData(token,user.username());
        dataAccess.createAuth(auth);
        return new RegisterResult(user.username(),token);
    }

    public LoginResult login(LoginRequest request)
            throws DataAccessException, BadRequestException, UnauthorizedException {
        if (request==null ||
                request.username()==null || request.username().isBlank() ||
                request.password()==null || request.password().isBlank()) {
            throw new BadRequestException("Bad Request");}
        var user=dataAccess.getUser(request.username());
        if (user==null) {throw new UnauthorizedException("Unauthorized");}
        if (!BCrypt.checkpw(request.password(),user.password())) {
            throw new UnauthorizedException("Unauthorized");
        }

        var token=UUID.randomUUID().toString();
        var auth=new AuthData(token,user.username());
        dataAccess.createAuth(auth);
        return new LoginResult(user.username(),token);
    }

    public void logout(LogoutRequest request)
            throws DataAccessException, UnauthorizedException {
        var authToken=request.authToken();
        if (authToken==null||authToken.isBlank()) {
            throw new UnauthorizedException("Unauthorized");}

        var auth=dataAccess.getAuth(authToken);
        if (auth==null) {
            throw new UnauthorizedException("Unauthorized");}
        dataAccess.deleteAuth(authToken);
    }
}
