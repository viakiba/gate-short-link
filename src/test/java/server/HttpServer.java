package server;

import io.vertx.core.Vertx;

public class HttpServer {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();
        server.requestHandler(request -> {
            // Handle the request in here
            System.out.println(request.absoluteURI());
            request.response().setStatusCode(200).end("");
        });
        server.listen(8081);
    }

}
