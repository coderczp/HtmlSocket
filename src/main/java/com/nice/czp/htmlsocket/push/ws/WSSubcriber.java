package com.nice.czp.htmlsocket.push.ws;

import java.nio.charset.Charset;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nice.czp.htmlsocket.api.IMessage;
import com.nice.czp.htmlsocket.api.IRemoteSubcriber;
import com.nice.czp.htmlsocket.push.PushContext;
import com.nice.czp.htmlsocket.push.ws.itf.IWebsocket;
import com.nice.czp.htmlsocket.push.ws.itf.WSListener;

/**
 * 
 * @author coder_czp@126.com-2015年8月17日
 * 
 */
public class WSSubcriber implements WSListener, IRemoteSubcriber {

    private static Logger log = LoggerFactory.getLogger(WSSubcriber.class);
    private Map<String, String[]> params;
    private PushContext context;
    private IWebsocket sock;

    public WSSubcriber(PushContext pushCtx) {
        this.context = pushCtx;
    }

    @Override
    public long getId() {
        return Long.valueOf(params.get("id")[0]);
    }

    @Override
    public String getTopic() {
        return params.get("topic")[0];
    }

    @Override
    public void doUnSubscrib() {
        log.info("ws client [{}]-unsub", sock);
        sock.close(IWebsocket.SERVER_ERROR, "Server remove subcriber");
    }

    @Override
    public void onWSMessage(byte[] data, boolean isText) {
        if (isText) {
            String text = new String(data, Charset.forName("UTF-8"));
            IMessage msg = context.getCodec().decode(text);
            context.getMessageCenter().send(msg);
        } else {
            IMessage msg = context.getCodec().decode(data);
            context.getMessageCenter().send(msg);
        }
    }

    @Override
    public void onMessage(IMessage message) {
        sock.send(context.getCodec().endcodeToText(message));
    }

    @Override
    public void onConnected(IWebsocket websocket, Map<String, String[]> params) {
        this.sock = websocket;
        this.params = params;
        context.getMessageCenter().addSubscriber(this);
        log.info("ws client [{}]-connected", websocket);
    }

    @Override
    public void onClosed(IWebsocket websocket, int code, String info) {
        log.info("ws client [{}]-closed,info:{}", websocket, info);
        context.getMessageCenter().removeSubscriber(getId(), false);
    }

}
