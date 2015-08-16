package com.nice.czp.htmlsocket.push;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nice.czp.htmlsocket.api.ICodec;
import com.nice.czp.htmlsocket.api.IConnListener;
import com.nice.czp.htmlsocket.api.ISubscriber;
import com.nice.czp.htmlsocket.api.PushError;

public class PushContext {

    private static final Logger log = LoggerFactory.getLogger(PushContext.class);
    private CopyOnWriteArrayList<IConnListener> connListeners;
    private MessageCenter messageCenter;
    private PushSerConfig config;

    public PushContext(PushSerConfig config) {
        connListeners = new CopyOnWriteArrayList<IConnListener>();
        this.config = config;
    }

    public ICodec getCodec() {
        return config.getCodec();
    }

    public PushSerConfig getConfig() {
        return config;
    }

    public MessageCenter getMessageCenter() {
        return messageCenter;
    }

    public void setMessageCenter(MessageCenter messageCenter) {
        this.messageCenter = messageCenter;
    }

    public void addConnListener(IConnListener connListener) {
        connListeners.add(connListener);
    }

    public void removeConnListener(IConnListener connListener) {
        connListeners.remove(connListener);
    }

    public Collection<IConnListener> getConnListeners() {
        return Collections.unmodifiableList(connListeners);
    }

    public PushError beforeConn(ISubscriber sub, Map<String, String[]> params) {
        for (IConnListener conn : connListeners) {
            PushError error = conn.beforeConnect(sub, params);
            if (error != null) {
                log.info("{} terminate request", conn);
                return error;
            }
        }
        return null;
    }

}
