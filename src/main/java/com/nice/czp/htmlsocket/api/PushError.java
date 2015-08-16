package com.nice.czp.htmlsocket.api;

/**
 * 
 * @author coder_czp@126.com-2015年8月8日
 * 
 */
public class PushError {

    public static final PushError IDERR = new PushError(600, "id or topic is null");
    public static final PushError TIMEOUT = new PushError(601, "connect timeout");
    public static final PushError RELOGIN = new PushError(602, "Have logged in");
    public static final PushError IDNOTNUMBER = new PushError(603, "id isn't a number");

    private int code;
    private String desc;

    private PushError(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    };

    @Override
    public String toString() {
        return String.format("{\"_serr\":true,\"code\":%s,\"desc\":\"%s\"}", code, desc);
    }

}
