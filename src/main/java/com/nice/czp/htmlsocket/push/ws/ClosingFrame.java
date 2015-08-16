package com.nice.czp.htmlsocket.push.ws;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;


public class ClosingFrame extends DataFrame {
	 public static final byte[] EMPTY_BYTES = new byte[0];
	 int NORMAL_CLOSURE = 1000;
	    private int code = NORMAL_CLOSURE;
	    private String reason;

	    public ClosingFrame() {
	        super(new ClosingFrameType());
	    }

	    public ClosingFrame(int code, String reason) {
	        super(new ClosingFrameType());
	        if (code > 0) {
	            this.code = code;
	        }
	        this.reason = reason;
	    }

	    public ClosingFrame(byte[] data) {
	        super(new ClosingFrameType());
	        setPayload(data);
	    }

	    public int getCode() {
	        return code;
	    }

	    public String getReason() {
	        return reason;
	    }

	    @Override
	    public void setPayload(byte[] bytes) {
	        if (bytes.length == 1) {
	            throw new ProtocolError("Closing frame payload, if present, must be a minimum of 2 bytes in length");
	        }
	        if (bytes.length > 0) {
	            code = (int) Utils.toLong(bytes, 0, 2);
	            if (code < 1000 || code == 1004 || code == 1005 || code == 1006 || (code > 1011 && code < 3000) || code > 4999) {
	                throw new ProtocolError("Illegal status code: " + code);
	            }
	            if (bytes.length > 2) {
	                utf8Decode(bytes);
	            }
	        }
	    }

	    @Override
	    public byte[] getBytes() {
	        if (code == -1) {
	            return EMPTY_BYTES;
	        }

	        final byte[] bytes = Utils.toArray(code);
	        final byte[] reasonBytes = reason == null ? EMPTY_BYTES : reason.getBytes(new StrictUtf8());
	        final byte[] frameBytes = new byte[2 + reasonBytes.length];
	        System.arraycopy(bytes, bytes.length - 2, frameBytes, 0, 2);
	        System.arraycopy(reasonBytes, 0, frameBytes, 2, reasonBytes.length);

	        return frameBytes;
	    }

	    @Override
	    public String toString() {
	        final StringBuilder sb = new StringBuilder();
	        sb.append("ClosingFrame");
	        sb.append("{code=").append(code);
	        sb.append(", reason=").append(reason == null ? null : "'" + reason + "'");
	        sb.append('}');
	        return sb.toString();
	    }


	    // --------------------------------------------------------- Private Methods

	    private void utf8Decode(byte[] data) {
	        final ByteBuffer b = ByteBuffer.wrap(data, 2, data.length - 2);
	        Charset charset = new StrictUtf8();
	        final CharsetDecoder decoder = charset.newDecoder();
	        int n = (int) (b.remaining() * decoder.averageCharsPerByte());
	        CharBuffer cb = CharBuffer.allocate(n);
	        for (; ; ) {
	            CoderResult result = decoder.decode(b, cb, true);
	            if (result.isUnderflow()) {
	                decoder.flush(cb);
	                cb.flip();
	                reason = cb.toString();
	                break;
	            }
	            if (result.isOverflow()) {
	                CharBuffer tmp = CharBuffer.allocate(2 * n + 1);
	                cb.flip();
	                tmp.put(cb);
	                cb = tmp;
	                continue;
	            }
	            if (result.isError() || result.isMalformed()) {
	                throw new Utf8DecodingError("Illegal UTF-8 Sequence");
	            }
	        }

	    }
}
