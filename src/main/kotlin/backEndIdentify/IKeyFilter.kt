package backEndIdentify

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.json.JsonObject
import io.vertx.core.net.SocketAddress
import io.vertx.ext.web.RoutingContext

interface IKeyFilter {
    fun initFilter(function: (Vertx) -> Unit)

    fun keyFilter(ctx: RoutingContext): Future<SocketAddress>

    fun configVertx(): Vertx {
        var vertxOption = constant.Config.getJsonObject("vertxConfig")
        var vertxOptions = VertxOptions(vertxOption)
        return Vertx.vertx(vertxOptions)
    }
}