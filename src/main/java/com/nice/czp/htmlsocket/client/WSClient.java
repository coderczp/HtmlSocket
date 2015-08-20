package com.nice.czp.htmlsocket.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.memory.Buffers;
import org.glassfish.grizzly.memory.MemoryManager;
import org.glassfish.grizzly.memory.PooledMemoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nice.czp.htmlsocket.push.ws.impl.RFC6455CodecImpl;
import com.nice.czp.htmlsocket.push.ws.itf.IWSCodec;
import com.nice.czp.htmlsocket.push.ws.itf.IWebsocket;
import com.nice.czp.htmlsocket.push.ws.itf.IWebsocketListener;
import com.nice.czp.htmlsocket.push.ws.itf.WSFrameType;
import com.nice.czp.htmlsocket.push.ws.itf.WSMessage;

/**
 * Websocket客户端
 * 
 * @author coder_czp@126.com-2015年8月19日
 * 
 */
public class WSClient implements IWebsocket {

	private String url;
	private Socket socket;
	private String address;
	private InputStream is;
	private OutputStream os;
	private RFC6455CodecImpl codec = new RFC6455CodecImpl(false);
	private Map<String, String> header = new HashMap<String, String>();
	private Map<String, Object> extmap = new HashMap<String, Object>();
	private List<IWebsocketListener> listenes = new CopyOnWriteArrayList<IWebsocketListener>();

	private static Logger log = LoggerFactory.getLogger(WSClient.class);

	public WSClient(String url) {
		this.url = url;
	}

	public boolean addHeader(String key, String value) {
		return header.put(key, value) != null;
	}

	public void connect() throws Exception {
		URI uri = new URI(url);
		String protocol = uri.getScheme();
		if (!(protocol.equals("ws") || protocol.equals("http")))
			throw new RuntimeException(url + " isn't a valid websocket url");
		doConnect(uri);
		log.info("webscoket has connected:{}", url);
	}

	private void doConnect(URI uri) throws Exception {
		socket = new Socket(uri.getHost(), uri.getPort());
		address = socket.getRemoteSocketAddress().toString();
		is = socket.getInputStream();
		os = socket.getOutputStream();

		String wsKey = generateWSKey();

		header.put("Accept", "*/*");
		header.put("Host", uri.getHost());
		header.put("Content-Length", "0");
		header.put("Upgrade", "websocket");
		header.put("Origin", uri.toString());
		header.put("Connection", "Upgrade");
		header.put("Sec-WebSocket-Key", wsKey);
		header.put("Sec-WebSocket-Version", "13");
		header.put("Sec-WebSocket-Protocol", "chat, superchat");
		header.put("User-Agent", "Mozilla/4.0(compatible;WSClient1.0;OS0.0)");

		String path = uri.getPath();
		String split = path.endsWith("/") ? "" : "/";
		PrintWriter writer = new PrintWriter(os);
		writer.print(String.format("GET %s%s?%s HTTP/1.1\r\n", path, split,
				uri.getQuery()));
		for (Map.Entry<String, String> item : header.entrySet()) {
			writer.print(String.format("%s: %s\r\n", item.getKey(),
					item.getValue()));
		}
		writer.print("\r\n");
		writer.flush();
		readResponse();
		startReadData();
	}

	private void readResponse() throws IOException {
		byte b = (byte) is.read();
		if (b == -1) {
			socket.close();
			throw new RuntimeException("handshake error: connect closed");
		}
		int available = is.available();
		byte[] data = new byte[available + 1];
		data[0] = b;
		is.read(data, 1, available);
		String response = new String(data);
		log.debug("http response:{}", response);
		if (!response.contains("HTTP/1.1 101")) {
			socket.close();
			throw new RuntimeException("handshake error:" + response);
		}
	}

	private String generateWSKey() {
		Random r = new Random();
		int numChars = r.nextInt(13) + 1;
		StringBuilder key = new StringBuilder();
		for (int i = 0; i < numChars; i++) {
			char randChar = (char) (r.nextInt(95) + 33);
			if (randChar >= 48 && randChar <= 57) {
				randChar -= 15;
			}
			key.append(randChar);
		}
		key.append("==");
		return key.toString();
	}

	private void writeAndFlush(WSMessage message) {
		if (socket.isClosed())
			throw new RuntimeException("wsocket has closed");
		try {
			os.write(codec.encode(message));
			os.flush();
		} catch (Exception e) {
			log.error("send message error:" + message, e);
		}
	}

	private byte[] generateMask() {
		Random r = new Random();
		ByteBuffer buf = ByteBuffer.allocate(4);
		return buf.putInt(r.nextInt()).array();
	}

	private void disConnected() {
		try {
			socket.close();
		} catch (IOException e) {
			log.error("close socket error", e);
		}
	}

	@SuppressWarnings("rawtypes")
	private void startReadData() {
		Runnable read = new Runnable() {

			@Override
			public void run() {
				try {
					MemoryManager mm = PooledMemoryManager.DEFAULT_MEMORY_MANAGER;
					Buffer buffer = mm.allocate(8192);
					byte[] buf = new byte[1024];
					int read = -1;
					WSMessage message;
					while (!Thread.interrupted()) {
						while ((read = is.read(buf)) != -1) {
							if (read > buffer.limit() - buffer.position()) {
								Buffer newBuf = mm.allocate(1024);
								buffer = Buffers.appendBuffers(mm, newBuf,
										buffer);
							}
							buffer.put(buf, 0, read).flip();
							if ((message = codec.decode(buffer)) == null)
								continue;
							buffer.clear();
							log.debug("recv message:{}", message);
							if (message.frameType == WSFrameType.CLOSE) {
								byte[] data = message.data;
								if (data.length < 2) {
									onClose(NORMAL, "normal closed");
									break;
								}
								int code = data[0] << 8 | data[1];
								onClose(code, new String(data, 2,
										data.length - 2));
								break;
							}
							dipatchMessage(message);
						}
						buffer.release();
						break;
					}
				} catch (Exception e) {
					log.error("read data error", e);
				}
				disConnected();
			}
		};
		Thread thread = new Thread(read, "wsclient-read");
		thread.setDaemon(true);
		thread.start();
	}

	@Override
	public IWSCodec getCodec() {
		return codec;
	}

	@Override
	public void send(Object data) {
		byte[] mask = generateMask();
		if (data instanceof String) {
			byte[] bytes = ((String) data).getBytes(Charset.forName("utf-8"));
			writeAndFlush(WSMessage.create(WSFrameType.TXT, bytes, mask));
			return;
		}
		if (data instanceof byte[]) {
			writeAndFlush(WSMessage
					.create(WSFrameType.BIN, (byte[]) data, mask));
			return;
		}
		if (data instanceof WSMessage) {
			writeAndFlush((WSMessage) data);
			return;
		}
		throw new RuntimeException("except string,byte[],WSMessage,get:" + data);
	}

	@Override
	public String getRemoteAddress() {
		return address;
	}

	@Override
	public Map<String, Object> getExt() {
		return extmap;
	}

	@Override
	public void close(int code, String info) {
		byte[] mask = generateMask();
		send(WSMessage.createClose(code, info, mask));
		disConnected();
		onClose(code, info);
	}

	@Override
	public void onClose(int code, String info) {
		log.error("websock is closed,code:{} reason:{}", code, info);
		for (IWebsocketListener listener : listenes) {
			listener.onClosed(this, code, info);
		}
	}

	@Override
	public void dipatchMessage(WSMessage message) {
		boolean isTxt = (message.frameType == WSFrameType.TXT);
		for (IWebsocketListener listener : listenes) {
			listener.onWSMessage(message.data, isTxt);
		}
	}

	@Override
	public void addListener(IWebsocketListener listener) {
		listenes.add(listener);
	}

	@Override
	public Map<String, String> getHandShakePack(
			Map<String, String> requestParams) {
		return header;
	}
}
