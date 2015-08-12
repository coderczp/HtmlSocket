package com.nice.czp.htmlsocket.nws;

import java.io.IOException;

import org.glassfish.grizzly.ConnectionProbe;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.HttpServerMonitoringConfig;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.monitoring.MonitoringConfig;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketEngine;

/**
 * TODO Please Descrip This Class
 * 
 * @author coder_czp@126.com-2015年8月12日
 * 
 */
public class Server {

    private HttpServer server;
    private WebSocketEngine wsEngine;
    private ServerConfiguration sercfg;
    private ConnManager connManager;

    public Server(int port) {
        connManager = new ConnManager();
        wsEngine = WebSocketEngine.getEngine();
        server = HttpServer.createSimpleServer(".", port);
        sercfg = server.getServerConfiguration();

        HttpServerMonitoringConfig cfg = sercfg.getMonitoringConfig();
        MonitoringConfig<ConnectionProbe> ccfg = cfg.getConnectionConfig();
        ccfg.addProbes(connManager);

        WebSocketAddOn ws = new WebSocketAddOn();
        for (NetworkListener ls : server.getListeners()) {
            ls.registerAddOn(ws);
        }
    }

    public ConnManager getConnManager() {
        return connManager;
    }

    public void addHandler(HttpHandler handler, String url) {
        sercfg.addHttpHandler(handler, url);
    }

    public void addWebSocketHandler(String contextPath, String url, WebSocketApplication app) {
        wsEngine.register(contextPath, url, app);
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
