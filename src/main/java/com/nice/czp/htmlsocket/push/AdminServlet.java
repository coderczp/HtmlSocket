package com.nice.czp.htmlsocket.push;

import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import com.nice.czp.htmlsocket.api.ISubscriber;

/**
 * 查看管理websocket和long poll链接
 * 
 * @author coder_czp@126.com-2015年8月8日
 * 
 */
public class AdminServlet extends HttpHandler {

    private PushContext ctx;

    public AdminServlet(PushContext context) {
        this.ctx = context;
    }

    @Override
    public void service(Request request, Response resp) throws Exception {
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("text/html");
        Writer writer = resp.getWriter();
        writer.write("<html><head><title>HtmlSocket</title></head><body>");
        writer.write("<h3>HtmlSocket manger</h3><hr><ul>");

        int count = 0;
        MessageCenter sever = ctx.getMessageCenter();
        Collection<ThreadMap> allsub = sever.getSubscribers();
        for (ThreadMap subs : allsub) {
            if (count < 100) {
                for (Map.Entry<Long, ISubscriber> item : subs.entrySet()) {
                    ISubscriber value = item.getValue();
                    String topic = value.getTopic();
                    long id = value.getId();
                    writer.write(String.format("<li>ID:[%s],Topic:[%s]-[%s]</li>", id, topic, value));
                }
            }
            count += subs.size();
        }
        writer.write("</ul><hr>");
        writer.write(String.format("订阅者[%s]", count));
        writer.write("</body></html>");
    }

}
