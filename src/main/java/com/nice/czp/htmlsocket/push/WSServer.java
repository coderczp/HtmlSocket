package com.nice.czp.htmlsocket.push;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class WSServer {

	public static void main(String[] args) throws IOException {
		ServerSocket ser = new ServerSocket(8878);
		while (!Thread.interrupted()) {
			Socket accept = ser.accept();
			InputStream in = accept.getInputStream();
			byte[] buf = new byte[1024];
			while (in.read(buf) != -1) {
				System.out.println(new String(buf));
			}
		}
		ser.close();
	}
}
