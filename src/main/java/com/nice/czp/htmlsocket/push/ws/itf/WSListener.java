package com.nice.czp.htmlsocket.push.ws.itf;

import java.util.Map;


/**
 * TODO Please Descrip This Class
 * 
 * @author coder_czp@126.com-2015年8月17日
 * 
 */
public interface WSListener{

    void onWSMessage(byte[] data,boolean isTextMessage);

    void onClosed(IWebsocket websocket,int code,String info);

    void onConnected(IWebsocket websocket, Map<String, String[]> params);

}
