package com.nice.czp.htmlsocket.push.ws;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpHeader;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.HttpResponsePacket;
import org.glassfish.grizzly.http.Protocol;
import org.glassfish.grizzly.http.util.HttpStatus;

final class RFC6455 implements WebsocketImpl {

	@SuppressWarnings("rawtypes")
	private Connection connection;
	private final ParsingState state = new ParsingState();
	private byte inFragmentedType;
	private boolean processingFragment;
	private Charset utf8 = new StrictUtf8();
	private CharsetDecoder currentDecoder = utf8.newDecoder();
	private ByteBuffer remainder;
	String pubKey = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	private HashMap<String, Object> ext = new HashMap<String, Object>();
	SecKey secKey = new SecKey();

	public RFC6455(FilterChainContext ctx2) {
		connection = ctx2.getConnection();
	}

	@Override
	public void send(byte[] data) {

	}

	@Override
	public String getVersion() {
		return "13";
	}

	@Override
	public void doHandShake(HttpHeader header, FilterChainContext ctx) {
		try {
			String wsKey = header.getHeader("Sec-WebSocket-Key");
			secKey = SecKey.generateServerKey(new SecKey(wsKey));
			HttpResponsePacket rep = ((HttpRequestPacket) header).getResponse();
			rep.setProtocol(Protocol.HTTP_1_1);
			rep.setStatus(HttpStatus.SWITCHING_PROTOCOLS_101);
			rep.setHeader("Upgrade", "websocket");
			rep.setHeader("Connection", "Upgrade");
			rep.setHeader("Server", "htmlsocket server");
			rep.setHeader("Date", "Mon, 26 Nov 2015 23:42:44 GMT");
			rep.setHeader("Access-Control-Allow-Credentials", "true");
			rep.setHeader("Sec-WebSocket-Accept", secKey.getSecKey());
			ctx.write(HttpContent.builder(rep).build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void cose() {

	}

	@Override
	public Map<String, Object> getExt() {
		return ext;
	}

	public long decodeLength(byte[] bytes) {
		return Utils.toLong(bytes, 0, bytes.length);
	}

	@Override
	public DataFrame parseFrame(Buffer buffer) {
		DataFrame dataFrame;
		try {
			switch (state.state) {
			case 0:
				if (buffer.remaining() < 2) {
					// Don't have enough bytes to read opcode and lengthCode
					return null;
				}
				byte opcode = buffer.get();
				boolean rsvBitSet = isBitSet(opcode, 6) || isBitSet(opcode, 5)
						|| isBitSet(opcode, 4);
				if (rsvBitSet) {
					throw new ProtocolError("RSV bit(s) incorrectly set.");
				}
				state.finalFragment = isBitSet(opcode, 7);
				state.controlFrame = isControlFrame(opcode);
				state.opcode = (byte) (opcode & 0x7f);
				state.frameType = valueOf(inFragmentedType, state.opcode);
				if (!state.finalFragment && state.controlFrame) {
					throw new ProtocolError("Fragmented control frame");
				}

				if (!state.controlFrame) {
					if (isContinuationFrame(state.opcode)
							&& !processingFragment) {
						throw new ProtocolError(
								"End fragment sent, but wasn't processing any previous fragments");
					}
					if (processingFragment
							&& !isContinuationFrame(state.opcode)) {
						throw new ProtocolError(
								"Fragment sent but opcode was not 0");
					}
					if (!state.finalFragment
							&& !isContinuationFrame(state.opcode)) {
						processingFragment = true;
					}
					if (!state.finalFragment) {
						if (inFragmentedType == 0) {
							inFragmentedType = state.opcode;
						}
					}
				}
				byte lengthCode = buffer.get();
				state.masked = (lengthCode & 0x80) == 0x80;
				state.masker = new Masker(buffer);
				if (state.masked) {
					lengthCode ^= 0x80;
				}
				state.lengthCode = lengthCode;

				state.state++;

			case 1:
				if (state.lengthCode <= 125) {
					state.length = state.lengthCode;
				} else {
					if (state.controlFrame) {
						throw new ProtocolError(
								"Control frame payloads must be no greater than 125 bytes.");
					}

					final int lengthBytes = state.lengthCode == 126 ? 2 : 8;
					if (buffer.remaining() < lengthBytes) {
						// Don't have enought bytes to read length
						return null;
					}
					state.masker.setBuffer(buffer);
					state.length = decodeLength(state.masker
							.unmask(lengthBytes));
				}
				state.state++;
			case 2:
				if (state.masked) {
					if (buffer.remaining() < Constants.MASK_SIZE) {
						// Don't have enough bytes to read mask
						return null;
					}
					state.masker.setBuffer(buffer);
					state.masker.readMask();
				}
				state.state++;
			case 3:
				if (buffer.remaining() < state.length) {
					return null;
				}

				state.masker.setBuffer(buffer);
				final byte[] data = state.masker.unmask((int) state.length);
				if (data.length != state.length) {
					throw new ProtocolError(
							String.format("Data read (%s) is not the expected"
									+ " size (%s)", data.length, state.length));
				}
				dataFrame = state.frameType.create(state.finalFragment, data);

				if (!state.controlFrame
						&& (isTextFrame(state.opcode) || inFragmentedType == 1)) {
					utf8Decode(state.finalFragment, data, dataFrame);
				}

				if (!state.controlFrame && state.finalFragment) {
					inFragmentedType = 0;
					processingFragment = false;
				}
				state.recycle();

				break;
			default:
				// Should never get here
				throw new IllegalStateException("Unexpected state: "
						+ state.state);
			}
		} catch (Exception e) {
			state.recycle();
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		}

		return dataFrame;

	}

	@Override
	public void onMessage(DataFrame result) {
		System.out.println(result.getTextPayload());

	}

	protected ByteBuffer getByteBuffer(final byte[] data) {
		if (remainder == null) {
			return ByteBuffer.wrap(data);
		} else {
			final int rem = remainder.remaining();
			final byte[] orig = remainder.array();
			byte[] b = new byte[rem + data.length];
			System.arraycopy(orig, orig.length - rem, b, 0, rem);
			System.arraycopy(data, 0, b, rem, data.length);
			remainder = null;
			return ByteBuffer.wrap(b);
		}
	}

	protected void utf8Decode(boolean finalFragment, byte[] data,
			DataFrame dataFrame) {
		final ByteBuffer b = getByteBuffer(data);
		int n = (int) (b.remaining() * currentDecoder.averageCharsPerByte());
		CharBuffer cb = CharBuffer.allocate(n);
		for (;;) {
			CoderResult result = currentDecoder.decode(b, cb, finalFragment);
			if (result.isUnderflow()) {
				if (finalFragment) {
					currentDecoder.flush(cb);
					if (b.hasRemaining()) {
						throw new IllegalStateException(
								"Final UTF-8 fragment received, but not all bytes consumed by decode process");
					}
					currentDecoder.reset();
				} else {
					if (b.hasRemaining()) {
						remainder = b;
					}
				}
				cb.flip();
				String res = cb.toString();
				dataFrame.setPayload(res);
				dataFrame.setPayload(Utf8Utils.encode(new StrictUtf8(), res));
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

	protected boolean isControlFrame(byte opcode) {
		return (opcode & 0x08) == 0x08;
	}

	private boolean isBitSet(final byte b, int bit) {
		return ((b >> bit & 1) != 0);
	}

	private boolean isContinuationFrame(byte opcode) {
		return opcode == 0;
	}

	private boolean isTextFrame(byte opcode) {
		return opcode == 1;
	}

	private byte getOpcode(FrameType type) {
		if (type instanceof TextFrameType) {
			return 0x01;
		} else if (type instanceof BinaryFrameType) {
			return 0x02;
		} else if (type instanceof ClosingFrameType) {
			return 0x08;
		} else if (type instanceof PingFrameType) {
			return 0x09;
		} else if (type instanceof PongFrameType) {
			return 0x0A;
		}

		throw new ProtocolError("Unknown frame type: "
				+ type.getClass().getName());
	}

	private FrameType valueOf(byte fragmentType, byte value) {
		final int opcode = value & 0xF;
		switch (opcode) {
		case 0x00:
			return new ContinuationFrameType((fragmentType & 0x01) == 0x01);
		case 0x01:
			return new TextFrameType();
		case 0x02:
			return new BinaryFrameType();
		case 0x08:
			return new ClosingFrameType();
		case 0x09:
			return new PingFrameType();
		case 0x0A:
			return new PongFrameType();
		default:
			throw new ProtocolError(String.format("Unknown frame type: %s, %s",
					Integer.toHexString(opcode & 0xFF).toUpperCase(Locale.US),
					connection));
		}
	}

	private static class ParsingState {
		int state = 0;
		byte opcode = (byte) -1;
		long length = -1;
		FrameType frameType;
		boolean masked;
		Masker masker;
		boolean finalFragment;
		boolean controlFrame;
		private byte lengthCode = -1;

		void recycle() {
			state = 0;
			opcode = (byte) -1;
			length = -1;
			lengthCode = -1;
			masked = false;
			masker = null;
			finalFragment = false;
			controlFrame = false;
			frameType = null;
		}
	}
}