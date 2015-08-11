package com.nice.czp.htmlsocket.demo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarUtil {

    public static List<String> decompressJar(String jarPath, String outPath, String[] filter) throws Exception {
        JarFile jarFile = new JarFile(jarPath);
        List<String> webWars = new ArrayList<String>();
        Enumeration<JarEntry> entrys = jarFile.entries();
        while (entrys.hasMoreElements()) {
            JarEntry entry = entrys.nextElement();
            String name = entry.getName();
            if (name.contains("about.html"))
                continue;
            for (String string : filter) {
                if (name.contains(string)) {
                    String file = extractJarEntry(outPath, jarFile, entry);
                    webWars.add(file);
                }
            }
        }
        jarFile.close();
        return webWars;
    }

    public static String extractJarEntry(String outPath, JarFile jarFile, JarEntry entry) throws Exception {
        outPath = outPath.endsWith("/") ? outPath : outPath + '/';
        File file = new File(outPath + entry.getName());
        if (!file.exists()) {
            if (entry.isDirectory()) {
                file.mkdirs();
            } else {
                file.getParentFile().mkdirs();
                InputStream is = jarFile.getInputStream(entry);
                IOUtil.copy(is, new FileOutputStream(file));
            }
        }
        return file.getCanonicalPath();

    }

    public static String getCurrentJarFile() {
        String clsName = JarUtil.class.getName().replace('.', '/');
        ClassLoader cl = JarUtil.class.getClassLoader();
        URL file = cl.getResource(clsName + ".class");
        String urlStr = file.toString();
        int from = "jar:file:/".length();
        int to = urlStr.indexOf("!/");
        return urlStr.substring(from, to);
    }
}
