package com.nice.czp.htmlsocket.ws;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.PollSubscriber;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nice.czp.htmlsocket.api.WSError;

/**
 * 长轮询接入类
 * 
 * @author coder_czp@126.com-2015年8月8日
 * 
 */
public class LongPollServlet extends HttpServlet implements AsyncListener {

    private static Logger log = LoggerFactory.getLogger(LongPollServlet.class);
    private static final long serialVersionUID = 1L;
    private WSContext ctx;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse rep) throws ServletException, IOException {
        try {
            ServletContext sCtx = req.getServletContext();
            ctx = (WSContext) sCtx.getAttribute(WSSever.CTX);
            setResponseHeaders(rep);
            if ("false".equals(req.getParameter("hold"))) {
                processShortConnect(req, rep);
            } else {
                processLongConnect(req, rep);
            }
        } catch (Exception e) {
            log.error("long poll client err", e);
        }
    }

    private void processLongConnect(HttpServletRequest req, HttpServletResponse rep) throws IOException {
        AsyncContext asyCtx = req.startAsync();
        PollSubscriber sub = new PollSubscriber((Request) req, asyCtx);
        WSError error = ctx.beforeConn(sub, req.getParameterMap());
        if (error != null) {
            PrintWriter writer = rep.getWriter();
            alertInfo(writer, error.getDesc());
            asyCtx.complete();
            return;
        }
        ctx.getMessageCenter().addSubscriber(sub);
        asyCtx.getRequest().setAttribute("sub", sub);
        //asyCtx.setTimeout(1000 * 60);
        asyCtx.addListener(this);
    }

    private void processShortConnect(HttpServletRequest req, HttpServletResponse rep) throws IOException {
        log.info("recv short conn [{}]", getHttpRemoteAddr(req));
        MessageCenter center = ctx.getMessageCenter();
        String msgStr = req.getParameter("data");
        center.send(ctx.getCodec().decode(msgStr));
        rep.getWriter().close();
    }

    private void setResponseHeaders(HttpServletResponse rep) {
        String corsControl = ctx.getConfig().getCorsControl();
        rep.addHeader("Access-Control-Allow-Origin", corsControl);
        rep.setHeader("Cache-Control", "no-cache");
        rep.setHeader("Pragma", "no-cache");
        rep.setCharacterEncoding("utf-8");
    }

    @Override
    public void onComplete(AsyncEvent paramAsyncEvent) throws IOException {
        // log.debug(paramAsyncEvent.toString());
    }

    @Override
    public void onStartAsync(AsyncEvent paramAsyncEvent) throws IOException {
        // log.debug(paramAsyncEvent.toString());
    }

    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
        ServletRequest request = event.getAsyncContext().getRequest();
        String addr = getHttpRemoteAddr(request);
        log.info("long poll client [{}] timeout", addr);
        onError(event);
    }

    @Override
    public void onError(AsyncEvent event) throws IOException {
        AsyncContext actx = event.getAsyncContext();
        ServletRequest request = actx.getRequest();
        String addr = getHttpRemoteAddr(request);
        log.info("long poll client [{}:{}] error", addr);

        ServletResponse response = actx.getResponse();
        writeToClient(request, response, WSError.TIMEOUT.toString());

        Object obj = request.getAttribute("sub");
        ((PollSubscriber) obj).onClose();

    }

    public static void writeToClient(ServletRequest req, ServletResponse response, String data) throws IOException {
        String callBack = req.getParameter("callback");
        if (callBack != null) {
            writeAndflush(response.getWriter(), String.format("%s(%s)", callBack, data));
        } else {
            response.setContentType("application/json");
            writeAndflush(response.getWriter(), data);
        }
    }

    private static String getHttpRemoteAddr(ServletRequest req) {
        return String.format("%s:%s", req.getRemoteAddr(), req.getRemotePort());
    }

    private static void writeAndflush(PrintWriter writer, String data) {
        writer.print(data);
        writer.flush();
    }

    private static void alertInfo(PrintWriter writer, String info) {
        String message = String.format("alert('%s')", info);
        writeAndflush(writer, message);
    }
}
