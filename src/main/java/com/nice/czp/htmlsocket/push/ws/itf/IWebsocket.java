package com.nice.czp.htmlsocket.push.ws.itf;

import java.util.Map;

public interface IWebsocket {
	/**
	 * 1000 indicates a normal closure, meaning that the purpose for which the
	 * connection was established has been fulfilled.
	 * <p>
	 * See <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455,
	 * Section 7.4.1 Defined Status Codes</a>.
	 */
	public final static int NORMAL = 1000;

	/**
	 * 1001 indicates that an endpoint is "going away", such as a server going
	 * down or a browser having navigated away from a page.
	 * <p>
	 * See <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455,
	 * Section 7.4.1 Defined Status Codes</a>.
	 */
	public final static int SHUTDOWN = 1001;

	/**
	 * 1002 indicates that an endpoint is terminating the connection due to a
	 * protocol error.
	 * <p>
	 * See <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455,
	 * Section 7.4.1 Defined Status Codes</a>.
	 */
	public final static int PROTOCOL = 1002;

	/**
	 * 1003 indicates that an endpoint is terminating the connection because it
	 * has received a type of data it cannot accept (e.g., an endpoint that
	 * understands only text data MAY send this if it receives a binary
	 * message).
	 * <p>
	 * See <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455,
	 * Section 7.4.1 Defined Status Codes</a>.
	 */
	public final static int BAD_DATA = 1003;

	/**
	 * Reserved. The specific meaning might be defined in the future.
	 * <p>
	 * See <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455,
	 * Section 7.4.1 Defined Status Codes</a>.
	 */
	public final static int UNDEFINED = 1004;

	/**
	 * 1005 is a reserved value and MUST NOT be set as a status code in a Close
	 * control frame by an endpoint. It is designated for use in applications
	 * expecting a status code to indicate that no status code was actually
	 * present.
	 * <p>
	 * See <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455,
	 * Section 7.4.1 Defined Status Codes</a>.
	 */
	public final static int NO_CODE = 1005;

	/**
	 * 1006 is a reserved value and MUST NOT be set as a status code in a Close
	 * control frame by an endpoint. It is designated for use in applications
	 * expecting a status code to indicate that the connection was closed
	 * abnormally, e.g., without sending or receiving a Close control frame.
	 * <p>
	 * See <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455,
	 * Section 7.4.1 Defined Status Codes</a>.
	 */
	public final static int NO_CLOSE = 1006;

	/**
	 * 1007 indicates that an endpoint is terminating the connection because it
	 * has received data within a message that was not consistent with the type
	 * of the message (e.g., non-UTF-8 [<a
	 * href="https://tools.ietf.org/html/rfc3629">RFC3629</a>] data within a
	 * text message).
	 * <p>
	 * See <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455,
	 * Section 7.4.1 Defined Status Codes</a>.
	 */
	public final static int BAD_PAYLOAD = 1007;

	/**
	 * 1008 indicates that an endpoint is terminating the connection because it
	 * has received a message that violates its policy. This is a generic status
	 * code that can be returned when there is no other more suitable status
	 * code (e.g., 1003 or 1009) or if there is a need to hide specific details
	 * about the policy.
	 * <p>
	 * See <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455,
	 * Section 7.4.1 Defined Status Codes</a>.
	 */
	public final static int POLICY_VIOLATION = 1008;

	/**
	 * 1009 indicates that an endpoint is terminating the connection because it
	 * has received a message that is too big for it to process.
	 * <p>
	 * See <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455,
	 * Section 7.4.1 Defined Status Codes</a>.
	 */
	public final static int MSG_TOO_LARGE = 1009;

	/**
	 * 1010 indicates that an endpoint (client) is terminating the connection
	 * because it has expected the server to negotiate one or more extension,
	 * but the server didn't return them in the response message of the
	 * WebSocket handshake. The list of extensions that are needed SHOULD appear
	 * in the /reason/ part of the Close frame. Note that this status code is
	 * not used by the server, because it can fail the WebSocket handshake
	 * instead.
	 * <p>
	 * See <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455,
	 * Section 7.4.1 Defined Status Codes</a>.
	 */
	public final static int REQUIRED_EXTENSION = 1010;

	/**
	 * 1011 indicates that a server is terminating the connection because it
	 * encountered an unexpected condition that prevented it from fulfilling the
	 * request.
	 * <p>
	 * See <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455,
	 * Section 7.4.1 Defined Status Codes</a>.
	 */
	public final static int SERVER_ERROR = 1011;

	/**
	 * 1012 indicates that the service is restarted. a client may reconnect, and
	 * if it chooses to do, should reconnect using a randomized delay of 5 -
	 * 30s.
	 * <p>
	 * See <a
	 * href="https://www.ietf.org/mail-archive/web/hybi/current/msg09649.html"
	 * >[hybi] Additional WebSocket Close Error Codes</a>
	 */
	public final static int SERVICE_RESTART = 1012;

	/**
	 * 1013 indicates that the service is experiencing overload. a client should
	 * only connect to a different IP (when there are multiple for the target)
	 * or reconnect to the same IP upon user action.
	 * <p>
	 * See <a
	 * href="https://www.ietf.org/mail-archive/web/hybi/current/msg09649.html"
	 * >[hybi] Additional WebSocket Close Error Codes</a>
	 */
	public final static int TRY_AGAIN_LATER = 1013;

	/**
	 * 1015 is a reserved value and MUST NOT be set as a status code in a Close
	 * control frame by an endpoint. It is designated for use in applications
	 * expecting a status code to indicate that the connection was closed due to
	 * a failure to perform a TLS handshake (e.g., the server certificate can't
	 * be verified).
	 * <p>
	 * See <a href="https://tools.ietf.org/html/rfc6455#section-7.4.1">RFC 6455,
	 * Section 7.4.1 Defined Status Codes</a>.
	 */
	public final static int FAILED_TLS_HANDSHAKE = 1015;

	public static final String SERVER_KEY_HASH = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	public static final String SEC_WS_KEY_HEADER = "Sec-WebSocket-Key";
	public static final String SEC_WS_VERSION = "Sec-WebSocket-Version";
	public static final String SERVER_NAME = "HtmlSocket Server 1.0";
	public static final int MASK_SIZE = 4;

	public IWSCodec getCodec();

	public void send(Object message);

	public String getRemoteAddress();

	public void close(int code, String info);

	public void onDisconnect(int code, String info);

	public void onConnected(Map<String, String[]> params);

	public void dipatchMessage(WSMessage message);

	public boolean addListener(WSListener listener);

}
