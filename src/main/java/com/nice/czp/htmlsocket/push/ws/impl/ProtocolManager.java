package com.nice.czp.htmlsocket.push.ws.impl;

import java.util.concurrent.CopyOnWriteArrayList;

import com.nice.czp.htmlsocket.push.ws.err.ProtocolError;
import com.nice.czp.htmlsocket.push.ws.itf.IWebsocket;
import com.nice.czp.htmlsocket.push.ws.itf.IWSCodec;

/**
 * 
 * @author coder_czp@126.com-2015年8月18日
 * 
 */
public class ProtocolManager {

    private static final ProtocolManager INSTANCE = new ProtocolManager();
    private CopyOnWriteArrayList<IWSCodec> codecs = new CopyOnWriteArrayList<IWSCodec>();

    private ProtocolManager() {

    }

    public static ProtocolManager getInstance() {
        return INSTANCE;
    }

    public boolean addCodec(IWSCodec codec) {
        return codecs.add(codec);
    }

    public boolean removeCodec(IWSCodec codec) {
        return codecs.remove(codec);
    }

    public IWSCodec getCodec(String version) {
        if (version != null) {
            for (IWSCodec wsCodec : codecs) {
                if (wsCodec.getVersion().equals(version))
                    return wsCodec;
            }
        }
        throw new ProtocolError(IWebsocket.PROTOCOL, "unsupport websocket version:" + version);
    }
}
