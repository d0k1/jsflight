package com.focusit.jsflight.player.context;

import java.util.concurrent.ConcurrentHashMap;
import groovy.lang.Binding;

public class PlayerContext
{
    private static final ConcurrentHashMap<String, Object> context = new ConcurrentHashMap<>();
    private static final PlayerContext instance = new PlayerContext();

    public static PlayerContext getInstance()
    {
        return instance;
    }

    public Object get(String key)
    {
        return context.get(key);
    }

    public void put(String key, Object value)
    {
        context.put(key, value);
    }

    public void reset()
    {
        context.keySet().forEach(it -> {
            context.remove(it);
        });
    }
    
    public Binding asBindings(){
    	Binding binding = new Binding(context);
    	return binding;
    }
}
