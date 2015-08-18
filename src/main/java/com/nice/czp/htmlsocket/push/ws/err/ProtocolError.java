package com.nice.czp.htmlsocket.push.ws.err;

/**
 * TODO Please Descrip This Class
 * 
 * @author coder_czp@126.com-2015年8月18日
 * 
 */
public class ProtocolError extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int code;
    private String message;

    public ProtocolError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ProtocolError [code=" + code + ", message=" + message + "]";
    }

}
