package com.focusit.jsflight.player.config;

/**
 * Common configuration i.e. everything about player. browser settings, timeout settings
 * Created by dkirpichenkov on 06.05.16.
 */
public class CommonConfiguration {

    private String proxyPort;
    private String proxyHost;
    private String ffPath;
    private String pjsPath;
    private String pageReadyTimout;
    private boolean makeShots;
    private String screenDir;
    private String checkPageJs;
    private String webDriverTag;
    private boolean useFirefox;
    private boolean usePhantomJs;
    private boolean useRandomChars;
    private String firefoxDisplay;

    public String getCheckPageJs()
    {
        return checkPageJs;
    }

    public String getFfPath()
    {
        return ffPath;
    }

    public boolean getMakeShots()
    {
        return Boolean.valueOf(makeShots);
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

    public boolean isUseRandomChars()
    {
        return useRandomChars;
    }

    public void setCheckPageJs(String checkPageJs)
    {
        this.checkPageJs = checkPageJs;
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

    public void setUseRandomChars(boolean useRandomChars)
    {
        this.useRandomChars = useRandomChars;
    }

    public void setWebDriverTag(String webDriverTag)
    {
        this.webDriverTag = webDriverTag;
    }

    public String getFirefoxDisplay() {
        return firefoxDisplay;
    }

    public void setFirefoxDisplay(String firefoxDisplay) {
        this.firefoxDisplay = firefoxDisplay;
    }

}
