package constant

import handler.ProxyHandlerImplDiy
import io.vertx.core.json.JsonObject
import io.vertx.httpproxy.HttpProxy

var BackEndIdentifyKey: String = "backEndIdentifyKey"
var BackEndIdentifyHeaderName: String = "backEndIdentifyHeaderName"
var ForwardedHost : Boolean = true
var Config: JsonObject = JsonObject()
var ProxyMap : MutableMap<String, HttpProxy> = mutableMapOf()
var ProxyHandlerImplDiy: ProxyHandlerImplDiy = ProxyHandlerImplDiy()
