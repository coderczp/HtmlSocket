package com.nice.czp.htmlsocket.js;

import com.nice.czp.htmlsocket.api.ICodec;
import com.nice.czp.htmlsocket.api.IMessage;
import com.nice.czp.htmlsocket.api.ISubscriber;
import com.nice.czp.htmlsocket.push.MessageCenter;
import com.nice.czp.htmlsocket.push.PullServer;
import com.nice.czp.htmlsocket.push.PushSerConfig;

/**
 * HtmlSocket sever 启动类
 */
public class HtmlSocketServlet {

    private static MessageCenter messageCenter;

    public static void main(String[] args) {
        try {
            ICodec codec = null;// Instead of your code.
            String portStr = args[0];
            int port = Integer.valueOf(portStr);
            PullServer ser = new PullServer(new PushSerConfig(port, codec));
            messageCenter = ser.getContext().getMessageCenter();
            ser.start();
        } catch (Exception e) {
            System.out.println("fail to start htmlsocket server," + e);
        }
    }

    public void sendMessage() {
        IMessage msg = null;// Instead of your code.
        messageCenter.sendTo(100, msg);
        messageCenter.broadcastGroup("demo", msg);
        messageCenter.broadcastAll(msg);
    }

    public void addMySubscriber() {
        messageCenter.addSubscriber(new ISubscriber() {

            @Override
            public void onMessage(IMessage message) {
                System.out.println(message);
            }

            @Override
            public String getTopic() {
                return "demo";
            }

            @Override
            public long getId() {
                return 1000;
            }
        });
    }

}
