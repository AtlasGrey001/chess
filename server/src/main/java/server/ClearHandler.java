package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.exceptions.BadRequestException;
import service.ClearService;

public class ClearHandler {
    private final ClearService clearService;
    private final Gson gson=new Gson();

    public ClearHandler(ClearService clearService) {
        this.clearService=clearService;
    }

    public void handle(Context ctx) {
        try {
            clearService.clear();
            ctx.status(200).json(new Object());
        } catch (BadRequestException e) {
            ctx.status(400).json(new ErrorResponse("Bad Request"));
        } catch (DataAccessException e) {
            ctx.status(500).json(new ErrorResponse(e.getMessage()));
        }
    }
}
