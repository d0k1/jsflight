package com.focusit.jsflight.player.webdriver;

import com.focusit.jsflight.player.cli.CliConfig;
import com.focusit.jsflight.player.controller.OptionsController;

public class SeleniumDriverConfig
{
    private final static SeleniumDriverConfig INSTANCE = new SeleniumDriverConfig();

    public static SeleniumDriverConfig get()
    {
        return INSTANCE;
    }

    private boolean useFirefox;
    private boolean usePhantomJs;
    private String proxyHost;
    private String proxyPort;
    private String ffPath;
    private String pjsPath;
    private boolean makeShots;
    private String screenDir;
    private String pageReadyTimeout;

    public String getFfPath()
    {
        return ffPath;
    }

    public String getPageReadyTimeout()
    {
        return pageReadyTimeout;
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

    public boolean isMakeShots()
    {
        return makeShots;
    }

    public boolean isUseFirefox()
    {
        return useFirefox;
    }

    public boolean isUsePhantomJs()
    {
        return usePhantomJs;
    }

    public void setFfPath(String ffPath)
    {
        this.ffPath = ffPath;
    }

    public void setMakeShots(boolean makeShots)
    {
        this.makeShots = makeShots;
    }

    public void setPageReadyTimeout(String pageReadyTimeout)
    {
        this.pageReadyTimeout = pageReadyTimeout;
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

    public void updateByCliConfig(CliConfig config)
    {
        setFfPath(config.getFfPath());
        setPjsPath(config.getPjsPath());
        setMakeShots(config.getMakeShots());
        setProxyHost(config.getProxyHost());
        setProxyPort(config.getProxyPort());
        setScreenDir(config.getScreenDir());
        setUseFirefox(config.isUseFifefox());
        setUsePhantomJs(config.isUsePhantomJS());
        setPageReadyTimeout(config.getPageReadyTimeout());
    }

    public void updateByOptions(OptionsController options)
    {
        setFfPath(options.getFfPath());
        setPjsPath(options.getPjsPath());
        setMakeShots(Boolean.valueOf(options.getMakeShots()));
        setScreenDir(options.getScreenDir());
        setProxyHost(options.getProxyHost());
        setProxyPort(options.getProxyPort());
        setPageReadyTimeout(options.getPageReadyTimeout());
        setUseFirefox(Boolean.valueOf(options.isUseFirefox()));
        setUsePhantomJs(Boolean.valueOf(options.isUsePhantomJs()));
    }
}
