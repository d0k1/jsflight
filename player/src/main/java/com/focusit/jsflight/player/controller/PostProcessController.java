package com.focusit.jsflight.player.controller;

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

}
