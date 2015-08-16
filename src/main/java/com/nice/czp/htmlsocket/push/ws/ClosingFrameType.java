package com.nice.czp.htmlsocket.push.ws;

public class ClosingFrameType extends BaseFrameType {

	@Override
	public DataFrame create(boolean fin, byte[] data) {
		return new ClosingFrame(data);
	}
}
