package com.nice.czp.htmlsocket.push.ws;


public interface FrameType {

	void setPayload(DataFrame frame, byte[] data);

	byte[] getBytes(DataFrame dataFrame);

	DataFrame create(boolean fin, byte[] data);
}
