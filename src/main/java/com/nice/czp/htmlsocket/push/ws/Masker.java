package com.nice.czp.htmlsocket.push.ws;

import java.security.SecureRandom;

import org.glassfish.grizzly.Buffer;

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
				% Constants.MASK_SIZE]);
	}

	public byte[] unmask(int count) {
		byte[] bytes = get(count);
		if (mask != null) {
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] ^= mask[index++ % Constants.MASK_SIZE];
			}
		}

		return bytes;
	}

	public void generateMask() {
		mask = new byte[Constants.MASK_SIZE];
		new SecureRandom().nextBytes(mask);
	}

	public void mask(byte[] bytes, int location, byte b) {
		bytes[location] = mask == null ? b : (byte) (b ^ mask[index++
				% Constants.MASK_SIZE]);
	}

	public void mask(byte[] target, int location, byte[] bytes) {
		if (bytes != null && target != null) {
			for (int i = 0; i < bytes.length; i++) {
				target[location + i] = mask == null ? bytes[i]
						: (byte) (bytes[i] ^ mask[index++ % Constants.MASK_SIZE]);
			}
		}
	}

	public byte[] maskAndPrepend(byte[] packet) {
		byte[] masked = new byte[packet.length + Constants.MASK_SIZE];
		System.arraycopy(getMask(), 0, masked, 0, Constants.MASK_SIZE);
		mask(masked, Constants.MASK_SIZE, packet);
		return masked;
	}

	public void setBuffer(Buffer buffer) {
		this.buffer = buffer;
	}

	public byte[] getMask() {
		return mask;
	}

	public void readMask() {
		mask = get(Constants.MASK_SIZE);
	}
}
