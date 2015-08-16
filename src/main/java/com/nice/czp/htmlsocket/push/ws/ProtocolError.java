package com.nice.czp.htmlsocket.push.ws;


public class ProtocolError extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public ProtocolError(String s) {
        super(s);
    }

    public ProtocolError(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ProtocolError(Throwable throwable) {
        super(throwable);
    }

    public int getClosingCode() {
        return 1002;
    }

}
