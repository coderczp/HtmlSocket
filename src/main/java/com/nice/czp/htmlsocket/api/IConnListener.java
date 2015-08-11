package com.nice.czp.htmlsocket.api;

import java.util.Map;

/**
 * 上下文件监听器,主要针对连接进行连接
 * 
 * @author coder_czp@126.com-2015年8月8日
 * 
 */
public interface IConnListener {

    /**
     * 对将要建立连接的订阅者进行拦截,比如检测
     * 是否登录等,返回非空将取消subscriber
     * 的的连接,客户端将收到WSError
     * 
     * @param subscriber
     *            将要建立长连接的订阅者
     * @param params
     *            建立连接的参数
     * @return WSError|null
     * 
     */
    WSError beforeConnect(ISubscriber subscriber, Map<String, String[]> params);

    /**
     * subscriber断开连接前进行相关处理
     * 
     * @param subscriber
     *            将要断连的订阅者
     */
    void beforeClose(ISubscriber subscriber);

}
