package backEndIdentify

import io.vertx.core.*
import io.vertx.core.json.JsonObject
import io.vertx.core.net.SocketAddress
import io.vertx.core.spi.cluster.ClusterManager
import io.vertx.core.spi.cluster.NodeInfo
import io.vertx.core.spi.cluster.NodeListener
import io.vertx.ext.web.RoutingContext
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager
import org.slf4j.LoggerFactory


class ZookeeperBackEndIdentify : IKeyFilter {

    lateinit var identifyHostMap : MutableMap<String,String>

    override fun initFilter(function: (Vertx) -> Unit) {
        identifyHostMap = mutableMapOf()
        var jsonObject = constant.Config.getJsonObject("zookeeperConfig")
        val mgr: ClusterManager = ZookeeperClusterManager(jsonObject)
        val options = VertxOptions().setClusterManager(mgr)
        Vertx.clusteredVertx(options) { res: AsyncResult<Vertx?> ->
            if (res.succeeded()) {
                periodicRefreshNodeInfo(mgr,constant.Config)
                var vertx = res.result()!!
                function(vertx)
            } else {
                throw Exception("zookeeper is connect error!")
            }
        }
    }

    private fun periodicRefreshNodeInfo(mgr: ClusterManager, config: JsonObject) {
        // 实时监听
        var nodeListener = ProxyNodeListener(mgr,this)
        mgr.nodeListener(nodeListener)
        // 定时刷新
        constant.ProxyHandlerImplDiy.vertx.setPeriodic(1000) {
            nodeListener.refreshNodes(mgr)
        }
    }

    override fun keyFilter(ctx: RoutingContext): Future<SocketAddress> {
        var identify = ctx.request().getHeader(constant.BackEndIdentifyHeaderName)
        var get: String? = identifyHostMap[identify] ?: throw Exception("node can not find!")
        var split = get!!.split(":")
        return  Future.succeededFuture(
            SocketAddress.inetSocketAddress(split[1].toInt(), split[0])
        )
    }

}

class ProxyNodeListener : NodeListener {
    private val log = LoggerFactory.getLogger(this::class.java)

    private var mgr: ClusterManager
    private var zookeeperBackEndIdentify: ZookeeperBackEndIdentify
    constructor(mgr: ClusterManager, zookeeperBackEndIdentify: ZookeeperBackEndIdentify){
        this.mgr = mgr
        this.zookeeperBackEndIdentify = zookeeperBackEndIdentify
    }
    fun refreshNodes(mgr: ClusterManager) {
        synchronized(this){
            val temp = mutableMapOf<String, String>()
            mgr.nodes.forEach {
                temp[it] = it
            }
            constant.ProxyMap.forEach{
                if (!temp.contains(it.key) ){
                    constant.ProxyMap.remove(it.key)
                }
            }
            mgr.nodes.forEach { res ->
                val promise = Promise.promise<NodeInfo>()
                mgr.getNodeInfo(res, promise)
                promise.future().onComplete {
                    if (it.succeeded()) {
                        var result = it.result()
                        var backEndIdentifyValue = result.metadata().getString(constant.BackEndIdentifyKey)
                        this.zookeeperBackEndIdentify.identifyHostMap[res] = backEndIdentifyValue
                    } else {
                        log.error("zk can not get node info $res")
                    }
                }
            }
        }
    }

    override fun nodeAdded(nodeID: String?) {
       refreshNodes(mgr)
    }

    override fun nodeLeft(nodeID: String?) {
        refreshNodes(mgr)
    }

}
