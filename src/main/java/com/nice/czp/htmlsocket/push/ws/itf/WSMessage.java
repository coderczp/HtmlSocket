package com.nice.czp.htmlsocket.push.ws.itf;

import java.nio.ByteBuffer;

import org.glassfish.grizzly.utils.Charsets;


/**
 * TODO Please Descrip This Class
 * 
 * @author coder_czp@126.com-2015年8月17日
 * 
 */
public class WSMessage {

    public byte opcode;
    public boolean isFin;
    public boolean hasmask;
    public boolean rsvBitSet;
    public boolean controlFrame;
    public long payloadLen;
    public byte[] maskKey;
    public byte[] data;
    public WSFrameType frameType;
    public static final byte[] EMPTY = new byte[0];
    public static final WSMessage CONTINUE = create(WSFrameType.Continuation);

    @Override
    public String toString() {
        if (data != null)
            return new String(data);
        return "null";
    }

    public static WSMessage create(WSFrameType type, byte[] data, byte[] mask, boolean isFin) {
        WSMessage msg = new WSMessage();
        msg.controlFrame = type == WSFrameType.CLOSE || type == WSFrameType.PING || type == WSFrameType.PONG;
        msg.opcode = (byte) type.value;
        msg.payloadLen = data.length;
        msg.hasmask = mask != null;
        msg.rsvBitSet = false;
        msg.frameType = type;
        msg.isFin = isFin;
        msg.maskKey = mask;
        msg.data = data;
        return msg;
    }
    
    public static WSMessage create(WSFrameType type, byte[] data){
    	return create(type, data, true);
    }
    
    public static WSMessage create(WSFrameType type, byte[] data,byte[] mask) {
        return create(type, data, mask, true);
    }
    
    public static WSMessage create(WSFrameType type, byte[] data, boolean isFin) {
        return create(type, data, null);
    }

    public static WSMessage create(WSFrameType type) {
        return create(type, EMPTY, true);
    }

    /* 内容为两字节的code+utf8编码的reason */
    public static WSMessage createClose(int code, String reason,byte[] mask) {
    	byte[] utf8 = reason.getBytes(Charsets.UTF8_CHARSET);
        ByteBuffer buf = ByteBuffer.allocate(2 + utf8.length);
        buf.putChar((char) code);
        buf.put(utf8);
        return create(WSFrameType.CLOSE, buf.array(), mask);
    }
  
    public static WSMessage createClose(int code, String reason) {
        return createClose(code, reason,null);
    }
}
