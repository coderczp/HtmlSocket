package com.nice.czp.htmlsocket.demo;

import java.io.File;

import com.nice.czp.htmlsocket.ws.JSonMessage;
import com.nice.czp.htmlsocket.ws.ServerConfig;
import com.nice.czp.htmlsocket.ws.WSSever;

public class APPMain {

    public static void main(String[] args) throws Exception {
        System.out.println("usage: java -jar htmlsocket.jar port[8877]");
        int port = 8877;
        if (args.length == 1) {
            port = Integer.valueOf(args[0]);
        }
        String outPath = "./web";
        new File(outPath).mkdir();
        String jarPath = JarUtil.getCurrentJarFile();
        String[] filter = new String[] { ".html", ".mini.js", "HtmlSocketServlet.java" };
        JarUtil.decompressJar(jarPath, outPath, filter);
        ServerConfig config = new ServerConfig(port, new JSonMessage());
        // config.setResourceBase("src/main/resources");
        config.setResourceBase(outPath);
        config.setUseMappedbuf(false);
        startWSSer(config);
    }

    private static void startWSSer(ServerConfig config) throws Exception {
        WSSever wsSever = new WSSever(config);
        wsSever.start();
        Thread.currentThread().join();
    }

}
