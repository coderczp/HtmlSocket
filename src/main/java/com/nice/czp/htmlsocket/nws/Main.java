package com.nice.czp.htmlsocket.nws;

/**
 * 
 * @author coder_czp@126.com-2015年8月12日
 * 
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Server ser = new Server(8080);
        ConnManager cmgr = ser.getConnManager();
        ser.addHandler(new LongPollServer(cmgr), "/poll/*");
        ser.start();
        ser.sync();
    }
}
