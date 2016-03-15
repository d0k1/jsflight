package com.focusit.jsflight.player.controller;

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

    private String proxyPort;
    private String proxyHost;
    private String ffPath;
    private String pjsPath;
    private String pageReadyTimout;
    private String makeShots;
    private String screenDir;
    private String checkPageJs;
    private String webDriverTag;
    private boolean useFirefox;
    private boolean usePhantomJs;

    private OptionsController()
    {
    }

    public String getCheckPageJs()
    {
        return checkPageJs;
    }

    public String getFfPath()
    {
        return ffPath;
    }

    public String getMakeShots()
    {
        return makeShots;
    }

    public String getPageReadyTimeout()
    {
        return pageReadyTimout;
    }

    public String getPjsPath()
    {
        return pjsPath;
    }

    public String getProxyHost()
    {
        return proxyHost;
    }

    public String getProxyPort()
    {
        return proxyPort;
    }

    public String getScreenDir()
    {
        return screenDir;
    }

    public String getWebDriverTag()
    {
        return webDriverTag;
    }

    public boolean isUseFirefox()
    {
        return useFirefox;
    }

    public boolean isUsePhantomJs()
    {
        return usePhantomJs;
    }

    @Override
    public void load(String file) throws Exception
    {
        ObjectInputStream stream = getInputStream(file);
        proxyHost = (String)stream.readObject();
        proxyPort = (String)stream.readObject();
        ffPath = (String)stream.readObject();
        pjsPath = (String)stream.readObject();
        pageReadyTimout = (String)stream.readObject();
        makeShots = (String)stream.readObject();
        screenDir = (String)stream.readObject();
        checkPageJs = (String)stream.readObject();
        webDriverTag = (String)stream.readObject();
    }

    public void setCheckPageJs(String checkPageJs)
    {
        this.checkPageJs = checkPageJs;
    }

    public void setFfPath(String ffPath)
    {
        this.ffPath = ffPath;
    }

    public void setMakeShots(String makeShots)
    {
        this.makeShots = makeShots;
    }

    public void setPageReadyTimeout(String pageReadyTimeout)
    {
        this.pageReadyTimout = pageReadyTimeout;
    }

    public void setPjsPath(String pjsPath)
    {
        this.pjsPath = pjsPath;
    }

    public void setProxyHost(String proxyHost)
    {
        this.proxyHost = proxyHost;
    }

    public void setProxyPort(String proxyPort)
    {
        this.proxyPort = proxyPort;
    }

    public void setScreenDir(String screenDir)
    {
        this.screenDir = screenDir;
    }

    public void setUseFirefox(boolean useFirefox)
    {
        this.useFirefox = useFirefox;
    }

    public void setUsePhantomJs(boolean usePhantomJs)
    {
        this.usePhantomJs = usePhantomJs;
    }

    public void setWebDriverTag(String webDriverTag)
    {
        this.webDriverTag = webDriverTag;
    }

    @Override
    public void store(String file) throws Exception
    {
        ObjectOutputStream stream = getOutputStream(file);
        stream.writeObject(proxyHost);
        stream.writeObject(proxyPort);
        stream.writeObject(ffPath);
        stream.writeObject(pjsPath);
        stream.writeObject(pageReadyTimout);
        stream.writeObject(makeShots);
        stream.writeObject(screenDir);
        stream.writeObject(checkPageJs);
        stream.writeObject(webDriverTag);
    }

    @Override
    protected String getControllerDataFilename()
    {
        return "options";
    }

}
