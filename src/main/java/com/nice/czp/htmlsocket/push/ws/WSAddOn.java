package com.nice.czp.htmlsocket.push.ws;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpHeader;
import org.glassfish.grizzly.http.server.AddOn;
import org.glassfish.grizzly.http.server.HttpServerFilter;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.memory.Buffers;
import org.glassfish.grizzly.memory.MemoryManager;

public class WSAddOn extends BaseFilter implements AddOn {

	@SuppressWarnings("rawtypes")
	private Map<Connection, WebsocketImpl> map = new HashMap<>();

	@Override
	public NextAction handleClose(FilterChainContext ctx) throws IOException {
		return super.handleClose(ctx);
	}

	@Override
	public NextAction handleRead(FilterChainContext ctx) throws IOException {
		HttpContent message = ctx.getMessage();
		HttpHeader header = message.getHttpHeader();
		WebsocketImpl impl = map.get(ctx.getConnection());
		if (isWebsocketRequest(header) && impl == null) {
			impl = new RFC6455(ctx);
			impl.doHandShake(header, ctx);
			map.put(ctx.getConnection(), impl);
			return ctx.getStopAction();
		}
		if (impl != null) {
			return doReadWebsockData(ctx, message, impl);
		}
		return super.handleRead(ctx);
	}

	@SuppressWarnings("rawtypes")
	private NextAction doReadWebsockData(FilterChainContext ctx,
			HttpContent message, WebsocketImpl impl) {
		Buffer buffer = message.getContent();
		if (buffer.hasRemaining()) {
			try {
				message.recycle();
				MemoryManager mmgr = ctx.getMemoryManager();
				Map<String, Object> ext = impl.getExt();
				while (buffer != null && buffer.hasRemaining()) {
					Object object = ext.get("buffer");
					if (object instanceof Buffer) {
						Buffer oldBuf = (Buffer) object;
						buffer = Buffers.appendBuffers(mmgr, oldBuf, buffer);
						ext.put("buffer", buffer);
					}
					DataFrame result = impl.parseFrame(buffer);
					if (result == null) {
						ext.put("buffer", buffer);
						break;
					} else {
						impl.onMessage(result);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ctx.getStopAction();
	}

	private boolean isWebsocketRequest(HttpHeader header) {
		boolean up = "Upgrade".equals(header.getHeader("Connection"));
		boolean ws = "websocket".equals(header.getHeader("Upgrade"));
		return up && ws;
	}

	@Override
	public NextAction handleWrite(FilterChainContext ctx) throws IOException {
		return super.handleWrite(ctx);
	}

	@Override
	public void setup(NetworkListener listener, FilterChainBuilder builder) {
		int httpServerFilterIdx = builder.indexOfType(HttpServerFilter.class);
		if (httpServerFilterIdx < 0)
			return;
		builder.add(httpServerFilterIdx, this);
	}

}
