package com.nice.czp.htmlsocket.push.ws;

import java.util.Map;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.http.HttpHeader;

public interface WebsocketImpl {

	void doHandShake(HttpHeader header, FilterChainContext ctx);

	void send(byte[] data);

	void cose();
	
	String getVersion();
	
	Map<String,Object> getExt();

	DataFrame parseFrame(Buffer buffer);

	void onMessage(DataFrame result);
}
