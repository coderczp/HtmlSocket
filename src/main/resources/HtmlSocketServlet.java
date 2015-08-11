package com.nice.czp.htmlsocket.demo;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.nice.czp.htmlsocket.api.ICodec;
import com.nice.czp.htmlsocket.api.IMessage;
import com.nice.czp.htmlsocket.api.ISubscriber;
import com.nice.czp.htmlsocket.ws.MessageCenter;
import com.nice.czp.htmlsocket.ws.ServerConfig;
import com.nice.czp.htmlsocket.ws.WSSever;

/**
 * HtmlSocket sever 启动类
 */
public class HtmlSocketServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private MessageCenter messageCenter;
    private WSSever wsServer;

    @Override
    public void destroy() {
        try {
            if (wsServer != null)
                wsServer.stop();
        } catch (Exception e) {
            System.out.println("fail to stop htmlsocket server," + e);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            ICodec codec = null;// Instead of your code.
            String portStr = config.getInitParameter("port");
            int port = Integer.valueOf(portStr);
            ServerConfig cfg = new ServerConfig(port, codec);
            wsServer = new WSSever(cfg);
            messageCenter = wsServer.getContext().getMessageCenter();
            wsServer.start();
        } catch (Exception e) {
            System.out.println("fail to start htmlsocket server," + e);
        }
    }

    public void sendMessage() {
        IMessage msg = null;// Instead of your code.
        messageCenter.sendTo("demoId", msg);
        messageCenter.broadcastGroup("demo", msg);
        messageCenter.broadcastAll(msg);
    }

    public void addMySubscriber() {
        messageCenter.addSubscriber(new ISubscriber() {

            @Override
            public void onMessage(IMessage message) {
                System.out.println(message);
            }

            @Override
            public String getTopic() {
                return "demo";
            }

            @Override
            public String getId() {
                return "demoid";
            }
        });
    }

}
