package com.nice.czp.htmlsocket.push;

import java.io.IOException;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.HttpServerFilter;
import org.glassfish.grizzly.http.server.HttpServerProbe.Adapter;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nice.czp.htmlsocket.api.ISubscriber;
import com.nice.czp.htmlsocket.push.poll.LongPollServer;
import com.nice.czp.htmlsocket.push.ws.WSAddOn;
import com.nice.czp.htmlsocket.push.ws.impl.ProtocolManager;
import com.nice.czp.htmlsocket.push.ws.impl.RFC6455CodecImpl;

/**
 * 
 * @author coder_czp@126.com-2015年8月15日
 * 
 */
public class PullServer extends Adapter {

	private int port;
	private WSAddOn wsAddOn;
	private HttpServer server;
	private MessageCenter center;
	private PushContext wsContext;
	private ServerConfiguration sercfg;
	private static Logger log = LoggerFactory.getLogger(PullServer.class);

	public PullServer(PushSerConfig config) {

		this.wsContext = new PushContext(config);
		this.center = new MessageCenter();
		PreCheckListener check = new PreCheckListener(center);
		ProtocolManager instance = ProtocolManager.getInstance();
		instance.addCodec(new RFC6455CodecImpl());
		this.wsContext.setProtocolManager(instance);
		this.wsContext.addConnListener(check);
		this.wsContext.setMessageCenter(center);
		this.port = config.getPort();
		
		String base = config.getResourceBase();
		server = HttpServer.createSimpleServer(base, port);
		sercfg = server.getServerConfiguration();
		sercfg.getMonitoringConfig().getWebServerConfig().addProbes(this);
		doInit(center);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void onRequestCompleteEvent(HttpServerFilter filter,
			Connection conn, Response response) {
		try {
			Request request = response.getRequest();
			Object sub = request.getAttribute("sub");
			if (sub instanceof ISubscriber) {
				center.removeSubscriber((ISubscriber) sub);
				log.info("long poll client:[{}] close", conn.getPeerAddress());
			}
		} catch (Exception e) {
			log.error("remove long poll client error", e);
		}
	}

	private void doInit(MessageCenter mc) {
		LongPollServer poll = new LongPollServer(wsContext);
		AdminServlet admin = new AdminServlet(wsContext);
		this.wsAddOn = new WSAddOn(wsContext);
		this.addHandler(admin, "/admin/*");
		this.addHandler(poll, "/poll/*");
		for (NetworkListener ls : server.getListeners()) {
			ls.registerAddOn(wsAddOn);
		}
	}

	public void addHandler(HttpHandler handler, String url) {
		sercfg.addHttpHandler(handler, url);
	}

	public void start() throws IOException {
		server.start();
		log.info("push server running at:{}",port);
	}

	public PushContext getContext() {
		return wsContext;
	}

	public void sync() throws IOException {
		System.out.println("press any key to quit");
		System.in.read();
		server.shutdownNow();
	}

	public void stop() {
		server.shutdownNow();
		log.info("push server is shutdown");
	}
}
