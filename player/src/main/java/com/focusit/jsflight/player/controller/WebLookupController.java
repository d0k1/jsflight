package com.focusit.jsflight.player.controller;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class WebLookupController extends UIController
{
    private static final long serialVersionUID = 1L;
    private final static WebLookupController instance = new WebLookupController();

    public static WebLookupController getInstance()
    {
        return instance;
    }

    private String filename = "";
    private String script;

    private WebLookupController()
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
        setFilename((String)stream.readObject());
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
        stream.writeObject(getFilename());
    }

    @Override
    protected String getControllerDataFilename()
    {
        return "weblookup";
    }

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

}
