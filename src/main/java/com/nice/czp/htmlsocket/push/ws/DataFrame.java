package com.nice.czp.htmlsocket.push.ws;

import java.io.IOException;
import java.io.OutputStream;


public class DataFrame {
	 public static final StrictUtf8 STRICT_UTF8_CHARSET = new StrictUtf8();
	    public static final float STRICT_UTF8_MAX_BYTES_PER_CHAR =
	            STRICT_UTF8_CHARSET.newEncoder().maxBytesPerChar();
	    
	    public static boolean isDataFrame(final Object o) {
	        return o instanceof DataFrame;
	    }
	    
	    private String payload;
	    private byte[] bytes;
	    private final FrameType type;
	    private boolean last = true;
	    
	    public DataFrame(FrameType type) {
	        this.type = type;
	    }

	    public DataFrame(FrameType type, String data) {
	        this(type, data, true);
	    }

	    public DataFrame(FrameType type, String data, boolean fin) {
	        this.type = type;
	        setPayload(data);
	        last = fin;
	    }

	    public DataFrame(FrameType type, byte[] data) {
	        this(type, data, true);
	    }

	    public DataFrame(FrameType type, byte[] data, boolean fin) {
	        this.type = type;
	        type.setPayload(this, data);
	        last = fin;
	    }

	    public FrameType getType() {
	        return type;
	    }

	    public String getTextPayload() {
	        return payload;
	    }

	    public final void setPayload(String payload) {
	        this.payload = payload;
	    }

	    public void setPayload(byte[] bytes) {
	        this.bytes = bytes;
	    }

	    public byte[] getBytes() {
	        if (payload != null) {
	            bytes = Utf8Utils.encode(STRICT_UTF8_CHARSET, payload);
	        }
	        return bytes;
	    }

	    public void toStream(final OutputStream os) throws IOException {
	        if (payload != null) {
	            Utf8Utils.encode(STRICT_UTF8_CHARSET, payload, os);
	        }
	    }

	    @Override
	    public String toString() {
	        return new StringBuilder("DataFrame")
	                .append("{")
	                .append("last=").append(last)
	                .append(", type=").append(type.getClass().getSimpleName())
	                .append(", payload='").append(getTextPayload()).append('\'')
	                .append(", bytes=").append(Utils.toString(bytes))
	                .append('}')
	                .toString();
	    }

	    public boolean isLast() {
	        return last;
	    }

	    public void setLast(boolean last) {
	        this.last = last;
	    }
}
