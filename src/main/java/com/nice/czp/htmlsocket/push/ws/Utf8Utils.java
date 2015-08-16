package com.nice.czp.htmlsocket.push.ws;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

import org.glassfish.grizzly.utils.Charsets;

public class Utf8Utils {
	 private static final byte[] EMPTY_ARRAY = new byte[0];

	    public static byte[] encode(Charset charset, String string) {
	        if (string.length() == 0) {
	            return EMPTY_ARRAY;
	        }


	        CharsetEncoder ce = Charsets.getCharsetEncoder(charset);

	        int en = scale(string.length(), ce.maxBytesPerChar());
	        byte[] ba = new byte[en];

	        ce.reset();
	        ByteBuffer bb = ByteBuffer.wrap(ba);
	        CharBuffer cb = CharBuffer.wrap(string);
	        try {
	            CoderResult cr = ce.encode(cb, bb, true);
	            if (!cr.isUnderflow())
	                cr.throwException();
	            cr = ce.flush(bb);
	            if (!cr.isUnderflow())
	                cr.throwException();
	        } catch (CharacterCodingException x) {
	            // Substitution is always enabled,
	            // so this shouldn't happen
	            throw new Error(x);
	        }
	        return safeTrim(ba, bb.position());
	    }

	    public static void encode(Charset charset, String string, OutputStream os)
	            throws IOException {
	        
	        if (string.length() == 0)
	            return;
	        
	        final CharsetEncoder ce = Charsets.getCharsetEncoder(charset);
	        int en = scale(string.length(), ce.maxBytesPerChar());
	        byte[] ba = new byte[en];

	        ce.reset();
	        ByteBuffer bb = ByteBuffer.wrap(ba);
	        CharBuffer cb = CharBuffer.wrap(string);
	        try {
	            CoderResult cr = ce.encode(cb, bb, true);
	            if (!cr.isUnderflow())
	                cr.throwException();
	            cr = ce.flush(bb);
	            if (!cr.isUnderflow())
	                cr.throwException();
	        } catch (CharacterCodingException x) {
	            // Substitution is always enabled,
	            // so this shouldn't happen
	            throw new Error(x);
	        }
	        
	        os.write(ba, 0, bb.position());
	    }

	    private static int scale(int len, float expansionFactor) {
	        // We need to perform double, not float, arithmetic; otherwise
	        // we lose low order bits when len is larger than 2**24.
	        return (int) (len * (double) expansionFactor);
	    }

	    // Trim the given byte array to the given length
	    //
	    private static byte[] safeTrim(byte[] ba, int len) {
	        if (len == ba.length
	                && (System.getSecurityManager() == null)) {
	            return ba;
	        } else {
	            return copyOf(ba, len);
	        }
	    }

	    private static byte[] copyOf(byte[] original, int newLength) {
	        byte[] copy = new byte[newLength];
	        System.arraycopy(original, 0, copy, 0,
	                Math.min(original.length, newLength));
	        return copy;
	    }
}
