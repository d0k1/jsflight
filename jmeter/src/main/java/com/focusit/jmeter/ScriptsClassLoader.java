package com.focusit.jmeter;

import groovy.lang.GroovyClassLoader;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Object NO_CLASS_DEF_FOUND = new Object();
    private static final Map<String, Object> classes = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(ScriptsClassLoader.class);

    private final static boolean prefetchClasses = System.getProperty("prefetchClasses") != null;

    public ScriptsClassLoader(ClassLoader parent) {
        super(new GroovyClassLoader(parent));
    }

    public Class loadClass(String className) throws ClassNotFoundException {
        return findClass(className);
    }

    public Class findClass(String className) throws ClassNotFoundException {

        String dir = System.getProperty("cp");

        if(classes.get(className)!=null){
            if(classes.get(className)==NO_CLASS_DEF_FOUND) {
                throw new ClassNotFoundException();
            }
            return (Class) classes.get(className);
        }

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

        classes.put(className, NO_CLASS_DEF_FOUND);
        log.info("Loading "+className);

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
                            if (clazz.equals(className + ".class") || prefetchClasses) {
                                InputStream classstream = zipFile.getInputStream(entry);
                                byte classByte[];
                                classByte = IOUtils.toByteArray(classstream);
                                try
                                {
                                    result = defineClass(className, classByte, 0, classByte.length, null);

                                    log.info("Loaded " + className);

                                    classes.put(className, result);

                                    if (!prefetchClasses) {
                                        return result;
                                    }
                                } catch (Exception e) {
                                    log.error("Can't load "+className);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error(e.toString(), e);
            }
        }

        if(classes.get(className)==NO_CLASS_DEF_FOUND) {
            throw new ClassNotFoundException();
        }

        return (Class) classes.get(className);
     }
}
