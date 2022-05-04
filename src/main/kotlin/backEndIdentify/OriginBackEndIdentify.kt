package backEndIdentify

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.net.SocketAddress
import io.vertx.ext.web.RoutingContext

class OriginBackEndIdentify : IKeyFilter{

    override fun initFilter(function: (Vertx) -> Unit) {
        function(configVertx())
    }

    override fun keyFilter(ctx: RoutingContext): Future<SocketAddress> {
        var hosts = ctx.request().getHeader(constant.BackEndIdentifyHeaderName)
        if( hosts== null || hosts.isBlank()) {
            throw java.lang.Exception("host:port 为空")
        }
        var split = hosts.split(":")
        if (split.size != 2){
            throw java.lang.Exception("host:port 格式错误")
        }
        return Future.succeededFuture(
            SocketAddress.inetSocketAddress(split[1].toInt(), split[0])
        )
    }

}