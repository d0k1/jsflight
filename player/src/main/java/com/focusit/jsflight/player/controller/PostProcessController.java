package com.focusit.jsflight.player.controller;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PostProcessController extends UIController
{
    private static final long serialVersionUID = 1L;

    private final static PostProcessController instance = new PostProcessController();
    private String filename = "";
    private String script;
    private PostProcessController()
    {
    }

    public static PostProcessController getInstance()
    {
        return instance;
    }

    public String getScript()
    {
        return script;
    }

    public void setScript(String script)
    {
        this.script = script;
    }

    @Override
    public void load(String file) throws Exception
    {
    	ObjectInputStream stream = getInputStream(file);
        script = (String)stream.readObject();
        filename = (String)stream.readObject();
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

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}
