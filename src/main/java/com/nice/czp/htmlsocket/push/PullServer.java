package com.nice.czp.htmlsocket.push;

import java.io.IOException;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.HttpServerMonitoringConfig;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.ServerConfiguration;

import com.nice.czp.htmlsocket.push.poll.LongPollServer;
import com.nice.czp.htmlsocket.push.ws.WSAddOn;
import com.nice.czp.htmlsocket.push.ws.impl.ProtocolManager;
import com.nice.czp.htmlsocket.push.ws.impl.RFC6455CodecImpl;

/**
 * 
 * @author coder_czp@126.com-2015年8月15日
 * 
 */
public class PullServer {

    private HttpServer server;
    private PushContext wsContext;
    private ServerConfiguration sercfg;

    public PullServer(PushSerConfig config) {

        MessageCenter mc = new MessageCenter();
        PreCheckListener check = new PreCheckListener(mc);
        this.wsContext = new PushContext(config);
        this.wsContext.setMessageCenter(mc);
        this.wsContext.addConnListener(check);

        int port = config.getPort();
        String base = config.getResourceBase();
        server = HttpServer.createSimpleServer(base, port);
        sercfg = server.getServerConfiguration();

        ProtocolManager.getInstance().addCodec(new RFC6455CodecImpl());
        doInit(mc);
    }

    private void doInit(MessageCenter mc) {
        HttpServerMonitoringConfig cfg = sercfg.getMonitoringConfig();
        cfg.getWebServerConfig().addProbes(mc);
        WSAddOn ws = new WSAddOn(wsContext);
        for (NetworkListener ls : server.getListeners()) {
            ls.registerAddOn(ws);
        }

        LongPollServer poll = new LongPollServer(wsContext);
        AdminServlet admin = new AdminServlet(wsContext);

        this.addHandler(admin, "/admin/*");
        this.addHandler(poll, "/poll/*");
    }

    public PushContext getContext() {
        return wsContext;
    }

    public void addHandler(HttpHandler handler, String url) {
        sercfg.addHttpHandler(handler, url);
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
