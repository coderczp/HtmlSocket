package com.nice.czp.htmlsocket.demo;

import com.nice.czp.htmlsocket.push.JSonMessage;
import com.nice.czp.htmlsocket.push.PullServer;
import com.nice.czp.htmlsocket.push.PushSerConfig;

public class APPMain {

    public static void main(String[] args) throws Exception {
        System.out.println("usage: java -jar htmlsocket.jar port[8877]");
        int port = 8877;
        if (args.length == 1) {
            port = Integer.valueOf(args[0]);
        }
        // String outPath = "./web";
        // new File(outPath).mkdir();
        // String jarPath = JarUtil.getCurrentJarFile();
        // String[] filter = new String[] { ".html", ".mini.js", "HtmlSocketServlet.java" };
        // JarUtil.decompressJar(jarPath, outPath, filter);
        PushSerConfig config = new PushSerConfig(port, new JSonMessage());
        config.setResourceBase("src/main/resources");
        startWSSer(config);
    }

    private static void startWSSer(PushSerConfig config) throws Exception {
        PullServer ser = new PullServer(config);
       // ser.addWebSocketHandler("/ws/*", new WebSocketServer(ser.getContext()));
        ser.start();
        ser.sync();
    }

}
