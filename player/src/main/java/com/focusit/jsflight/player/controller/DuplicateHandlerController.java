package com.focusit.jsflight.player.controller;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DuplicateHandlerController extends UIController
{
    private static final long serialVersionUID = 5900843806336550183L;

    private static final DuplicateHandlerController INSTANCE = new DuplicateHandlerController();

    public static DuplicateHandlerController getInstance()
    {
        return INSTANCE;
    }

    private String scriptFileName = "";
    private String scriptBody;

    public String getScriptBody()
    {
        return scriptBody;
    }

    public String getScriptFileName()
    {
        return scriptFileName;
    }

    @Override
    public void load(String file) throws Exception
    {
        ObjectInputStream inputStream = getInputStream(file);
        this.scriptFileName = (String)inputStream.readObject();
        this.scriptBody = (String)inputStream.readObject();
    }

    public void setScriptBody(String scriptBody)
    {
        this.scriptBody = scriptBody;
    }

    public void setScriptFileName(String scriptFileName)
    {
        this.scriptFileName = scriptFileName;
    }

    @Override
    public void store(String file) throws Exception
    {
        ObjectOutputStream outputStream = getOutputStream(file);
        outputStream.writeObject(scriptFileName);
        outputStream.writeObject(scriptBody);
    }

    @Override
    protected String getControllerDataFilename()
    {
        return "duplicateHandler";
    }

}
