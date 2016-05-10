package com.focusit.jsflight.player.controller;

import com.focusit.jsflight.player.config.CommonConfiguration;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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

    private CommonConfiguration configuration;

    @Override
    public void load(String file) throws Exception
    {
        ObjectInputStream stream = getInputStream(file);
        configuration.setProxyHost((String)stream.readObject());
        configuration.setProxyPort((String)stream.readObject());
        configuration.setFfPath((String)stream.readObject());
        configuration.setPjsPath((String)stream.readObject());
        configuration.setPageReadyTimeout((String)stream.readObject());
        configuration.setMakeShots(stream.readBoolean());
        configuration.setUseFirefox(stream.readBoolean());
        configuration.setUsePhantomJs(stream.readBoolean());
        configuration.setUseRandomChars(stream.readBoolean());
        configuration.setScreenDir((String)stream.readObject());
        configuration.setCheckPageJs((String)stream.readObject());
        configuration.setWebDriverTag((String)stream.readObject());
        configuration.setFirefoxDisplay((String)stream.readObject());
    }

    @Override
    public void store(String file) throws Exception
    {
        ObjectOutputStream stream = getOutputStream(file);
        stream.writeObject(configuration.getProxyHost());
        stream.writeObject(configuration.getProxyPort());
        stream.writeObject(configuration.getFfPath());
        stream.writeObject(configuration.getPjsPath());
        stream.writeObject(configuration.getPageReadyTimeout());
        stream.writeBoolean(configuration.getMakeShots());
        stream.writeBoolean(configuration.isUseFirefox());
        stream.writeBoolean(configuration.isUsePhantomJs());
        stream.writeBoolean(configuration.isUseRandomChars());
        stream.writeObject(configuration.getScreenDir());
        stream.writeObject(configuration.getCheckPageJs());
        stream.writeObject(configuration.getWebDriverTag());
        stream.writeObject(configuration.getFirefoxDisplay());
    }

    @Override
    protected String getControllerDataFilename()
    {
        return "options";
    }

    public void setConfiguration(CommonConfiguration configuration) {
        this.configuration = configuration;
    }
}
