package com.focusit.jmeter;

import groovy.lang.GroovyClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Map<String, Class> classes = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(ScriptsClassLoader.class);

    public ScriptsClassLoader(ClassLoader parent) {
        super(new GroovyClassLoader(parent));
    }

    public Class loadClass(String className) throws ClassNotFoundException {
        return findClass(className);
    }

    public Class findClass(String className) throws ClassNotFoundException {

        String dir = System.getProperty("cp");

        if(classes.get(className)!=null){
            return classes.get(className);
        }

        //log.info("Loading "+className);

        Class result = null;
        try {
            result = getParent().loadClass(className);
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
        }

        if(result!=null) {
            classes.put(className, result);
            return result;
        }

        if(dir!=null && dir.trim().length()>0) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(FileSystems.getDefault().getPath(dir), "*.jar")) {
                for (Path entry1 : stream) {
                    String filejar = entry1.toAbsolutePath().toString();
                    ZipFile zipFile = new ZipFile(filejar);
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                            // This ZipEntry represents a class. Now, what class does it represent?
                            String clazz = entry.getName().replace('/', '.'); // including ".class"
                            if (clazz.equals(className + ".class")) {
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

                                log.info("Loaded "+className);

                                classes.put(className, result);
                                return result;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                log.error(e.toString(), e);
            }
        }
        throw new ClassNotFoundException();
     }
}
