package com.nice.czp.htmlsocket.nws;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.ConnectionProbe.Adapter;

/**
 * TODO Please Descrip This Class
 * 
 * @author coder_czp@126.com-2015年8月12日
 * 
 */
public class ConnManager extends Adapter {

    private Map<String, RemoteConn> conns = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("rawtypes")
    public void onCloseEvent(Connection connection) {
        Object peerAddress = connection.getPeerAddress();
        if (peerAddress != null) {
            System.out.println(peerAddress + " is closed");
            RemoteConn conn = conns.get(peerAddress.toString());
            if (conn != null) {
                conn.onClose();
            }
        }
    }

    public void addConn(RemoteConn conn) {
        conns.put(conn.getAddress(), conn);
    }
}
