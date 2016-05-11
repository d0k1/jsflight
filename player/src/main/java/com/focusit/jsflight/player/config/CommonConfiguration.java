package com.focusit.jsflight.player.config;

/**
 * Common configuration i.e. everything about player. browser settings, timeout settings
 * Created by dkirpichenkov on 06.05.16.
 */
public class CommonConfiguration
{

    public static final String CHECK_PAGE_JS_DEFAULT = "return (document.getElementById('state.dispatch')==null || document.getElementById('state.dispatch').getAttribute('value')==0) &&  (document.getElementById('state.context')==null ||  document.getElementById('state.context').getAttribute('value')=='ready');";
    private String proxyPort;
    private String proxyHost;
    private String ffPath;
    private String pjsPath;
    private String pageReadyTimout;
    private boolean makeShots;
    private String screenDir;
    private String webDriverTag;
    private boolean useFirefox;
    private boolean usePhantomJs;
    private boolean useRandomChars;
    private String firefoxDisplay;

    private String checkPageJs;
    private String maxElementGroovy;

    private String uiShownScript;

    private int scrollTimeout;

    private int pageShownTimeout;

    public CommonConfiguration()
    {
        checkPageJs = CHECK_PAGE_JS_DEFAULT;
    }

    public String getCheckPageJs()
    {
        return checkPageJs;
    }

    public void setCheckPageJs(String checkPageJs)
    {
        this.checkPageJs = checkPageJs;
    }

    public String getFfPath()
    {
        return ffPath;
    }

    public void setFfPath(String ffPath)
    {
        this.ffPath = ffPath;
    }

    public boolean getMakeShots()
    {
        return Boolean.valueOf(makeShots);
    }

    public void setMakeShots(boolean makeShots)
    {
        this.makeShots = makeShots;
    }

    public String getPageReadyTimeout()
    {
        return pageReadyTimout;
    }

    public void setPageReadyTimeout(String pageReadyTimeout)
    {
        this.pageReadyTimout = pageReadyTimeout;
    }

    public String getPjsPath()
    {
        return pjsPath;
    }

    public void setPjsPath(String pjsPath)
    {
        this.pjsPath = pjsPath;
    }

    public String getProxyHost()
    {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost)
    {
        this.proxyHost = proxyHost;
    }

    public String getProxyPort()
    {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort)
    {
        this.proxyPort = proxyPort;
    }

    public String getScreenDir()
    {
        return screenDir;
    }

    public void setScreenDir(String screenDir)
    {
        this.screenDir = screenDir;
    }

    public String getWebDriverTag()
    {
        return webDriverTag;
    }

    public void setWebDriverTag(String webDriverTag)
    {
        this.webDriverTag = webDriverTag;
    }

    public boolean isUseFirefox()
    {
        return useFirefox;
    }

    public void setUseFirefox(boolean useFirefox)
    {
        this.useFirefox = useFirefox;
    }

    public boolean isUsePhantomJs()
    {
        return usePhantomJs;
    }

    public void setUsePhantomJs(boolean usePhantomJs)
    {
        this.usePhantomJs = usePhantomJs;
    }

    public boolean isUseRandomChars()
    {
        return useRandomChars;
    }

    public void setUseRandomChars(boolean useRandomChars)
    {
        this.useRandomChars = useRandomChars;
    }

    public String getFirefoxDisplay()
    {
        return firefoxDisplay;
    }

    public void setFirefoxDisplay(String firefoxDisplay)
    {
        this.firefoxDisplay = firefoxDisplay;
    }

    public String getMaxElementGroovy()
    {
        return maxElementGroovy;
    }

    public void setMaxElementGroovy(String maxElementGroovy)
    {
        this.maxElementGroovy = maxElementGroovy;
    }

    public String getUiShownScript()
    {
        return uiShownScript;
    }

    public void setUiShownScript(String uiShownScript)
    {
        this.uiShownScript = uiShownScript;
    }

    public int getScrollTimeout()
    {
        return scrollTimeout;
    }

    public void setScrollTimeout(int scrollTimeout)
    {
        this.scrollTimeout = scrollTimeout;
    }

    public int getPageShownTimeout()
    {
        return pageShownTimeout;
    }

    public void setPageShownTimeout(int pageShownTimeout)
    {
        this.pageShownTimeout = pageShownTimeout;
    }

    public void loadDefaultValues()
    {
        if (getCheckPageJs() == null)
        {
            setCheckPageJs(CHECK_PAGE_JS_DEFAULT);
        }
        if (getMaxElementGroovy() == null)
        {
            setMaxElementGroovy(
                    "def list = webdriver.findElements(org.openqa.selenium.By.xpath(\"//div[@id='gwt-debug-PopupListSelect']//div[@__idx]\"));\n"
                            + "\n" + "def maxEl = null;\n" + "Integer val = null;\n" + "\n" + "def tempEl = null;\n"
                            + "def tempVal = 0;\n" + "\n" + "for(int i=0;i<list.size();i++)\n" + "{\n"
                            + "\ttempEl = list.get(i);\n"
                            + "\ttempVal = Integer.parseInt(tempEl.getAttribute(\"__idx\"));\n" + "\n"
                            + "\tif(maxEl==null)\n" + "\t{\n" + "\t\tmaxEl = tempEl;\n" + "\t\tval = tempVal;\n"
                            + "\t\tcontinue;\n" + "\t}\n" + "\n" + "\tif(val<tempVal){\n" + "\t\tmaxEl = tempEl;\n"
                            + "\t\tval = tempVal;\n" + "\t}\n" + "}\n" + "\n" + "return maxEl;\n");
        }

        if (getUiShownScript() == null)
        {
            setUiShownScript(
                    "return webdriver.findElement(org.openqa.selenium.By.xpath(\"//*[@id='gwt-debug-editProfile']\"));");
        }
    }
}
