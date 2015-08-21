package com.nice.czp.htmlsocket.push.ws.util;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

import com.nice.czp.htmlsocket.push.ws.itf.IWebsocket;

/**
 * 
 * @author coder_czp@126.com-2015年8月17日
 * 
 */
public class Util {

    // 控制帧最高位为1,和0x08[1000]与运算即可判断
    public static boolean isControlFrame(byte opcode) {
        return (opcode & 0x08) == 0x08;
    }

    public static boolean isBitSet(final byte b, int bit) {
        return ((b >> bit & 1) != 0);
    }

    // x0 代表一个继续帧
    public static boolean isContinueFrame(byte opcode) {
        return opcode == 0;
    }

    public static long toLong(byte[] bytes, int start, int end) {
        long value = 0;
        for (int i = start; i < end; i++) {
            value <<= 8;
            value ^= (long) bytes[i] & 0xFF;
        }
        return value;
    }

    public static byte[] toArray(long length) {
        long value = length;
        byte[] b = new byte[8];
        for (int i = 7; i >= 0 && value > 0; i--) {
            b[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return b;
    }

    public static byte[] toArray(int value) {
        ByteBuffer b = ByteBuffer.allocate(4);
        return b.putInt(value).array();
    }

    public static byte bitSet(byte b, int pos) {
        return (byte) (b | (1 << pos));
    }

    @SuppressWarnings("restriction")
    public static String generateSecKey(String clientKey) {
        try {
            sun.misc.BASE64Encoder endoer = new sun.misc.BASE64Encoder();
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            String serKey = clientKey + IWebsocket.SERVER_KEY_HASH;
            md.update(serKey.getBytes("ASCII"));
            return endoer.encode(md.digest());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }
}
