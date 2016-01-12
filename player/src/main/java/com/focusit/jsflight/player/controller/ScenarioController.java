package com.focusit.jsflight.player.controller;

public class ScenarioController extends UIController
{
    private final static ScenarioController instance = new ScenarioController();

    public static ScenarioController getInstance()
    {
        return instance;
    }

    private ScenarioController()
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
        return "scenario";
    }
}
