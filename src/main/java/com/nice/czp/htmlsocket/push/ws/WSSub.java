package com.nice.czp.htmlsocket.push.ws;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nice.czp.htmlsocket.api.IMessage;
import com.nice.czp.htmlsocket.api.IRemoteSubcriber;
import com.nice.czp.htmlsocket.api.PushError;
import com.nice.czp.htmlsocket.push.PushContext;
import com.nice.czp.htmlsocket.push.ws.itf.IWebsocket;
import com.nice.czp.htmlsocket.push.ws.itf.IWebsocketListener;

/**
 * 
 * @author coder_czp@126.com-2015年8月17日
 * 
 */
public class WSSub implements IWebsocketListener, IRemoteSubcriber {

	private static Logger log = LoggerFactory.getLogger(WSSub.class);
	private Map<String, String[]> params;
	private PushContext context;
	private IWebsocket sock;

	public WSSub(PushContext pushCtx) {
		this.context = pushCtx;
	}

	@Override
	public long getId() {
		return Long.valueOf(params.get("id")[0]);
	}

	@Override
	public String getTopic() {
		return params.get("topic")[0];
	}

	@Override
	public void doUnSubscrib() {
		log.info("ws client [{}]-unsub", sock);
		sock.close(IWebsocket.SERVER_ERROR, "normol closed");
	}

	@Override
	public void onBytesMessage(byte[] data) {
		try {
			IMessage msg = context.getCodec().decode(data);
			context.getMessageCenter().send(msg);
		} catch (Exception e) {
			log.error("decode byte message err", e);
		}
	}

	@Override
	public void onTextMessage(String text) {
		try {
			IMessage msg = context.getCodec().decode(text);
			context.getMessageCenter().send(msg);
		} catch (Throwable e) {
			log.error("decode text message err", e);
		}
	}

	@Override
	public void onMessage(IMessage message) {
		try {
			sock.send(context.getCodec().endcodeToText(message));
		} catch (Throwable e) {
			log.error("onMessage err", e);
		}
	}

	@Override
	public void onConnected(IWebsocket websocket, Map<String, String[]> params) {
		PushError err;
		this.params = params;
		this.sock = websocket;
		if ((err = context.beforeConn(this, params)) != null) {
			websocket.close(IWebsocket.SERVER_ERROR, err.toString());
		} else {
			context.getMessageCenter().addSubscriber(this);
			log.info("ws client [{}]-connected", websocket);
		}
	}

	@Override
	public void onClosed(IWebsocket websocket, int code, String info) {
		log.info("ws client [{}]-closed,info:{}", websocket, info);
		context.getMessageCenter().removeSubscriber(getId(), false);
	}
}
