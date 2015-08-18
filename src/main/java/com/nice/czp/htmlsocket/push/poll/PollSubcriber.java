package com.nice.czp.htmlsocket.push.poll;

import java.io.Writer;

import org.glassfish.grizzly.http.server.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nice.czp.htmlsocket.api.IMessage;
import com.nice.czp.htmlsocket.api.IRemoteSubcriber;
import com.nice.czp.htmlsocket.push.PushContext;

/**
 * comet订阅者
 * 
 * @author coder_czp@126.com-2015年8月13日
 * 
 */
public class PollSubcriber implements IRemoteSubcriber {

    private Response response;
    private PushContext context;
    private static Logger log = LoggerFactory.getLogger(PollSubcriber.class);

    public PollSubcriber(Response resp, PushContext ctx) {
        context = ctx;
        response = resp;
    }

    @Override
    public long getId() {
        return Long.valueOf(response.getRequest().getParameter("id"));
    }

    @Override
    public String getTopic() {
        return response.getRequest().getParameter("topic");
    }

    @Override
    public void onMessage(IMessage message) {
        try {
            log.debug("recv message:[{}]", message);
            String json = context.getCodec().endcodeToText(message);
            String callBack = response.getRequest().getParameter("callback");
            Writer writer = response.getWriter();
            if (callBack != null) {
                json = String.format("%s(%s)", callBack, json);
            }
            writer.write(json);
            writer.flush();
            doUnSubscrib();
        } catch (Exception e) {
            log.error("onMessage error", e);
        }
    }

    @Override
    public void doUnSubscrib() {
        if (response.isSuspended()) {
            response.resume();
        }
    }

}
