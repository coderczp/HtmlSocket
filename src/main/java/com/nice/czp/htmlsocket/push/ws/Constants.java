package com.nice.czp.htmlsocket.push.ws;

public class Constants {
	public static final String SEC_WS_ACCEPT = "Sec-WebSocket-Accept";
	public static final String SEC_WS_KEY_HEADER = "Sec-WebSocket-Key";
	public static final String SEC_WS_ORIGIN_HEADER = "Sec-WebSocket-Origin";
	public static final String ORIGIN_HEADER = "Origin";
	public static final String SEC_WS_PROTOCOL_HEADER = "Sec-WebSocket-Protocol";
	public static final String SEC_WS_EXTENSIONS_HEADER = "Sec-WebSocket-Extensions";
	public static final String SEC_WS_VERSION = "Sec-WebSocket-Version";
	public static final String WEBSOCKET = "websocket";
	public static final String RESPONSE_CODE_MESSAGE = "Switching Protocols";
	public static final String RESPONSE_CODE_HEADER = "Response Code";
	public static final int RESPONSE_CODE_VALUE = 101;
	public static final String UPGRADE = "upgrade";
	public static final String CONNECTION = "connection";
	public static final String CLIENT_WS_ORIGIN_HEADER = "Origin";
	public static final String SERVER_KEY_HASH = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	public static final int MASK_SIZE = 4;
}
