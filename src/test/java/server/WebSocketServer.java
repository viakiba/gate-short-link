package server;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;

public class WebSocketServer {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();
        server.webSocketHandler(ws -> {
            ws.handler(h -> {
                ws.writeTextMessage("xxxx");
            });

        });

        server.listen(8081);
    }
}
