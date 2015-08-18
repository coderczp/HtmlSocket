package com.nice.czp.htmlsocket.push.ws.itf;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.memory.Buffers;
import org.glassfish.grizzly.utils.Charsets;

/**
 * TODO Please Descrip This Class
 * 
 * @author coder_czp@126.com-2015年8月17日
 * 
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractWebscoket implements IWebsocket {

	protected CopyOnWriteArrayList<IWebsocketListener> listeners = new CopyOnWriteArrayList<IWebsocketListener>();
	protected Map<String, String[]> params = new HashMap<String, String[]>();
	protected HashMap<String, Object> ext = new HashMap<String, Object>();
	protected Connection connection;

	public AbstractWebscoket(Connection connection) {
		this.connection = connection;
	}

	@Override
	public void addListener(IWebsocketListener listener) {
		listeners.add(listener);
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
	public void onMessage(WSMessage result) {
		if (result.frameType == WSFrameType.TXT) {
			fireTextMessage(new String(result.data));
		} else if (result.frameType == WSFrameType.BIN) {
			fireBinMessage(result.data);
		}
	}

	@Override
	public void close(int code, String info) {
		if (connection.isOpen()) {
			writeMessage(WSMessage.createClose(code, info));
			connection.closeSilently();
		}
		fireDisConnected(code, info);
	}

	@Override
	public final Map<String, Object> getExt() {
		return ext;
	}

	protected Connection ensureConnIsOpen() {
		Connection conn = connection;
		if (conn == null || !conn.isOpen()) {
			throw new IllegalStateException("Connection is null or closed");
		}
		return conn;
	}

	protected void fireConnected() {
		for (IWebsocketListener listener : listeners) {
			listener.onConnected(this, params);
		}
	}

	protected void fireDisConnected(int code, String info) {
		for (IWebsocketListener listener : listeners) {
			listener.onClosed(this, code, info);
		}
	}

	protected void fireTextMessage(String message) {
		for (IWebsocketListener listener : listeners) {
			listener.onTextMessage(message);
		}
	}

	protected void fireBinMessage(byte[] message) {
		for (IWebsocketListener listener : listeners) {
			listener.onBytesMessage(message);
		}
	}

	protected void writeMessage(WSMessage msg) {
		IWSCodec codec = getCodec();
		byte[] encode = codec.encode(msg);
		Connection<?> conn = ensureConnIsOpen();
		conn.write(Buffers.wrap(conn.getMemoryManager(), encode));
	}

}
