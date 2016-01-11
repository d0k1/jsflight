package com.focusit.jsflight.player.controller;

public class WebLookupController extends UIController
{
    private static final long serialVersionUID = 1L;
    private final static WebLookupController instance = new WebLookupController();

    public static WebLookupController getInstance()
    {
        return instance;
    }

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
        return "weblookup";
    }

}
