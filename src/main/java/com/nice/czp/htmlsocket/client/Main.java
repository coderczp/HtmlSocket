package com.nice.czp.htmlsocket.client;

import java.util.Map;
import java.util.Scanner;

import com.nice.czp.htmlsocket.push.ws.itf.IWebsocket;
import com.nice.czp.htmlsocket.push.ws.itf.WSListener;

/**
 * TODO Please Descrip This Class
 * 
 * @author coder_czp@126.com-2015年8月19日
 * 
 */
public class Main implements WSListener {

    public static void main(String[] args) throws Exception {
        try {
            // String url =
            // "http://127.0.0.1:8877/socket.io/?EIO=3&transport=websocket&&t=1439979242559-2";
            String url = "http://127.0.0.1:8877/ws/?id=13&topic=test";
            WSClient client = new WSClient(url);
            client.addListener(new Main());
            client.connect();
            String line;
            Scanner sc = new Scanner(System.in);
            while (!"quit".equals((line = sc.nextLine()))) {
                String msg = String.format("{\"to\":\"13\",\"from\":\"123\",\"data\":\"%s\"}", line);
                client.send(msg);
                System.out.println("enter message");
            }
            sc.close();
            client.close(WSClient.NORMAL, "normal closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWSMessage(byte[] message, boolean isText) {
        System.out.println(new String(message));
    }

    @Override
    public void onClosed(IWebsocket websocket, int code, String info) {
        System.out.println(info);
    }

    @Override
    public void onConnected(IWebsocket websocket, Map<String, String[]> params) {

    }

}
