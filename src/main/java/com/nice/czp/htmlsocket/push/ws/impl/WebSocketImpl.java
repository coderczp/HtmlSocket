package com.nice.czp.htmlsocket.push.ws.impl;

import java.security.MessageDigest;
import java.util.StringTokenizer;

import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpHeader;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.HttpResponsePacket;
import org.glassfish.grizzly.http.Protocol;
import org.glassfish.grizzly.http.util.Base64Utils;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.grizzly.utils.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nice.czp.htmlsocket.push.ws.itf.AbstractWebscoket;
import com.nice.czp.htmlsocket.push.ws.itf.IWSCodec;
import com.nice.czp.htmlsocket.push.ws.itf.IWebsocket;

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

    private void initQueryParams(FilterChainContext ctx) {
        HttpContent message = ctx.getMessage();
        HttpHeader header = message.getHttpHeader();
        HttpRequestPacket pack = (HttpRequestPacket) header;
        String queryString = pack.getQueryString();
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

    @Override
    public void doHandShake(HttpHeader header, FilterChainContext ctx) {
        try {
            remoteAddress = ctx.getConnection().getPeerAddress().toString();
            String cliKey = header.getHeader("Sec-WebSocket-Key");
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            String serKey = cliKey + IWebsocket.SERVER_KEY_HASH;
            md.update(serKey.getBytes(Charsets.ASCII_CHARSET));
            String key = base64Encode(md.digest());

            HttpRequestPacket pack = (HttpRequestPacket) header;
            HttpResponsePacket rep = pack.getResponse();
            rep.setStatus(HttpStatus.SWITCHING_PROTOCOLS_101);
            rep.setHeader("Access-Control-Allow-Credentials", "true");
            rep.setHeader("Server", "htmlsocket server");
            rep.setHeader("Sec-WebSocket-Accept", key);
            rep.setHeader("Connection", "Upgrade");
            rep.setHeader("Upgrade", "websocket");
            rep.setProtocol(Protocol.HTTP_1_1);
            ctx.write(HttpContent.builder(rep).build());
            initQueryParams(ctx);

            fireConnected();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String base64Encode(byte[] input) {
        return Base64Utils.encodeToString(input, false);
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
}
