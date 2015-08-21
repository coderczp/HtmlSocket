package com.nice.czp.htmlsocket.push.ws;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
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
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.HttpResponsePacket;
import org.glassfish.grizzly.http.server.AddOn;
import org.glassfish.grizzly.http.server.HttpServerFilter;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.grizzly.memory.Buffers;
import org.glassfish.grizzly.memory.MemoryManager;
import org.glassfish.grizzly.utils.IdleTimeoutFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nice.czp.htmlsocket.api.PushError;
import com.nice.czp.htmlsocket.push.PushContext;
import com.nice.czp.htmlsocket.push.ws.err.ProtocolError;
import com.nice.czp.htmlsocket.push.ws.impl.WebSocketImpl;
import com.nice.czp.htmlsocket.push.ws.itf.IWSCodec;
import com.nice.czp.htmlsocket.push.ws.itf.IWebsocket;
import com.nice.czp.htmlsocket.push.ws.itf.WSFrameType;
import com.nice.czp.htmlsocket.push.ws.itf.WSMessage;
import com.nice.czp.htmlsocket.push.ws.util.Util;

@SuppressWarnings("rawtypes")
public class WSAddOn extends BaseFilter implements AddOn {

	private long idleTimeout;
	private PushContext pctx;
	private Map<Connection, Buffer> cache = new HashMap<>();
	private Map<Connection, IWebsocket> map = new HashMap<>();
	private static Logger log = LoggerFactory.getLogger(WSAddOn.class);

	public WSAddOn(PushContext ctx) {
		this(ctx, 5 * 60 * 1000);
	}

	public WSAddOn(PushContext ctx, long idleTimeout) {
		this.idleTimeout = idleTimeout;
		this.pctx = ctx;
	}

	@Override
	public NextAction handleClose(FilterChainContext ctx) throws IOException {
		IWebsocket ws = map.remove(ctx.getConnection());
		if (ws != null) {
			ws.onDisconnect(IWebsocket.NO_CLOSE, "normal closed");
			return ctx.getStopAction();
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
			HttpHeader header = ctx.<HttpContent> getMessage().getHttpHeader();
			Connection conn = ctx.getConnection();
			IWebsocket impl = map.get(conn);
			if (impl != null) {
				return doReadWebsockData(ctx, impl);
			}
			if (isWebsocketRequest(header)) {
				doHandShake(ctx);
				IWSCodec codec = getCodec(header);
				Map<String, String[]> param = getParam(header);
				if (beforeCreateCheck(param, codec, ctx))
					return ctx.getStopAction();

				impl = new WebSocketImpl(conn, pctx, codec);
				impl.addListener(new WSSubcriber(pctx));
				setWSConnIdleTimeout(ctx);
				map.put(conn, impl);
				impl.onConnected(param);
				return ctx.getStopAction();
			}
		} catch (Exception e) {
			log.error("handle read error ", e);
			handleWebscoketClose(ctx, e);
			return ctx.getStopAction();
		}
		return ctx.getInvokeAction();
	}

	/* queryString :id=xx&topic=xx .. */
	private Map<String, String[]> getParam(HttpHeader header) {
		HttpRequestPacket pack = (HttpRequestPacket) header;
		Map<String, String[]> map = new HashMap<String, String[]>();
		StringTokenizer tk = new StringTokenizer(pack.getQueryString(), "&");
		while (tk.hasMoreTokens()) {
			String keyValue = tk.nextToken();
			int index = keyValue.indexOf("=");
			if (index > 0) {
				String key = keyValue.substring(0, index);
				String[] value = keyValue.substring(index + 1).split(",");
				map.put(key, value);
			} else {
				log.error("error param {}", keyValue);
			}
		}
		return map;
	}

	private boolean beforeCreateCheck(Map<String, String[]> params,
			IWSCodec codec, FilterChainContext ctx) {
		PushError err;
		if ((err = pctx.beAddSub(params)) != null) {
			int code = err.getCode();
			String desc = err.getDesc();
			WSMessage close = WSMessage.createClose(code, desc);
			ctx.write(Buffers.wrap(ctx.getMemoryManager(), codec.encode(close)));
			ctx.getConnection().closeSilently();
			return true;
		}
		return false;
	}

	private void doHandShake(FilterChainContext ctx) {
		HttpHeader header = ctx.<HttpContent> getMessage().getHttpHeader();
		HttpRequestPacket pack = (HttpRequestPacket) header;
		HttpResponsePacket rep = pack.getResponse();
		rep.setStatus(HttpStatus.SWITCHING_PROTOCOLS_101);

		String cliKey = header.getHeader(IWebsocket.SEC_WS_KEY_HEADER);
		rep.addHeader("Sec-WebSocket-Accept", Util.generateSecKey(cliKey));
		rep.addHeader("Server", IWebsocket.SERVER_NAME);
		rep.addHeader("Upgrade", "websocket");
		rep.addHeader("Connection", "Upgrade");

		ctx.write(HttpContent.builder(rep).build());
	}

	private void handleWebscoketClose(FilterChainContext ctx, Exception e) {
		IWebsocket impl = map.remove(ctx.getConnection());
		if (impl == null)
			return;
		if (e instanceof ProtocolError) {
			ProtocolError pe = (ProtocolError) e;
			impl.close(pe.getCode(), pe.getMessage());
		} else {
			impl.close(IWebsocket.NORMAL, "normal closed");
		}
	}

	private IWSCodec getCodec(HttpHeader header) {
		String version = header.getHeader(IWebsocket.SEC_WS_VERSION);
		return pctx.getProtocolManager().getCodec(version);
	}

	private NextAction doReadWebsockData(FilterChainContext ctx, IWebsocket impl)
			throws IOException {
		HttpContent message = ctx.getMessage();
		Buffer buffer = message.getContent();
		message.recycle();
		if (!buffer.hasRemaining())
			return ctx.getStopAction();

		Connection conn = ctx.getConnection();
		Buffer lastData = cache.get(conn);
		if (lastData != null) {
			MemoryManager mm = ctx.getMemoryManager();
			buffer = Buffers.appendBuffers(mm, lastData, buffer);
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
				cache.put(conn, buffer);
				break;
			}
			type = result.frameType;
			if (type == WSFrameType.CLOSE) {
				hasmsg = true;
				handleWebscoketClose(ctx, null);
				break;
			}
			if (type == WSFrameType.TXT || type == WSFrameType.BIN) {
				hasmsg = true;
				impl.dipatchMessage(result);
			}
		}
		if (hasmsg) {
			buffer.clear();
			buffer.release();
			cache.remove(conn);
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
