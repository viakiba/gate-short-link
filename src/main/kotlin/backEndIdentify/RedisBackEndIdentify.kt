package backEndIdentify

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.net.SocketAddress
import io.vertx.ext.web.RoutingContext
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisAPI
import io.vertx.redis.client.RedisConnection
import io.vertx.redis.client.RedisOptions


class RedisBackEndIdentify : IKeyFilter {

    lateinit var redis: RedisAPI

    override fun initFilter(function: (Vertx) -> Unit) {
        var vertx = configVertx()

        var jsonObject = constant.Config.getJsonObject("redisConfig")
        var redisOptions = RedisOptions(jsonObject)

        Redis.createClient(vertx,redisOptions)
            .connect()
            .onSuccess {
                    conn: RedisConnection? ->
                redis = RedisAPI.api(conn)
            }
        function(vertx)
    }

    override fun keyFilter(ctx: RoutingContext): Future<SocketAddress> {
        var backEndIdentify = ctx.request().getHeader(constant.BackEndIdentifyHeaderName)
        var z = redis.get(backEndIdentify)
            .onSuccess{
                println(it)
            }
            .compose {
                var split = it.toString().split(":")
                Future.succeededFuture(
                    SocketAddress.inetSocketAddress(split[1].toInt(), split[0])
                ) }
        return z
    }


}