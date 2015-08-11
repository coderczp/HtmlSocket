package com.nice.czp.htmlsocket.api;

/**
 * 
 * @author coder_czp@126.com-2015年8月8日
 * 
 */
public class WSError {

    public static final WSError IDERR = new WSError(600, "i dor topic is null");
    public static final WSError TIMEOUT = new WSError(601, "connect timeout");
    public static final WSError RELOGIN = new WSError(602, "Have logged in");

    private int code;
    private String desc;

    private WSError(int code, String desc) {
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
