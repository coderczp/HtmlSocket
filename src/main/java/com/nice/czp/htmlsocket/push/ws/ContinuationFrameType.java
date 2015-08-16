package com.nice.czp.htmlsocket.push.ws;


public class ContinuationFrameType extends BaseFrameType {
    private boolean text;
    private FrameType wrappedType;

    public ContinuationFrameType(boolean text) {
        this.text = text;
        wrappedType = text ? new TextFrameType() : new BinaryFrameType();
    }

//    public void respond(WebSocket socket, DataFrame frame) {
//        if (text) {
//            socket.onFragment(frame.isLast(), frame.getTextPayload());
//        } else {
//            socket.onFragment(frame.isLast(), frame.getBytes());
//        }
//    }

    @Override
    public void setPayload(DataFrame frame, byte[] data) {
        wrappedType.setPayload(frame, data);
    }

    @Override
    public byte[] getBytes(DataFrame dataFrame) {
        return wrappedType.getBytes(dataFrame);
    }
}

