package com.focusit.jsflight.player.controller;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ScriptEventExectutionController extends UIController
{
    private static final long serialVersionUID = 8326565501493347454L;

    private static final ScriptEventExectutionController INSTANCE = new ScriptEventExectutionController();

    public static ScriptEventExectutionController getInstance()
    {
        return INSTANCE;
    }

    private String filename = "";

    private String script;

    public String getFilename()
    {
        return filename;
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

    public void setFilename(String filename)
    {
        this.filename = filename;
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
        return "scriptEventExecution";
    }

}
