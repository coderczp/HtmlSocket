package org.eclipse.jetty.server;

import java.net.InetSocketAddress;

import javax.servlet.AsyncContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletResponse;

import org.eclipse.jetty.io.EndPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nice.czp.htmlsocket.api.ICodec;
import com.nice.czp.htmlsocket.api.IMessage;
import com.nice.czp.htmlsocket.api.ISubscriber;
import com.nice.czp.htmlsocket.ws.LongPollServlet;
import com.nice.czp.htmlsocket.ws.MessageCenter;
import com.nice.czp.htmlsocket.ws.WSContext;
import com.nice.czp.htmlsocket.ws.WSSever;

/**
 * 该类处理long-polling的连接,由于Jetty的getChannel()是protect方法,
 * 所以PollSubscriber的package必须和Request相同
 * 
 * @author coder_czp@126.com-2015年8月8日
 * 
 */
public class PollSubscriber implements ISubscriber {

    private String topic;
    private ICodec codec;
    private String clientId;
    private Request request;
    private EndPoint endPoint;
    private AsyncContext asynCtx;
    private MessageCenter mCenter;
    private InetSocketAddress client;

    private static Logger log = LoggerFactory.getLogger(PollSubscriber.class);

    public PollSubscriber(Request request, AsyncContext asynCtx) {
        HttpChannel<?> httpChannel = request.getHttpChannel();
        ServletContext sCtx = request.getServletContext();
        this.endPoint = httpChannel.getEndPoint();

        this.clientId = request.getParameter("id");
        this.topic = request.getParameter("topic");
        this.client = endPoint.getRemoteAddress();
        this.request = request;
        this.asynCtx = asynCtx;

        WSContext ctx = (WSContext) sCtx.getAttribute(WSSever.CTX);
        mCenter = ctx.getMessageCenter();
        codec = ctx.getCodec();
        
        log.info("long poll client-[{}][{}]-connected", client, getId());
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public int hashCode() {
        return request.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PollSubscriber) {
            PollSubscriber sub = (PollSubscriber) obj;
            return sub.request.equals(request);
        }
        return super.equals(obj);
    }

    @Override
    public void onMessage(IMessage message) {
        try {
            mCenter.removeSubscriber(this);
            String json = codec.endcodeToText(message);
            ServletResponse resp = request.getServletResponse();
            LongPollServlet.writeToClient(request, resp, json);
            processAsynctx();
        } catch (Exception e) {
            log.error("send ws message err", e);
        }
    }

    public void onClose() {
        mCenter.removeSubscriber(this);
        processAsynctx();
        log.info("long poll client-[{}][{}]-closed", client, getId());
    }

    public boolean isClosed() {
        return !endPoint.isOpen();
    }

    @Override
    public String getId() {
        return clientId;
    }

    @Override
    public String toString() {
        return "PollSubscriber [topic=" + topic + ", client=" + client + "]";
    }

    private void processAsynctx() {
        switch (request.getHttpChannelState().getState()) {
        case IDLE:
        case ASYNCWAIT:
        case ASYNCSTARTED:
            asynCtx.complete();
            break;
        default:
            break;
        }
    }


}
