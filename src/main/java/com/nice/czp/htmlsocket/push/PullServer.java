package com.nice.czp.htmlsocket.push;

import java.io.IOException;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.HttpServerMonitoringConfig;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketEngine;

import com.nice.czp.htmlsocket.push.poll.LongPollServer;
import com.nice.czp.htmlsocket.push.ws.WSAddOn;
import com.nice.czp.htmlsocket.push.ws.WebSocketServer;

/**
 * 
 * @author coder_czp@126.com-2015年8月15日
 * 
 */
public class PullServer {

    private HttpServer server;
    private PushContext wsContext;
    private WebSocketEngine wsEngine;
    private ServerConfiguration sercfg;

    public PullServer(PushSerConfig config) {

        MessageCenter mc = new MessageCenter();
        PreCheckListener check = new PreCheckListener(mc);
        this.wsContext = new PushContext(config);
        this.wsContext.setMessageCenter(mc);
        this.wsContext.addConnListener(check);

        int port = config.getPort();
        String base = config.getResourceBase();
        wsEngine = WebSocketEngine.getEngine();
        server = HttpServer.createSimpleServer(base, port);
        sercfg = server.getServerConfiguration();

        doInit(mc);
    }

    private void doInit(MessageCenter mc) {
        HttpServerMonitoringConfig cfg = sercfg.getMonitoringConfig();
        cfg.getWebServerConfig().addProbes(mc);
//        WebSocketAddOn ws = new WebSocketAddOn();
        WSAddOn ws = new WSAddOn();
        for (NetworkListener ls : server.getListeners()) {
            ls.registerAddOn(ws);
        }

        WebSocketServer wser = new WebSocketServer(wsContext);
        LongPollServer poll = new LongPollServer(wsContext);
        AdminServlet admin = new AdminServlet(wsContext);

        this.addWebSocketHandler("/ws/*", wser);
        this.addHandler(admin, "/admin/*");
        this.addHandler(poll, "/poll/*");
    }

    public PushContext getContext() {
        return wsContext;
    }

    public void addHandler(HttpHandler handler, String url) {
        sercfg.addHttpHandler(handler, url);
    }

    public void addWebSocketHandler(String url, WebSocketApplication app) {
        wsEngine.register("/", url, app);
    }

    public void start() throws IOException {
        server.start();
    }

    public void sync() throws IOException {
        System.out.println("press any key to quit");
        System.in.read();
        server.shutdownNow();
    }
}
