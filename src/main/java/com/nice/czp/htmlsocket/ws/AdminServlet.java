package com.nice.czp.htmlsocket.ws;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nice.czp.htmlsocket.api.ISubscriber;

/**
 * 查看管理websocket和long poll链接
 * 
 * @author coder_czp@126.com-2015年8月8日
 * 
 */
public class AdminServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("utf-8");
        ServletContext sCtx = req.getServletContext();
        Object obj = sCtx.getAttribute(WSSever.CTX);
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("text/html");
        WSContext ctx = (WSContext) obj;
        PrintWriter writer = resp.getWriter();
        writer.print("<html><head><title>HtmlSocket</title></head><body>");
        writer.print("<h3>HtmlSocket 管理中心</h3><hr><ul>");

        MessageCenter sever = ctx.getMessageCenter();
        Map<String, ISubscriber> subs = sever.getSubscribers();
        for (Map.Entry<String, ISubscriber> item : subs.entrySet()) {
            ISubscriber value = item.getValue();
            String topic = value.getTopic();
            String id = value.getId();
            writer.print(String.format("<li>ID:[%s],Topic:[%s]-[%s]</li>", id, topic, value));
        }
        writer.print("</ul><hr>");
        writer.print(String.format("订阅者[%s]", subs.size()));
        writer.print("</body></html>");
        writer.close();
    }
}
