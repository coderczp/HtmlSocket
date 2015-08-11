package com.nice.czp.htmlsocket.ws;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nice.czp.htmlsocket.api.IMessage;
import com.nice.czp.htmlsocket.api.ISubscriber;
import com.nice.czp.htmlsocket.api.WSError;

/**
 * Websocket订阅者,该类是线程安全的,每次链接会创建新的对象
 * 
 * @author coder_czp@126.com-2015年8月8日
 * 
 */
public class WSSubscriber extends WebSocketAdapter implements ISubscriber {

    private static Logger log = LoggerFactory.getLogger(WSSubscriber.class);
    private Session session;
    private String connInfo;
    private String clientId;
    private WSContext ctx;
    private String topic;

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        try {
            log.debug("recv byte message:{}", payload);
            IMessage msg = ctx.getCodec().decode(payload);
            ctx.getMessageCenter().send(msg);
        } catch (Exception e) {
            log.error("ws client onbinary error", e);
        }
    }

    @Override
    public void onWebSocketText(String message) {
        try {
            log.debug("recv txt message:{}", message);
            IMessage msg = ctx.getCodec().decode(message);
            ctx.getMessageCenter().send(msg);
        } catch (Exception e) {
            log.error("ws client ontext error", e);
        }
    }

    @Override
    public void onWebSocketConnect(Session sess) {

        this.connInfo = sess.getRemoteAddress().toString();
        log.info("ws client [{}]-connected", connInfo);

        UpgradeRequest req = sess.getUpgradeRequest();
        if (!(req.getSession() instanceof HttpSession)) {
            throw new RuntimeException("req.getSession() is't HttpSession");
        }

        HttpSession session = (HttpSession) req.getSession();
        ServletContext sCtx = session.getServletContext();
        ctx = (WSContext) sCtx.getAttribute(WSSever.CTX);

        Map<String, String[]> param = req.getParameterMap();
        WSError err = ctx.beforeConn(this, param);
        if (err != null) {
            sendErrorInfo(sess, err.toString());
            return;
        }

        String[] strings = param.get("topic");
        String[] idString = param.get("id");
        this.clientId = idString[0];
        this.topic = strings[0];
        this.session = sess;

        ctx.getMessageCenter().addSubscriber(this);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        log.info("ws client [{}]-closed", connInfo);
        /* topic表示checkParam检测失败未注册Subscriber */
        if (topic != null)
            ctx.getMessageCenter().removeSubscriber(this);
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public String getId() {
        return clientId;
    }

    @Override
    public String toString() {
        return "WSSubscriber [topic=" + topic + ",client=" + connInfo + "]";
    }

    @Override
    public void onMessage(IMessage message) {
        try {
            String json = ctx.getCodec().endcodeToText(message);
            session.getRemote().sendString(json);
        } catch (IOException e) {
            log.error("send ws message err", e);
        }
    }

    private void sendErrorInfo(Session sess, String info) {
        try {
            int serverError = StatusCode.SERVER_ERROR;
            sess.close(serverError, info);
            sess.disconnect();
        } catch (IOException e) {
            log.error("id or topic is null", e);
        }
    }
}
