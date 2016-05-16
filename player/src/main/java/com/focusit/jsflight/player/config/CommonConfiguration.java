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
    private String pageReadyTimout = "30";
    private boolean makeShots;
    private String screenDir;
    private String webDriverTag;
    private boolean useFirefox;
    private boolean usePhantomJs;
    private boolean useRandomChars;
    private String firefoxDisplay;
    // TODO add path to xvfb start/stop scripts. To start xvfb on random display ondemand
    private String formOrDialogXpath;

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

    public String getFfPath()
    {
        return ffPath;
    }

    public String getFirefoxDisplay()
    {
        return firefoxDisplay;
    }

    public boolean getMakeShots()
    {
        return Boolean.valueOf(makeShots);
    }

    public String getMaxElementGroovy()
    {
        return maxElementGroovy;
    }

    public String getPageReadyTimeout()
    {
        return pageReadyTimout;
    }

    public int getPageShownTimeout()
    {
        return pageShownTimeout;
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

    public int getScrollTimeout()
    {
        return scrollTimeout;
    }

    public String getUiShownScript()
    {
        return uiShownScript;
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

        if (getFormOrDialogXpath() == null)
        {
            setFormOrDialogXpath("//div[@id='gwt-debug-Form' or @id='gwt-debug-PropertyDialogBox']");
        }
    }

    public void setCheckPageJs(String checkPageJs)
    {
        this.checkPageJs = checkPageJs;
    }

    public void setFfPath(String ffPath)
    {
        this.ffPath = ffPath;
    }

    public void setFirefoxDisplay(String firefoxDisplay)
    {
        this.firefoxDisplay = firefoxDisplay;
    }

    public void setMakeShots(boolean makeShots)
    {
        this.makeShots = makeShots;
    }

    public void setMaxElementGroovy(String maxElementGroovy)
    {
        this.maxElementGroovy = maxElementGroovy;
    }

    public void setPageReadyTimeout(String pageReadyTimeout)
    {
        this.pageReadyTimout = pageReadyTimeout;
    }

    public void setPageShownTimeout(int pageShownTimeout)
    {
        this.pageShownTimeout = pageShownTimeout;
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

    public void setScrollTimeout(int scrollTimeout)
    {
        this.scrollTimeout = scrollTimeout;
    }

    public void setUiShownScript(String uiShownScript)
    {
        this.uiShownScript = uiShownScript;
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

    public String getFormOrDialogXpath()
    {
        return formOrDialogXpath;
    }

    public void setFormOrDialogXpath(String formOrDialogXpath)
    {
        this.formOrDialogXpath = formOrDialogXpath;
    }
}
