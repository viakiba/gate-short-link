package backEndIdentify

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.net.SocketAddress
import io.vertx.ext.auth.impl.Codec.base64Decode
import io.vertx.ext.web.RoutingContext

class JwtBackEndIdentify : IKeyFilter {

    override fun initFilter(function: (Vertx) -> Unit){
        function(configVertx())
    }

    override fun keyFilter(ctx: RoutingContext): Future<SocketAddress> {
        var token = ctx.request().getHeader(constant.BackEndIdentifyHeaderName)
        if (token == null || token.isBlank()) {
            throw java.lang.Exception("jwt token can not find!")
        }
        var split = token.split(",")
        if (split.size != 3) {
            throw java.lang.Exception("jwt token value is error!")
        }
        var s = split[1]
        var base64Decode = base64Decode(s)
        var json = JsonObject(String(base64Decode))
        var ipPortString = json.getString("origin")
        if (ipPortString == null || ipPortString.isEmpty()) {
            throw java.lang.Exception("jwt token value origin can not find!")
        }
        var split1 = ipPortString.split(":")
        return  Future.succeededFuture(
            SocketAddress.inetSocketAddress(split1[1].toInt(), split[0])
        )
    }
}