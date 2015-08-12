package com.nice.czp.htmlsocket.nws;


import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

/**
 * 
 * @author coder_czp@126.com-2015年8月12日
 * 
 */
public class LongPollServer extends HttpHandler {

    private ConnManager connmagr;

    public LongPollServer(ConnManager connmagr) {
        this.connmagr = connmagr;
    }

    @Override
    public void service(final Request request, Response response) throws Exception {
        connmagr.addConn(new RemoteConn() {

            @Override
            public void onClose() {
              System.out.println("---------->");
            }

            @Override
            public String getAddress() {
                return request.getRemoteAddr();
            }
        });
        response.suspend();
        response.getWriter().write("ok");
    }

}
