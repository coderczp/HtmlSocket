package com.nice.czp.htmlsocket.push.ws;

public class TextFrameType extends BaseFrameType {
	@Override
	public void setPayload(DataFrame frame, byte[] data) {
		frame.setPayload(data);
	}

	@Override
	public byte[] getBytes(DataFrame dataFrame) {
		final byte[] bytes = dataFrame.getBytes();
		if (bytes == null) {
			setPayload(
					dataFrame,
					Utf8Utils.encode(DataFrame.STRICT_UTF8_CHARSET,
							dataFrame.getTextPayload()));
		}
		return dataFrame.getBytes();
	}

	// public void respond(WebSocket socket, DataFrame frame) {
	// if(frame.isLast()) {
	// socket.onMessage(frame.getTextPayload());
	// } else {
	// socket.onFragment(frame.isLast(), frame.getTextPayload());
	// }
	// }
}
