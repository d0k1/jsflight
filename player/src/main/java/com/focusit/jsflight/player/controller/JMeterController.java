package com.focusit.jsflight.player.controller;

public class JMeterController extends UIController
{
    private static final long serialVersionUID = 1L;
    private final static JMeterController instance = new JMeterController();

    public static JMeterController getInstance()
    {
        return instance;
    }

    private JMeterController()
    {
    }

    @Override
    public void load(String file) throws Exception
    {
    }

    @Override
    public void store(String file) throws Exception
    {
    }

    @Override
    protected String getControllerDataFilename()
    {
        return null;
    }

}
