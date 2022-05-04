package handler

import backEndIdentify.*
import interceptor.OutInterceptor
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.httpproxy.HttpProxy
import io.vertx.httpproxy.ProxyOptions

class ProxyHandlerImplDiy : Handler<RoutingContext> {

    lateinit var vertx: Vertx
    lateinit var keyFilter: IKeyFilter
    lateinit var proxyClient: HttpClient

    fun initProxyHandler(function: (Vertx) -> Unit) {
        var backEndIdentify = constant.Config.getString("backEndIdentifyMode", "origin")
        if (backEndIdentify == "origin"){
            keyFilter = OriginBackEndIdentify()
        }
        if (backEndIdentify == "jwt"){
            keyFilter = JwtBackEndIdentify()
        }
        if (backEndIdentify == "redis"){
            keyFilter = RedisBackEndIdentify()
        }
        if (backEndIdentify == "zookeeper"){
            keyFilter = ZookeeperBackEndIdentify()
        }
        keyFilter.initFilter(fun (vertx:Vertx){
            var httpClientOptions = HttpClientOptions(constant.Config.getJsonObject("httpClientConfig"))
            proxyClient = vertx.createHttpClient(httpClientOptions)
            this.vertx = vertx
            function(vertx)
        })
    }

    override fun handle(ctx: RoutingContext) {
        try {
            var httpProxy = buildHttpProxy(ctx)
            if (constant.ForwardedHost) {
                ctx.request().headers().add("x-forwarded-host", ctx.request().host())
            }
            httpProxy!!.handle(ctx.request())
        }catch (e : Exception){
            e.printStackTrace()
            ctx.fail(500)
        }
    }

    private fun buildHttpProxy(ctx: RoutingContext): HttpProxy? {
        var default = ctx.request().getHeader(constant.BackEndIdentifyKey)
        if (default == null || default.isEmpty()){
            throw Exception("BackEndIdentifyKey value can not find!")
        }
        var httpProxy = constant.ProxyMap[default]
        if (httpProxy == null){
            httpProxy = HttpProxy.reverseProxy(ProxyOptions(constant.Config.getJsonObject("proxyOptionsConfig")),proxyClient).originSelector { address: HttpServerRequest? ->
                 keyFilter.keyFilter(ctx)
            }
            httpProxy.addInterceptor(OutInterceptor())
            constant.ProxyMap[default] = httpProxy
        }
        return httpProxy
    }

}