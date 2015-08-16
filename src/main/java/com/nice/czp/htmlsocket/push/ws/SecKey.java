package com.nice.czp.htmlsocket.push.ws;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import org.glassfish.grizzly.http.util.Base64Utils;
import org.glassfish.grizzly.utils.Charsets;
import org.glassfish.grizzly.websockets.Constants;
import org.glassfish.grizzly.websockets.HandshakeException;

public class SecKey {
	 private static final Random random = new SecureRandom();

	    public static final int KEY_SIZE = 16;

	    /**
	     * Security key string representation, which includes chars and spaces.
	     */
	    private final String secKey;

	    private byte[] bytes;

	    public SecKey() {
	        secKey = create();
	    }

	    private String create() {
	        bytes = new byte[KEY_SIZE];
	        random.nextBytes(bytes);
	        return Base64Utils.encodeToString(bytes, false);
	    }

	    public SecKey(String base64) {
	        if(base64 == null) {
	            throw new HandshakeException("Null keys are not allowed.");
	        }
	        secKey = base64;
	    }

	    /**
	     * Generate server-side security key, which gets passed to the client during
	     * the handshake phase as part of message payload.
	     *
	     * @param clientKey client's Sec-WebSocket-Key
	     * @return server key.
	     *
	     */
	    public static SecKey generateServerKey(SecKey clientKey) throws HandshakeException {
	        String key = clientKey.getSecKey() + Constants.SERVER_KEY_HASH;
	        final MessageDigest instance;
	        try {
	            instance = MessageDigest.getInstance("SHA-1");
	            instance.update(key.getBytes(Charsets.ASCII_CHARSET));
	            final byte[] digest = instance.digest();
	            if(digest.length != 20) {
	                throw new HandshakeException("Invalid key length.  Should be 20: " + digest.length);
	            }

	            return new SecKey(Base64Utils.encodeToString(digest, false));
	        } catch (NoSuchAlgorithmException e) {
	            throw new HandshakeException(e.getMessage());
	        }
	    }

	    /**
	     * Gets security key string representation, which includes chars and spaces.
	     *
	     * @return Security key string representation, which includes chars and spaces.
	     */
	    public String getSecKey() {
	        return secKey;
	    }

	    @Override
	    public String toString() {
	        return secKey;
	    }

	    public byte[] getBytes() {
	        if(bytes == null) {
	            bytes = Base64Utils.decode(secKey);
	        }
	        return bytes;
	    }

	    public void validateServerKey(String serverKey) {
	        final SecKey key = generateServerKey(this);
	        if(!key.getSecKey().equals(serverKey)) {
	            throw new HandshakeException("Server key returned does not match expected response");
	        }
	    }
}
