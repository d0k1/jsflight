package com.focusit.jsflight.player.controller;

import com.focusit.jsflight.player.input.Events;
import com.focusit.jsflight.player.script.Engine;
import org.json.JSONObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PostProcessController extends UIController
{
    private static final long serialVersionUID = 1L;

    private final static PostProcessController instance = new PostProcessController();

    public static PostProcessController getInstance()
    {
        return instance;
    }

    private String filename = "";
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
    	ObjectInputStream stream = getInputStream(file);
        script = (String)stream.readObject();
        filename = (String)stream.readObject();
    }

    public void setScript(String script)
    {
        this.script = script;
    }

    @Override
    public void store(String file) throws Exception
    {
    	ObjectOutputStream stream = getOutputStream(file);
        stream.writeObject(script);
        stream.writeObject(filename);
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

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}
