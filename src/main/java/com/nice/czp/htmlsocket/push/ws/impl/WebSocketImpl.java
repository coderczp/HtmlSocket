package com.nice.czp.htmlsocket.push.ws.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nice.czp.htmlsocket.push.ws.itf.AbstractWebscoket;
import com.nice.czp.htmlsocket.push.ws.itf.IWSCodec;
import com.nice.czp.htmlsocket.push.ws.util.FrameUtil;

/**
 * http://jinnianshilongnian.iteye.com/blog/1899876
 * 
 * @author coder_czp@126.com-2015年8月17日
 * 
 */
public class WebSocketImpl extends AbstractWebscoket {

    private IWSCodec codec;
    private String remoteAddress;
    private static Logger log = LoggerFactory.getLogger(WebSocketImpl.class);

    public WebSocketImpl(FilterChainContext ctx, IWSCodec codec) {
        super(ctx.getConnection());
        this.codec = codec;
    }

    @Override
    public String getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public String toString() {
        return remoteAddress;
    }

    @Override
    public IWSCodec getCodec() {
        return codec;
    }

    @Override
    public Map<String, String> getHandShakePack(Map<String, String> requestParams) {
        Map<String, String> handShake = new HashMap<String, String>();
        remoteAddress = requestParams.get(REMOTE_ADDRESS);
        String cliKey = requestParams.get(SEC_WS_KEY_HEADER);

        handShake.put("Sec-WebSocket-Accept", FrameUtil.generateSecKey(cliKey));
        handShake.put("Connection", "Upgrade");
        handShake.put("Upgrade", "websocket");
        handShake.put("Server", SERVER_NAME);

        initQueryParams(requestParams.get(QUERY_STRING));
        fireConnected();

        return handShake;
    }

    /* queryString :id=xx&topic=xx .. */
    private void initQueryParams(String queryString) {
        StringTokenizer tk = new StringTokenizer(queryString, "&");
        int index;
        String key;
        String[] value;
        String keyValue;
        while (tk.hasMoreTokens()) {
            keyValue = tk.nextToken();
            index = keyValue.indexOf("=");
            if (index > 0) {
                key = keyValue.substring(0, index);
                value = keyValue.substring(index + 1).split(",");
                params.put(key, value);
            } else {
                log.error("error param {}", keyValue);
            }
        }
    }
}
