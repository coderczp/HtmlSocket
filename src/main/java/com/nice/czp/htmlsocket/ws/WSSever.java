package com.nice.czp.htmlsocket.ws;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author coder_czp@126.com-2015年8月8日
 */
public class WSSever extends WebSocketServlet {

    private static String mapbuf = "org.eclipse.jetty.servlet.Default.useFileMappedBuffer";
    private static String dirAllowed = "org.eclipse.jetty.servlet.Default.dirAllowed";
    private static Logger log = LoggerFactory.getLogger(WSSever.class);
    private static final long serialVersionUID = 1L;
    public static final String CTX = "_ctx_";
    private ServerConfig config;
    private WSContext wsContext;
    private Server server;

    public WSSever(ServerConfig config) {

        this.config = config;

        MessageCenter mc = new MessageCenter();
        PreCheckListener check = new PreCheckListener(mc);
        this.wsContext = new WSContext(config);
        this.wsContext.setMessageCenter(mc);
        this.wsContext.addConnListener(check);

        String useMapped = String.valueOf(config.isUseMappedbuf());
        String resourceBase = config.getResourceBase();
        WebAppContext context = new WebAppContext();
        context.setInitParameter(dirAllowed, "false");
        context.setInitParameter(mapbuf, useMapped);
        context.setResourceBase(resourceBase);
        context.setParentLoaderPriority(true);
        context.setAttribute(CTX, wsContext);
        context.setContextPath("/");

        context.addServlet(new ServletHolder(this), "/ws/*");
        context.addServlet(LongPollServlet.class, "/poll/*");
        context.addServlet(AdminServlet.class, "/admin/*");

        this.server = new Server(config.getPort());
        this.server.setHandler(context);

    }

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.getPolicy().setIdleTimeout(config.getConnTimeout());
        factory.register(WSSubscriber.class);
    }

    public void stop() throws Exception {
        this.server.stop();
        log.info("websocket server is shutdown");
    }

    public void start() throws Exception {
        this.server.start();
        log.info("wsserver is running:[{}]", config.getPort());
    }

    public WSContext getContext() {
        return wsContext;
    }
}
