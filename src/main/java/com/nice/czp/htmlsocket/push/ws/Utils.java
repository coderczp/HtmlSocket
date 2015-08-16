package com.nice.czp.htmlsocket.push.ws;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



public class Utils {
    
    static final Writer NULL_WRITER = new Writer() {
        private IOException ioe;

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            // we don't care if ioe will be initialized several times because of thread racing
            if (ioe == null) {
                ioe = new IOException("Can't write to a websocket using ServletWriter");
            }
            
            throw ioe;
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }
        
    };
    
    public static byte[] toArray(long length) {
        long value = length;
        byte[] b = new byte[8];
        for (int i = 7; i >= 0 && value > 0; i--) {
            b[i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return b;
    }

    public static long toLong(byte[] bytes, int start, int end) {
        long value = 0;
        for (int i = start; i < end; i++) {
            value <<= 8;
            value ^= (long) bytes[i] & 0xFF;
        }
        return value;
    }

    public static List<String> toString(byte[] bytes) {
        return toString(bytes, 0, bytes.length);
    }

    public static List<String> toString(byte[] bytes, int start, int end) {
        List<String> list = new ArrayList<String>();
        for (int i = start; i < end; i++) {
            list.add(Integer.toHexString(bytes[i] & 0xFF).toUpperCase(Locale.US));
        }
        return list;
    }
}
