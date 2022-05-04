import handler.ProxyHandlerImplDiy
import io.vertx.core.Vertx
import io.vertx.core.file.FileSystem
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router

fun main(args: Array<String>) {
    dynamicProxy()
}

fun initConfig(function: (vertx : Vertx,json: JsonObject) -> Unit) {
    var vertx = Vertx.vertx()
    val fs: FileSystem = vertx.fileSystem()
    fs.readFile("config/config.json") { ar ->
        if (ar.succeeded()) {
            val config = ar.result().toString()
            var jsonObject = JsonObject(config)
            constant.BackEndIdentifyHeaderName = jsonObject.getString("backEndIdentifyHeaderName","Identify-Value")
            constant.ForwardedHost = jsonObject.getBoolean("forwardedHost", true)
            constant.Config = jsonObject
            function(vertx,jsonObject)
        } else {
            println("read file failed")
        }
    }
}

private fun dynamicProxy() {
    initConfig(fun (vertx : Vertx,config: JsonObject){
        vertx.close().onSuccess{
                constant.ProxyHandlerImplDiy.initProxyHandler(fun (vertx:Vertx){
                var port = config.getInteger("gatePort", 8080)
                var proxyServer = vertx.createHttpServer()
                var proxyRouter = Router.router(vertx);
                proxyRouter
                    .route("/*")
                    .handler (constant.ProxyHandlerImplDiy)
                proxyServer.requestHandler(proxyRouter)
                proxyServer.listen(port.toInt())
                println("Single Proxy server started on port $port")
            })

        }

    })


}
