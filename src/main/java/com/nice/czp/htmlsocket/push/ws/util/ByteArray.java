package com.nice.czp.htmlsocket.push.ws.util;

import java.io.ByteArrayOutputStream;

/**
 * 
 * @author coder_czp@126.com-2015年8月18日
 * 
 */
public class ByteArray extends ByteArrayOutputStream {

    public ByteArray() {
        this(32);
    }

    public ByteArray(int length) {
        super(length);
    }

    /**
     * 为了避免复制直接返回原始的buf
     */
    @Override
    public synchronized byte[] toByteArray() {
        return buf;
    }

    public synchronized void write(byte[] data) {
        write(data, 0, data.length);
    }

    public synchronized void close() {
        reset();
    }
}
