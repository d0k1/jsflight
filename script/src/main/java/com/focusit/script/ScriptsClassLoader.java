package com.focusit.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dkirpichenkov on 01.04.16.
 */
public class ScriptsClassLoader extends URLClassLoader {
    private static final Map<String, Object> classes = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(ScriptsClassLoader.class);

    private static URL urls[];

    static {
        if(System.getProperty("cp")!=null){
            File f = new File(System.getProperty("cp"));
            if(f!=null) {
                int size = f.listFiles().length;
                urls = new URL[size];
                int i = 0;

                try {
                    for (File file : f.listFiles()) {
                        urls[i++] = file.toURI().toURL();
                    }
                } catch (MalformedURLException e) {
                    log.error(e.toString(), e);
                }
            }
        }
    }

    public ScriptsClassLoader(ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
       return super.findClass(name);
    }
}
