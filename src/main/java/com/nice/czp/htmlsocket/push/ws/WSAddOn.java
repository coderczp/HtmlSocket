package com.nice.czp.htmlsocket.push.ws;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChain;
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
import org.glassfish.grizzly.utils.IdleTimeoutFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nice.czp.htmlsocket.push.PushContext;
import com.nice.czp.htmlsocket.push.ws.err.ProtocolError;
import com.nice.czp.htmlsocket.push.ws.impl.ProtocolManager;
import com.nice.czp.htmlsocket.push.ws.impl.WebSocketImpl;
import com.nice.czp.htmlsocket.push.ws.itf.IWSCodec;
import com.nice.czp.htmlsocket.push.ws.itf.IWebsocket;
import com.nice.czp.htmlsocket.push.ws.itf.WSFrameType;
import com.nice.czp.htmlsocket.push.ws.itf.WSMessage;

@SuppressWarnings("rawtypes")
public class WSAddOn extends BaseFilter implements AddOn {

	private long idleTimeout;
	private PushContext pushCtx;
	private Map<Connection, IWebsocket> map = new HashMap<>();
	private static Logger log = LoggerFactory.getLogger(WSAddOn.class);

	public WSAddOn(PushContext ctx) {
		this(ctx, 3 * 60 * 1000);
	}

	public WSAddOn(PushContext ctx, long idleTimeout) {
		this.idleTimeout = idleTimeout;
		this.pushCtx = ctx;
	}

	@Override
	public NextAction handleClose(FilterChainContext ctx) throws IOException {
		try {
			IWebsocket ws = map.remove(ctx.getConnection());
			if (ws == null)
				return ctx.getInvokeAction();

			if (!(ctx.getMessage() instanceof ProtocolError)) {
				ws.close(IWebsocket.NO_CLOSE, "normal closed");
				return ctx.getInvokeAction();
			}

			ProtocolError er = ctx.getMessage();
			ws.close(er.getCode(), er.getMessage());

		} catch (Exception e) {
			log.error("close webscok erroe", e);
		}
		return ctx.getInvokeAction();
	}

	@Override
	public NextAction handleWrite(FilterChainContext ctx) throws IOException {
		IWebsocket ws = map.get(ctx.getConnection());
		if (ws != null && ctx.getMessage() instanceof WSMessage) {
			ws.send(ctx.getMessage());
		}
		return ctx.getInvokeAction();
	}

	@Override
	public void setup(NetworkListener listener, FilterChainBuilder builder) {
		int index = builder.indexOfType(HttpServerFilter.class);
		index = index < 0 ? 0 : index;
		builder.add(index, this);
	}

	@Override
	public NextAction handleRead(FilterChainContext ctx) throws IOException {
		try {
			HttpContent message = ctx.getMessage();
			HttpHeader header = message.getHttpHeader();
			IWebsocket impl = map.get(ctx.getConnection());
			if (impl != null) {
				return doReadWebsockData(ctx, message, impl);
			}
			if (isWebsocketRequest(header)) {
				IWSCodec codec = getCodec(header);
				impl = new WebSocketImpl(ctx, codec);
				impl.addListener(new WSSub(pushCtx));
				impl.doHandShake(header, ctx);
				map.put(ctx.getConnection(), impl);
				setWSConnIdleTimeout(ctx);
				return ctx.getStopAction();
			}
		} catch (Exception e) {
			log.error("handle read error ", e);
			ctx.setMessage(e);
			handleClose(ctx);

		}
		return ctx.getInvokeAction();
	}

	private IWSCodec getCodec(HttpHeader header) {
		String version = header.getHeader(IWebsocket.SEC_WS_VERSION);
		return ProtocolManager.getInstance().getCodec(version);
	}

	private NextAction doReadWebsockData(FilterChainContext ctx,
			HttpContent message, IWebsocket impl) throws IOException {
		Buffer buffer = message.getContent();
		message.recycle();
		if (!buffer.hasRemaining())
			return ctx.getStopAction();

		Map<String, Object> ext = impl.getExt();
		Object obj = ext.get("buffer");
		if (obj instanceof Buffer) {
			Buffer oldBuf = (Buffer) obj;
			MemoryManager mm = ctx.getMemoryManager();
			buffer = Buffers.appendBuffers(mm, oldBuf, buffer);
		}
		WSMessage result;
		WSFrameType type;
		boolean hasmsg = false;
		IWSCodec codec = impl.getCodec();
		while (buffer.hasRemaining()) {
			buffer.mark();
			result = codec.decode(buffer);
			if (result == null) {
				buffer.reset();
				ext.put("buffer", buffer);
				break;
			}
			type = result.frameType;
			if (type == WSFrameType.CLOSE) {
				hasmsg = true;
				handleClose(ctx);
				break;
			}
			if (type == WSFrameType.TXT || type == WSFrameType.BIN) {
				hasmsg = true;
				impl.onMessage(result);
			}
		}
		if (hasmsg) {
			buffer.clear();
			buffer.release();
			ext.remove("buffer");
		}
		return ctx.getStopAction();
	}

	private boolean isWebsocketRequest(HttpHeader header) {
		return "upgrade".equalsIgnoreCase(header.getHeader("Connection"))
				&& "websocket".equalsIgnoreCase(header.getHeader("Upgrade"));
	}

	private void setWSConnIdleTimeout(FilterChainContext ctx) {
		FilterChain filterChain = ctx.getFilterChain();
		if (filterChain.indexOfType(IdleTimeoutFilter.class) >= 0) {
			IdleTimeoutFilter.setCustomTimeout(ctx.getConnection(),
					idleTimeout, TimeUnit.MILLISECONDS);
		}
	}

}
