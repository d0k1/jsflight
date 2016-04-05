package com.focusit.script.jmeter;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Context to hold a http sample pre/post processing context.
 * Helps to modify requests with JMeter's context variables
 * Created by doki on 25.03.16.
 */
public class JMeterRecorderContext {
    private final static JMeterRecorderContext instance = new JMeterRecorderContext();

    private static final ConcurrentHashMap<String, Object> templateReplacemnts = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Object> properties = new ConcurrentHashMap<>();

    private JMeterRecorderContext(){

    }

    public static JMeterRecorderContext getInstance(){
        return instance;
    }

    public void addTemplate(String source, Object template){
        templateReplacemnts.put(source, template);
    }

    public Object getTemplate(String source) {
        return templateReplacemnts.get(source);
    }

    public void reset(){
        templateReplacemnts.clear();
    }

    public Collection<String> getSources(){
        return templateReplacemnts.keySet();
    }

    public void addProperty(String key, Object value){
        properties.put(key, value);
    }

    public Object getProperty(String key){
        return properties.get(key);
    }
}
