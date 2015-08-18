package com.nice.czp.htmlsocket.push.ws.util;

import java.security.SecureRandom;

import org.glassfish.grizzly.Buffer;

import com.nice.czp.htmlsocket.push.ws.itf.IWebsocket;


public class Masker {
	private Buffer buffer;
	private byte[] mask;
	private int index = 0;

	public Masker(Buffer buffer) {
		this.buffer = buffer;
	}

	public Masker() {
		generateMask();
	}

	public byte get() {
		return buffer.get();
	}

	public byte[] get(final int size) {
		byte[] bytes = new byte[size];
		buffer.get(bytes);
		return bytes;
	}

	public byte unmask() {
		final byte b = get();
		return mask == null ? b : (byte) (b ^ mask[index++
				% IWebsocket.MASK_SIZE]);
	}

	public byte[] unmask(int count) {
		byte[] bytes = get(count);
		if (mask != null) {
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] ^= mask[index++ % IWebsocket.MASK_SIZE];
			}
		}

		return bytes;
	}

	public void generateMask() {
		mask = new byte[IWebsocket.MASK_SIZE];
		new SecureRandom().nextBytes(mask);
	}

	public void mask(byte[] bytes, int location, byte b) {
		bytes[location] = mask == null ? b : (byte) (b ^ mask[index++
				% IWebsocket.MASK_SIZE]);
	}

	public void mask(byte[] target, int location, byte[] bytes) {
		if (bytes != null && target != null) {
			for (int i = 0; i < bytes.length; i++) {
				target[location + i] = mask == null ? bytes[i]
						: (byte) (bytes[i] ^ mask[index++ % IWebsocket.MASK_SIZE]);
			}
		}
	}

	public byte[] maskAndPrepend(byte[] packet) {
		byte[] masked = new byte[packet.length + IWebsocket.MASK_SIZE];
		System.arraycopy(getMask(), 0, masked, 0, IWebsocket.MASK_SIZE);
		mask(masked, IWebsocket.MASK_SIZE, packet);
		return masked;
	}

	public void setBuffer(Buffer buffer) {
		this.buffer = buffer;
	}

	public byte[] getMask() {
		return mask;
	}

	public void readMask() {
		mask = get(IWebsocket.MASK_SIZE);
	}
}
