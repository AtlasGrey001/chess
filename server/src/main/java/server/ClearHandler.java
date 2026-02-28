package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.ClearService;

public class ClearHandler {
    private final ClearService clearService;
    private final Gson gson=new Gson();

    public ClearHandler(ClearService clearService) {
        this.clearService=clearService;
    }

    public void handle(Context ctx) throws DataAccessException {
        clearService.clear();
        ctx.status(200);
        ctx.result(gson.toJson(new Object()));
    }
}
