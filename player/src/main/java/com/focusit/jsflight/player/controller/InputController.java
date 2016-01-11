package com.focusit.jsflight.player.controller;


public class InputController extends UIController
{
    private static final long serialVersionUID = 1L;

    private final static InputController instance = new InputController();

    public static InputController getInstance()
    {
        return instance;
    }

    private String filename = "";

    private InputController()
    {
    }

    public String getFilename()
    {
        return filename;
    }

    @Override
    public void load(String file) throws Exception
    {
        filename = (String)getInputStream(file).readObject();
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    @Override
    public void store(String file) throws Exception
    {
        getOutputStream(file).writeObject(filename);
    }

    @Override
    protected String getControllerDataFilename()
    {
        return "input";
    }
}
