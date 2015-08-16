package com.nice.czp.htmlsocket.push.ws;

public class Utf8DecodingError extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public Utf8DecodingError(String s) {
		super(s);
	}

	public Utf8DecodingError(String s, Throwable throwable) {
		super(s, throwable);
	}

	public Utf8DecodingError(Throwable throwable) {
		super(throwable);
	}

	public int getClosingCode() {
		return 1007;
	}
}
