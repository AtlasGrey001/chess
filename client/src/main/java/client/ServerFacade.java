package client;

import model.*;
import java.io.*;
import java.net.*;

public class ServerFacade {
    private final String baseUrl;

    public ServerFacade(int port) {this.baseUrl="http://localhost:"+ port;}

    public AuthData register(String username,String password,String email) throws Exception {
        var body="""
                {
                  "username": "%s",
                  "password": "%s",
                  "email": "%s"
                }
                """.formatted(username,password,email);
        return makeRequest("POST","/user",body,null,AuthData.class);
    }

    public AuthData login(String username,String password) throws Exception {
        var body="""
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username,password);
        return makeRequest("POST","/session",body,null,AuthData.class);
    }

    public void logout(String authToken) throws Exception {
        makeRequest("DELETE","/session",null,authToken,null);
    }

    public java.util.List<GameData> listGames(String authToken) throws Exception {
        var response=makeRequest("GET","/game",null,authToken,ListGameResponse.class);
        return response.games();
    }

    public GameData createGame(String authToken,String gameName) throws Exception {
        var body="""
                {
                  "gameName": "%s"
                }
                """.formatted(gameName);
        var response=makeRequest("POST","/game",body,authToken,CreateGameResponse.class);
        return response.game();
    }

    public void joinGame(String authToken,int gameId,String color) throws Exception {
        var body="""
                {
                  "playerColor": "%s",
                  "gameID": %d
                }
                """.formatted(color,gameId);
        makeRequest("PUT","/game",body,authToken,null);
    }

    public void clear() throws Exception {
        makeRequest("DELETE","/db",null,null,null);
    }

    private <T> T makeRequest(String method,String path,String body,String authToken,Class<T> responseClass) throws Exception {
        var url=new URL(baseUrl+path);
        var connection=(HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setDoInput(true);
        connection.setRequestProperty("Accept","application/json");
        if (authToken!=null) {
            connection.setRequestProperty("Authorization",authToken);
        }

        if (body!=null) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type","application/json");
            try (var out=connection.getOutputStream()) {out.write(body.getBytes());}
        }
        int status=connection.getResponseCode();
        InputStream stream=(status>=200 && status<300) ? connection.getInputStream() : connection.getErrorStream();

        // No response body expected
        if (responseClass==null) {
            if (status>=200 && status<300) {return null;}
            throw new RuntimeException("Server error");
        }

        // Read bytes directly from input
        String json;
        try (stream) {json=new String(stream.readAllBytes());}
        if (status>=200 && status<300) {return JsonUtil.fromJson(json, responseClass);}
        else {throw new RuntimeException("Server error: "+json);}
    }

}
