package com.nice.czp.htmlsocket.push.ws.impl;

import java.nio.ByteBuffer;

import org.glassfish.grizzly.Buffer;

import com.nice.czp.htmlsocket.push.ws.err.ProtocolError;
import com.nice.czp.htmlsocket.push.ws.itf.IWSCodec;
import com.nice.czp.htmlsocket.push.ws.itf.IWebsocket;
import com.nice.czp.htmlsocket.push.ws.itf.WSFrameType;
import com.nice.czp.htmlsocket.push.ws.itf.WSMessage;
import com.nice.czp.htmlsocket.push.ws.util.ByteList;
import com.nice.czp.htmlsocket.push.ws.util.FrameUtil;

/**
 * 
 * @author coder_czp@126.com-2015年8月18日
 * 
 */
public class RFC6455CodecImpl implements IWSCodec {

    private boolean isServer;
    private static final int MAX_FRAME_SIZE = 8 * 1024;
    private ThreadLocal<ByteList> continueData = new ThreadLocal<ByteList>();

    public RFC6455CodecImpl() {
        this(true);
    }

    public RFC6455CodecImpl(boolean isServer) {
        this.isServer = isServer;
    }

    @Override
    public byte[] encode(WSMessage frame) {

        if (frame.hasmask && isServer) {
            throw new ProtocolError(IWebsocket.PROTOCOL, "Messages sent to the client can't be set up a mask");
        }
        if (frame.data.length > MAX_FRAME_SIZE) {
            throw new ProtocolError(IWebsocket.MSG_TOO_LARGE, "Data frame payloads must be less than " + MAX_FRAME_SIZE);
        }
        byte[] data = frame.data;
        int dataLen = data.length;
        boolean maskData = frame.hasmask;
        byte[] lengthBytes = encodeLength(dataLen);
        int length = 1 + lengthBytes.length + dataLen + (maskData ? IWebsocket.MASK_SIZE : 0);

        ByteList buf = new ByteList(length);
        byte frist = frame.opcode;
        if (frame.isFin)
            frist |= 0x80;
        if (maskData)
            lengthBytes[0] |= 0x80;

        buf.write(frist);
        buf.write(lengthBytes);
        if (maskData) {
            /* 发给客户端的数据不能加掩码,但客户端发送的消息必须加掩码 */
            byte[] mask = frame.maskKey;
            buf.write(mask);
            maskData(data, mask);
            buf.write(data);

        } else {
            buf.write(data);
        }
        buf.close();
        return buf.toByteArray();
    }

    @Override
    public WSMessage decode(Buffer buf) {

        byte start = buf.get();
        byte lengthCode = buf.get();
        byte opcode = (byte) (start & 0x7f);
        boolean rsvBitSet = decodeRSV(start);
        boolean isFin = FrameUtil.isBitSet(start, 7);
        boolean hasmask = (lengthCode & 0x80) == 0x80;
        WSFrameType frameType = WSFrameType.from(start);
        boolean controlFrame = FrameUtil.isControlFrame(opcode);

        if (rsvBitSet) {
            throw new ProtocolError(IWebsocket.PROTOCOL, "RSV bit(s) doesn't support");
        }
        if (!hasmask && isServer) {
            throw new ProtocolError(IWebsocket.PROTOCOL, "Client message must be set mask");
        }
        if (!isFin && controlFrame) {
            throw new ProtocolError(IWebsocket.PROTOCOL, "Recv control frame,but fin bit isn't 1,data[0]=" + start);
        }
        if (opcode < 0 || opcode > 0xF) {
            throw new ProtocolError(IWebsocket.PROTOCOL, "Unknow opcode :" + opcode);
        }

        if (hasmask) {
            lengthCode ^= 0x80;
        }
        int payloadLen;
        if (lengthCode <= 125) {
            payloadLen = lengthCode;
        } else {
            if (controlFrame) {
                throw new ProtocolError(IWebsocket.PROTOCOL, "Control frame payloads must be less than 125 bytes");
            }
            int lengthBytes = lengthCode == 126 ? 2 : 8;
            if (buf.remaining() < lengthBytes) {
                return null;
            }
            ByteBuffer len = ByteBuffer.allocate(lengthBytes);
            buf.get(len);
            len.flip();
            if (lengthBytes == 2) {
                payloadLen = len.getChar();
            } else {
                payloadLen = (int) len.getLong();
            }
        }
        /* 为了安全考虑 单帧不能超过8K */
        if (payloadLen > MAX_FRAME_SIZE) {
            throw new ProtocolError(IWebsocket.MSG_TOO_LARGE, "Data frame payloads must be less than " + MAX_FRAME_SIZE);
        }
        if (hasmask && buf.remaining() < payloadLen + IWebsocket.MASK_SIZE)
            return null;
        if (!hasmask && buf.remaining() < payloadLen)
            return null;

        byte[] maskKey = null;
        byte[] data = new byte[payloadLen];
        if (hasmask) {
            maskKey = new byte[IWebsocket.MASK_SIZE];
            buf.get(maskKey);
            buf.get(data);
            unmaskData(data, maskKey);
        } else {
            buf.get(data);
        }
        /* 如果不是结束帧,则opcode必须为TXT BIN CONNINUE */
        if (!isFin) {
            ByteList list = continueData.get();
            if (list == null) {
                list = new ByteList();
                continueData.set(list);
            }
            list.write(data);
            /* 返回CONTINUE对象,上层清空缓存数据,但不调用onMessage */
            return WSMessage.CONTINUE;
        }
        if (isFin) {
            if (FrameUtil.isContinueFrame(opcode) && continueData.get() != null) {
                ByteList list = continueData.get();
                /* 把最后一帧添加到缓存 */
                list.write(data);
                data = list.toByteArray();
                /* 必须回收以便下次继续使用 */
                list.reset();
            }
        }

        WSMessage msg = new WSMessage();
        msg.controlFrame = controlFrame;
        msg.payloadLen = payloadLen;
        msg.frameType = frameType;
        msg.rsvBitSet = rsvBitSet;
        msg.hasmask = hasmask;
        msg.maskKey = maskKey;
        msg.opcode = opcode;
        msg.isFin = isFin;
        msg.data = data;

        return msg;
    }

    @Override
    public String getVersion() {
        return "13";
    }

    private boolean decodeRSV(byte start) {
        boolean rsvBitSet = FrameUtil.isBitSet(start, 6) || FrameUtil.isBitSet(start, 5)
                || FrameUtil.isBitSet(start, 4);
        return rsvBitSet;
    }

    private void maskData(byte[] data, byte[] mask) {
        for (int i = 0; i < data.length; i++) {
            data[i] ^= mask[i % mask.length];
        }
    }

    private void unmaskData(byte[] data, byte[] mask) {
        for (int i = 0; i < data.length; i++) {
            data[i] ^= mask[i % mask.length];
        }
    }

    private byte[] encodeLength(long length) {
        if (length <= 125) {
            return new byte[] { (byte) length };
        }
        if (length <= 0xFFFF) {
            ByteBuffer buf = ByteBuffer.allocate(3);
            buf.put((byte) 126);
            buf.putChar((char) length);
            return buf.array();
        }
        ByteBuffer buf = ByteBuffer.allocate(9);
        buf.put((byte) 127);
        buf.putLong(length);
        return buf.array();
    }
}
