package com.focusit.jsflight.player.script;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by dkirpichenkov on 01.04.16.
 */
public class ScriptsClassLoader extends ClassLoader {
    private Map<String, Class> classes = new ConcurrentHashMap<>();

    public ScriptsClassLoader(ClassLoader parent) {
        super(parent);
    }

    public Class loadClass(String className) throws ClassNotFoundException {
        return findClass(className);
    }

    public Class findClass(String className) {

        String dir = System.getProperty("cp");

        if(classes.get(className)!=null){
            return classes.get(className);
        }

        Class result = null;
        try {
            result = getParent().loadClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(result!=null) {
            classes.put(className, result);
            return result;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(FileSystems.getDefault().getPath(dir), "*.jar")) {
            for (Path entry1: stream) {
                String filejar = entry1.toAbsolutePath().toString();
                ZipFile zipFile = new ZipFile(filejar);
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while(entries.hasMoreElements()){
                    ZipEntry entry = entries.nextElement();
                    if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                        // This ZipEntry represents a class. Now, what class does it represent?
                        String clazz = entry.getName().replace('/', '.'); // including ".class"
                        if(clazz.equals(className+".class")) {
                            InputStream classstream = zipFile.getInputStream(entry);
                            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                            int nextValue = classstream.read();
                            while (-1 != nextValue) {
                                byteStream.write(nextValue);
                                nextValue = classstream.read();
                            }
                            byte classByte[];
                            classByte = byteStream.toByteArray();
                            result = defineClass(className, classByte, 0, classByte.length, null);
                            classes.put(className, result);
                            return result;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
     }
}
