package interceptor

import io.vertx.core.Future
import io.vertx.httpproxy.ProxyContext
import io.vertx.httpproxy.ProxyInterceptor
import io.vertx.httpproxy.ProxyResponse

class OutInterceptor : ProxyInterceptor {
    override fun handleProxyRequest(context: ProxyContext?): Future<ProxyResponse> {
        var handleProxyRequest = super.handleProxyRequest(context)
        handleProxyRequest.onComplete {
            var result = it.result()
            if (result == null && context!=null){
                var x = context!!.request().headers().get(constant.BackEndIdentifyKey)
                constant.ProxyMap.remove(x)
                // 后端服务不通的干掉
            }
        }
        return handleProxyRequest
    }

    override fun handleProxyResponse(context: ProxyContext?): Future<Void> {
        return super.handleProxyResponse(context)
    }
}