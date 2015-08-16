package com.nice.czp.htmlsocket.push.ws;



public class BaseFrameType implements FrameType{
	public void setPayload(DataFrame frame, byte[] data) {
		frame.setPayload(data);
	}

	public byte[] getBytes(DataFrame dataFrame) {
		return dataFrame.getBytes();
	}

	public DataFrame create(boolean fin, byte[] data) {
		return new DataFrame(this, data, fin);
	}
}
