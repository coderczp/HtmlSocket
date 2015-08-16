package com.nice.czp.htmlsocket.push.ws;

import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.SimpleWebSocket;
import org.glassfish.grizzly.websockets.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nice.czp.htmlsocket.api.IMessage;
import com.nice.czp.htmlsocket.api.IRemoteSubcriber;
import com.nice.czp.htmlsocket.api.PushError;
import com.nice.czp.htmlsocket.push.PushContext;

/**
 * websocket订阅者,为了减少内存占用,这个尽可能能少的添加属性
 * 
 * @author coder_czp@126.com-2015年8月13日
 * 
 */
public class WSSubcriber extends SimpleWebSocket implements IRemoteSubcriber {

    private Request request;
    private PushContext context;
    private static Logger log = LoggerFactory.getLogger(WSSubcriber.class);

    public WSSubcriber(ProtocolHandler handler, HttpRequestPacket pack, WebSocketListener[] listeners, PushContext ctx) {
        super(handler, listeners);
        context = ctx;
        request = Request.create();
        request.initialize(pack, handler.getFilterChainContext(), null);
    }

    @Override
    public void onClose(DataFrame frame) {
        super.onClose(frame);
        removeIfHavaAddToMessageCenter();
        log.info("ws client [{}]-closed", this);
    }

    @Override
    public void onConnect() {
        super.onConnect();
        PushError err;
        if ((err = context.beforeConn(this, request.getParameterMap())) != null) {
            close(WebsockCode.SERVER_ERROR, err.toString());
        } else {
            this.context.getMessageCenter().addSubscriber(this);
            log.info("ws client [{}]-connected", request.getRemoteAddr());
        }
    }

    @Override
    public void onMessage(byte[] data) {
        try {
            IMessage msg = context.getCodec().decode(data);
            context.getMessageCenter().send(msg);
        } catch (Exception e) {
            log.error("decode byte message err", e);
        }
    }

    @Override
    public void onMessage(String text) {
        try {
            IMessage msg = context.getCodec().decode(text);
            context.getMessageCenter().send(msg);
        } catch (Throwable e) {
            log.error("decode text message err", e);
        }
    }

    @Override
    public void onMessage(IMessage message) {
        try {
            send(context.getCodec().endcodeToText(message));
        } catch (Throwable e) {
            log.error("onMessage err", e);
        }
    }

    @Override
    public void doClose() {
        log.info("ws client [{}]-unsub", this);
        removeIfHavaAddToMessageCenter();
    }

    @Override
    public long getId() {
        return Long.valueOf(request.getParameter("id"));
    }

    @Override
    public String getTopic() {
        return request.getParameter("topic");
    }

    @Override
    public String toString() {
        return new StringBuffer("WSSubcriber[topic=").append(getTopic()).append(",id=").append(getId()).append("]")
                .toString();
    }

    /* topic表示Precheck检测失败未注册Subscriber */
    private void removeIfHavaAddToMessageCenter() {
        if (getTopic() != null)
            context.getMessageCenter().removeSubscriber(this);
    }
}
