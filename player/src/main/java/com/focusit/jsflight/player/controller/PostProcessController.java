package com.focusit.jsflight.player.controller;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.focusit.jsflight.player.input.Events;
import com.focusit.jsflight.player.script.Engine;

public class PostProcessController extends UIController
{
    private static final long serialVersionUID = 1L;

    private final static PostProcessController instance = new PostProcessController();

    public static PostProcessController getInstance()
    {
        return instance;
    }

    private String script;

    private PostProcessController()
    {
    }

    public String getScript()
    {
        return script;
    }

    @Override
    public void load(String file) throws Exception
    {
        script = (String)getInputStream(file).readObject();
    }

    public void setScript(String script)
    {
        this.script = script;
    }

    @Override
    public void store(String file) throws Exception
    {
        getOutputStream(file).writeObject(script);
    }

    @Override
    protected String getControllerDataFilename()
    {
        return "postprocess";
    }

    public Events processEvents(Events e){
        Engine engine = new Engine(script);
        List<JSONObject> events = new ArrayList<>();
        if (e != null && e.getEvents() != null)
        {
            events = e.getEvents();
        }
        engine.testPostProcess(events);
        return e;        
    }
}
