package com.nice.czp.htmlsocket.push.ws;


import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketListener;

import com.nice.czp.htmlsocket.push.PushContext;

/**
 * 
 * @author coder_czp@126.com-2015年8月13日
 * 
 */
public class WebSocketServer extends WebSocketApplication {

    private PushContext context;

    public WebSocketServer(PushContext context) {
        this.context = context;
    }

    @Override
    public WebSocket createSocket(ProtocolHandler handler, HttpRequestPacket requestPacket,
            WebSocketListener... listeners) {
        return new WSSubcriber(handler, requestPacket, listeners,context);
    }

}
