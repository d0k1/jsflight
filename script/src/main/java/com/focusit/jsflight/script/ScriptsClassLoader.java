package com.focusit.jsflight.script;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by dkirpichenkov on 01.04.16.
 */
public class ScriptsClassLoader extends URLClassLoader
{
    public ScriptsClassLoader(ClassLoader parent, URL... urls)
    {
        super(urls, parent);
    }
}
