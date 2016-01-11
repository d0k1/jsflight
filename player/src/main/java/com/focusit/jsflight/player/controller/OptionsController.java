package com.focusit.jsflight.player.controller;

public class OptionsController extends UIController
{
    private static final long serialVersionUID = 1L;

    private final static OptionsController instance = new OptionsController();

    public static OptionsController getInstance()
    {
        return instance;
    }

    private OptionsController()
    {
    }

    @Override
    public void load(String file)
    {
    }

    @Override
    public void store(String file)
    {
    }

    @Override
    protected String getControllerDataFilename()
    {
        return "options";
    }

}
