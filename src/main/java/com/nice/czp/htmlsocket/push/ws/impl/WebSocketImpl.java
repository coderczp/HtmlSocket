package com.nice.czp.htmlsocket.push.ws.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.memory.Buffers;
import org.glassfish.grizzly.utils.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nice.czp.htmlsocket.push.PushContext;
import com.nice.czp.htmlsocket.push.ws.itf.IWSCodec;
import com.nice.czp.htmlsocket.push.ws.itf.IWebsocket;
import com.nice.czp.htmlsocket.push.ws.itf.WSFrameType;
import com.nice.czp.htmlsocket.push.ws.itf.WSMessage;
import com.nice.czp.htmlsocket.push.ws.itf.WSListener;

/**
 * http://jinnianshilongnian.iteye.com/blog/1899876
 * 
 * @author coder_czp@126.com-2015年8月17日
 * 
 */
@SuppressWarnings("rawtypes")
public class WebSocketImpl implements IWebsocket {

	protected IWSCodec codec;
	protected PushContext context;
	protected Connection connection;
	protected HashMap<String, Object> ext = new HashMap<String, Object>();
	protected List<WSListener> listeners = new CopyOnWriteArrayList<WSListener>();
	private static final Logger log = LoggerFactory.getLogger(WebSocketImpl.class);

	public WebSocketImpl(Connection conn, PushContext context, IWSCodec codec) {
		this.connection = conn;
		this.context = context;
		this.codec = codec;
	}

	@Override
	public String getRemoteAddress() {
		return connection.getPeerAddress().toString();
	}

	@Override
	public String toString() {
		return getRemoteAddress();
	}

	@Override
	public IWSCodec getCodec() {
		return codec;
	}

	@Override
	public void send(Object data) {
		if (data instanceof String) {
			byte[] bytes = ((String) data).getBytes(Charsets.UTF8_CHARSET);
			writeMessage(WSMessage.create(WSFrameType.TXT, bytes, true));
			return;
		}
		if (data instanceof byte[]) {
			writeMessage(WSMessage.create(WSFrameType.BIN, (byte[]) data, true));
			return;
		}
		if (data instanceof WSMessage) {
			writeMessage((WSMessage) data);
			return;
		}
		throw new RuntimeException("except string,byte[],WSMessage,get:" + data);
	}

	@Override
	public void dipatchMessage(WSMessage result) {
		for (WSListener listener : listeners) {
			try {
				boolean isText = result.frameType == WSFrameType.TXT;
				listener.onWSMessage(result.data, isText);
			} catch (Exception e) {
				log.error("{} process message err", listener, e);
			}
		}
	}

	@Override
	public void onDisconnect(int code, String info) {
		for (WSListener listener : listeners) {
			try {
				listener.onClosed(this, code, info);
			} catch (Exception e) {
				log.error("{} closed event error", listener, e);
			}
		}
	}

	@Override
	public void close(int code, String info) {
		if (connection.isOpen()) {
			System.out.println("code:" + code + " info:" + info);
			writeMessage(WSMessage.createClose(code, info));
			connection.closeSilently();
		}
		onDisconnect(code, info);
	}

	protected Connection ensureConnIsOpen() {
		if (connection == null || !connection.isOpen()) {
			throw new IllegalStateException("Connection is null or closed");
		}
		return connection;
	}


	protected void writeMessage(WSMessage msg) {
		IWSCodec codec = getCodec();
		byte[] encode = codec.encode(msg);
		Connection<?> conn = ensureConnIsOpen();
		conn.write(Buffers.wrap(conn.getMemoryManager(), encode));
	}


	@Override
	public boolean addListener(WSListener listener) {
		return listeners.add(listener);
	}

	@Override
	public void onConnected(Map<String, String[]> params) {
		for (WSListener listener : listeners) {
			try {
				listener.onConnected(this, params);
			} catch (Exception e) {
				log.error("{} connect event error", listener, e);
			}
		}
	}
}
