package com.nice.czp.htmlsocket.push.ws.itf;

import org.glassfish.grizzly.Buffer;

/**
 * TODO Please Descrip This Class
 * 
 * @author coder_czp@126.com-2015年8月18日
 * 
 */
public interface IWSCodec {

    public byte[] encode(WSMessage frame);

    public WSMessage decode(Buffer buffer);

    public String getVersion();
}
