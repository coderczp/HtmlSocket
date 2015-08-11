package com.nice.czp.htmlsocket.demo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtil {

    public static void close(Closeable ... ios) {
        for (Closeable io : ios) {
            try {
                if (io != null)
                    io.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String readAsString(InputStream is) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            copy(is, out);
            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void copy(String src, String dest) throws Exception {
        FileInputStream fis = new FileInputStream(src);
        FileOutputStream fos = new FileOutputStream(dest);
        fos.getChannel().transferFrom(fis.getChannel(), 0, fis.available());
        close(fis, fos);
    }

    public static void copy(InputStream is, OutputStream os) throws IOException {
        copyNotClose(is, os);
        close(is, os);
    }

    public static void copyNotClose(InputStream is, OutputStream os) throws IOException {
        int read;
        byte[] buf = new byte[1024];
        BufferedInputStream fis = new BufferedInputStream(is);
        BufferedOutputStream fos = new BufferedOutputStream(os);
        while ((read = fis.read(buf)) != -1) {
            fos.write(buf, 0, read);
        }
        fos.flush();
    }
}
