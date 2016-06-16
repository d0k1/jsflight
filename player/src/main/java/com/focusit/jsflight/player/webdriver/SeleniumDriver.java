package com.focusit.jsflight.player.webdriver;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.focusit.jsflight.player.constants.EventType;
import com.focusit.jsflight.player.scenario.UserScenario;
import com.focusit.jsflight.player.script.PlayerScriptProcessor;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Selenium webdriver proxy: runs a browser, sends events, make screenshots
 *
 * @author Denis V. Kirpichenkov
 *
 */
public class SeleniumDriver
{

    /**
     * Non operational element indicating step processing must be aborted
     * returned from weblookup script
     */
    public static final RemoteWebElement NO_OP_ELEMENT = new RemoteWebElement();
    private static final Logger LOG = LoggerFactory.getLogger(SeleniumDriver.class);

    private static final int DISPLAY_CAPACITY = 200;

    static
    {
        //This must be set due to equals of WebElement
        NO_OP_ELEMENT.setId("NO_OP");
    }

    /**
     * As we close unsued browsers, 100 number of displays is more than enough
     */
    private ArrayList<String> availiableDisplays = new ArrayList<>(DISPLAY_CAPACITY);
    private HashMap<String, String> tagDisplay = new HashMap<>();
    private HashMap<String, WebDriver> drivers = new HashMap<>();
    /**
     * Using BiMap for removing entries by value, because tabUuids and Windowhandles are unique
     */
    private BiMap<String, String> tabsWindow = HashBiMap.create();
    private Map<String, String> lastUrls = new HashMap<>();
    private StringGenerator stringGen;
    private UserScenario scenario;
    private int pageTimeoutMs;
    private String checkPageJs;
    private String lookupScript;
    private String maxElementGroovy;
    private String uiShownScript;
    private boolean useRandomChars;

    public SeleniumDriver(UserScenario scenario)
    {
        this.scenario = scenario;

        //init queue
        //staring from 1, rather than 0, cuz on :0 firefox open on real display
        for (int i = 1; i < DISPLAY_CAPACITY; i++)
        {
            availiableDisplays.add(":" + i);
        }
    }

    public static RemoteWebElement getNoOpElement()
    {
        return NO_OP_ELEMENT;
    }

    public SeleniumDriver setLastUrls(Map<String, String> externalUrls)
    {
        this.lastUrls = externalUrls;
        return this;
    }

    public SeleniumDriver setUseRandomChars(boolean useRandomChars)
    {
        this.useRandomChars = useRandomChars;
        return this;
    }

    public SeleniumDriver setPageTimeoutMs(int pageTimeoutMs)
    {
        this.pageTimeoutMs = pageTimeoutMs;
        return this;
    }

    public SeleniumDriver setCheckPageJs(String checkPageJs)
    {
        this.checkPageJs = checkPageJs;
        return this;
    }

    public SeleniumDriver setLookupScript(String lookupScript)
    {
        this.lookupScript = lookupScript;
        return this;
    }

    public SeleniumDriver setUiShownScript(String uiShownScript)
    {
        this.uiShownScript = uiShownScript;
        return this;
    }

    public SeleniumDriver setMaxElementGroovy(String maxElementGroovy)
    {
        this.maxElementGroovy = maxElementGroovy;
        return this;
    }

    public void closeWebDrivers()
    {
        drivers.values().forEach(WebDriver::close);
    }

    public WebElement findTargetWebElement(WebDriver wd, JSONObject event, String target)
    {
        waitPageReady(wd, event);
        return (WebElement)new PlayerScriptProcessor(scenario).executeWebLookupScript(lookupScript, wd, target, event);
    }

    public WebDriver getDriverForEvent(JSONObject event, boolean firefox, String path, String display, String proxyHost,
            String proxyPort)
    {
        String tabUuid = event.getString("tabuuid");
        WebDriver driver = drivers.get(tabUuid);

        try
        {
            if (driver != null)
            {
                return driver;
            }

            boolean useFirefox = firefox;
            boolean usePhantomJs = !firefox;
            DesiredCapabilities cap = new DesiredCapabilities();
            if (proxyHost.trim().length() > 0)
            {
                String host = proxyHost;
                if (proxyPort.trim().length() > 0)
                {
                    host += ":" + proxyPort;
                }
                Proxy proxy = new Proxy();
                proxy.setHttpProxy(host).setFtpProxy(host).setSslProxy(host);
                cap.setCapability(CapabilityType.PROXY, proxy);
            }
            if (useFirefox)
            {
                FirefoxProfile profile = new FirefoxProfile();
                String ffPath = path;
                FirefoxBinary binary = null;
                if (ffPath != null && ffPath.trim().length() > 0)
                {
                    binary = new FirefoxBinary(new File(ffPath));
                }
                else
                {
                    binary = new FirefoxBinary();
                }
                if (display != null && !display.trim().isEmpty())
                {
                    display = availiableDisplays.get(0);
                    availiableDisplays.remove(display);
                    LOG.info("Binding to {} display", display);
                    binary.setEnvironmentProperty("DISPLAY", display);
                }
                driver = new FirefoxDriver(binary, profile, cap);
            }
            else if (usePhantomJs)
            {
                String pjsPath = path;
                if (pjsPath != null && pjsPath.trim().length() > 0)
                {
                    cap.setCapability("phantomjs.binary.path", pjsPath);

                    driver = new PhantomJSDriver(cap);
                }
                else
                {
                    driver = new PhantomJSDriver(cap);
                }
            }
            driver = WebDriverWrapper.wrap(driver);

            drivers.put(tabUuid, driver);

            //as actual webdriver is RemoteWebdriver, calling to string return browser name, platform and sessionid
            //which are not subject to change, so we can use it as key;
            tagDisplay.put(driver.toString(), display);
            return driver;
        }
        catch (Throwable ex)
        {
            LOG.error(ex.toString(), ex);
            throw ex;
        }
    }

    public String getLastUrl(JSONObject event)
    {
        String no_result = "";
        String result = lastUrls.get(scenario.getTagForEvent(event));
        if (result == null)
        {
            lastUrls.put(scenario.getTagForEvent(event), no_result);
            result = no_result;
        }

        return result;
    }

    public void makeAShot(WebDriver wd, String screenDir)
    {
    }

    public void makeAShot(WebDriver wd, OutputStream outputStream) throws IOException
    {
        TakesScreenshot shooter = (TakesScreenshot)wd;
        byte[] shot = shooter.getScreenshotAs(OutputType.BYTES);
        outputStream.write(shot);
    }

    public void openEventUrl(WebDriver wd, JSONObject event)
    {
        String event_url = event.getString("url");

        resizeForEvent(wd, event);
        if (wd.getCurrentUrl().equals("about:blank") || !getLastUrl(event).equals(event_url))
        {
            wd.get(event_url);

            waitUiShow(uiShownScript, wd);
            waitPageReady(wd, event);
            updateLastUrl(event, event_url);
        }
    }

    public void processKeyboardEvent(WebDriver wd, JSONObject event) throws UnsupportedEncodingException
    {
        WebElement element = findTargetWebElement(wd, event, scenario.getTargetForEvent(event));
        if (element.equals(NO_OP_ELEMENT))
        {
            LOG.warn("Non operational element returned. Aborting event {} processing. Target xpath {}",
                    event.get("eventId"), event.getString("target2"));
            return;
        }
        ensureStringGeneratorInitialized(useRandomChars);

        //TODO remove this when recording of cursor in text box is implemented
        if (skipKeyboardForElement(element))
        {
            LOG.warn("Keyboard processing for non empty Date is disabled");
            return;
        }

        //Before processing keyboard events focus MUST be on the browser window, otherwise there is no
        //guaranties for correct processing.
        //Firefox ignores all fired events when window is not in focus
        //Selenium uses dark magic to deal with it
        wd.switchTo().window(wd.getWindowHandle());

        if (event.getString("type").equalsIgnoreCase(EventType.KEY_PRESS))
        {
            if (event.has("charCode"))
            {
                char ch = (char)event.getBigInteger(("charCode")).intValue();
                String keys = stringGen.getAsString(ch);
                if (!element.getTagName().contains("iframe"))
                {
                    String prevText = element.getAttribute("value");
                    element.clear();
                    element.sendKeys(prevText + keys);

                }
                else
                {
                    WebDriver frame = wd.switchTo().frame(element);
                    WebElement editor = frame.findElement(By.tagName("body"));
                    editor.sendKeys(keys);
                    wd.switchTo().defaultContent();
                }
            }
        }

        if (event.getString("type").equalsIgnoreCase(EventType.KEY_UP)
                || event.getString("type").equalsIgnoreCase(EventType.KEY_DOWN))
        {
            if (event.has("charCode"))
            {
                int code = event.getBigInteger(("charCode")).intValue();
                if (code == 0)
                {
                    code = event.getInt("keyCode");
                }
                if (event.getBoolean("ctrlKey"))
                {
                    element.sendKeys(
                            Keys.chord(Keys.CONTROL, new String(new byte[] { (byte)code }, StandardCharsets.UTF_8)));
                }
                else
                {
                    switch (code)
                    {
                    case 8:
                        element.sendKeys(Keys.BACK_SPACE);
                        break;
                    case 27:
                        element.sendKeys(Keys.ESCAPE);
                        break;
                    case 46:
                        element.sendKeys(Keys.DELETE);
                        break;
                    case 13:
                        element.sendKeys(Keys.ENTER);
                        break;
                    case 37:
                        element.sendKeys(Keys.ARROW_LEFT);
                        break;
                    case 38:
                        element.sendKeys(Keys.ARROW_UP);
                        break;
                    case 39:
                        element.sendKeys(Keys.ARROW_RIGHT);
                        break;
                    case 40:
                        element.sendKeys(Keys.ARROW_DOWN);
                        break;
                    default:
                        break;
                    }
                }
            }
        }
    }

    public void processMouseEvent(WebDriver wd, JSONObject event)
    {
        WebElement element = findTargetWebElement(wd, event, scenario.getTargetForEvent(event));
        if (element.equals(NO_OP_ELEMENT))
        {
            LOG.warn("Non operational element returned. Aborting event {} processing. Target xpath {}",
                    event.get("eventId"), event.getString("target2"));
            return;
        }
        ensureElementInWindow(wd, element);
        if (element.isDisplayed())
        {

            if (event.getInt("button") == 2)
            {
                try
                {
                    new Actions(wd).contextClick(element).perform();
                }
                catch (WebDriverException ex)
                {
                    try
                    {
                        LOG.warn("Error simulation right click. Retrying after 2 sec.");
                        Thread.sleep(2000);

                        new Actions(wd).contextClick(element).perform();
                    }
                    catch (Exception e)
                    {
                        LOG.error(e.toString(), e);
                    }
                }
            }
            else
            {
                try
                {
                    element.click();
                }
                catch (Exception ex)
                {
                    LOG.warn(ex.toString(), ex);

                    try
                    {
                        JavascriptExecutor executor = (JavascriptExecutor)wd;
                        executor.executeScript("arguments[0].click();", element);
                    }
                    catch (Exception ex1)
                    {
                        LOG.error(ex1.toString(), ex1);
                        throw new WebDriverException(ex1);
                    }
                }
            }
        }
        else
        {
            JavascriptExecutor executor = (JavascriptExecutor)wd;
            executor.executeScript("arguments[0].click();", element);
        }
    }

    public void processMouseWheel(WebDriver wd, JSONObject event, String target)
    {
        if (!event.has("deltaY"))
        {
            LOG.error("event has no deltaY - cant process scroll", new Exception());
            return;
        }
        WebElement el = (WebElement)new PlayerScriptProcessor(scenario).executeWebLookupScript(lookupScript, wd, target,
                event);
        if (el.equals(NO_OP_ELEMENT))
        {
            LOG.warn("Non operational element returned. Aborting event {} processing. Target xpath {}",
                    event.get("eventId"), event.getString("target2"));
            return;
        }
        //Web lookup script MUST return //body element if scroll occurs not in a popup
        if (!el.getTagName().equalsIgnoreCase("html"))
        {
            ((JavascriptExecutor)wd).executeScript("arguments[0].scrollTop = arguments[0].scrollTop + arguments[1]", el,
                    event.getInt("deltaY"));
        }
        else
        {
            ((JavascriptExecutor)wd).executeScript("window.scrollBy(0, arguments[0])", event.getInt("deltaY"));
        }
    }

    public void processScroll(WebDriver wd, JSONObject event, String target)
    {
        long timeout = System.currentTimeMillis() + 20000l;
        if (checkElementPresent(wd, target))
        {
            return;
        }
        do
        {
            waitPageReady(wd, event);
            // TODO WebLookup script must return the element
            try
            {
                WebElement el = getMax(wd, maxElementGroovy);
                scroll((JavascriptExecutor)wd, el);
                if (checkElementPresent(wd, target))
                {
                    return;
                }
            }
            catch (Exception ex)
            {
                LOG.error(ex.toString(), ex);
            }
        }
        while (System.currentTimeMillis() < timeout);
        throw new NoSuchElementException("Element was not found during scroll");
    }

    public void releaseBrowser(WebDriver driver, String formOrDialogXpath, JSONObject event)
    {
        if (!(null != formOrDialogXpath && !formOrDialogXpath.isEmpty()))
        {
            LOG.debug("Form or dialog xpath is not specified. Aborting release process");
            return;
        }
        if (driver.findElements(By.xpath(formOrDialogXpath)).isEmpty())
        {
            String display = tagDisplay.get(driver.toString());
            LOG.info("Display {} is available again", display);
            availiableDisplays.add(display);

            String tabUuid = event.getString("tabuuid");
            drivers.remove(tabUuid);
            driver.quit();
        }

    }

    public void resetLastUrls()
    {
        lastUrls.clear();
    }

    public void setScenario(UserScenario scenario)
    {
        this.scenario = scenario;
    }

    public void updateLastUrl(JSONObject event, String url)
    {
        lastUrls.put(scenario.getTagForEvent(event), url);
    }

    public void waitPageReady(WebDriver wd, JSONObject event)
    {
        String type = event.getString("type");
        if (type.equalsIgnoreCase(EventType.XHR) || type.equalsIgnoreCase(EventType.SCRIPT))
        {
            return;
        }
        int timeout = pageTimeoutMs * 1000;
        try
        {
            int sleeps = 0;
            JavascriptExecutor js = (JavascriptExecutor)wd;
            while (sleeps < timeout)
            {
                Object result = js.executeScript(checkPageJs);
                if (result != null && Boolean.parseBoolean(result.toString().toLowerCase()))
                {
                    break;
                }
                Thread.sleep(1 * 1000);
                sleeps += 1000;
            }
        }
        catch (InterruptedException e)
        {
            LOG.error(e.toString(), e);
            throw new IllegalStateException(e);
        }
    }

    private boolean checkElementPresent(WebDriver wd, String target)
    {
        try
        {
            wd.findElement(By.xpath(target));
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private void ensureElementInWindow(WebDriver wd, WebElement element)
    {
        int windowHeight = wd.manage().window().getSize().getHeight();
        int elementYCoord = element.getLocation().getY();
        if (elementYCoord > windowHeight)
        {
            //Using division of the Y coordinate by 2 ensures target element visibility in the browser view
            //anyway TODO think of not using hardcoded constants in scrolling
            String scrollScript = "window.scrollTo(0, " + elementYCoord / 2 + ");";
            ((JavascriptExecutor)wd).executeScript(scrollScript);
        }
    }

    private void ensureStringGeneratorInitialized(boolean useRandomChars)
    {
        if (stringGen == null)
        {
            if (useRandomChars)
            {
                stringGen = new RandomStringGenerator();
            }
            else
            {
                stringGen = new CharStringGenerator();
            }
        }
    }

    private boolean skipKeyboardForElement(WebElement element)
    {
        //TODO remove this when recording of cursor in text box is implemented
        return !element.getAttribute("value").isEmpty()
                && (element.getAttribute("class").contains("date") || element.getAttribute("id").contains("date"));
    }

    private WebElement getMax(WebDriver wd, String script)
    {
        return (WebElement)new PlayerScriptProcessor(scenario).executeWebLookupScript(script, wd, null, null);
    }

    private void resizeForEvent(WebDriver wd, JSONObject event)
    {
        int w = 0;
        int h = 0;
        if (event.has("window.width"))
        {
            w = event.getInt("window.width");
            h = event.getInt("window.height");
        }
        else
        {
            JSONObject window = event.getJSONObject("window");
            w = window.getInt("width");
            h = window.getInt("height");
        }

        if (w == 0)
        {
            w = 1000;
        }

        if (h == 0)
        {
            h = 1000;
        }

        wd.manage().window().setSize(new Dimension(w, h));
    }

    private void scroll(JavascriptExecutor js, WebElement element)
    {
        js.executeScript("arguments[0].scrollIntoView(true)", element);
    }

    private void waitUiShow(String script, WebDriver wd)
    {
        long timeout = System.currentTimeMillis() + 20000L;
        while (System.currentTimeMillis() < timeout)
        {
            try
            {
                if (new PlayerScriptProcessor(scenario).executeWebLookupScript(script, wd, null, null) != null)
                {
                    LOG.debug("Yeeepeee UI showed up!");
                    return;
                }
            }
            catch (NoSuchElementException e)
            {
                try
                {
                    Thread.sleep(500);
                }
                catch (InterruptedException e1)
                {
                    //Should never happen
                    LOG.error(e1.toString(), e1);
                }
            }
        }
        throw new NoSuchElementException("UI didn't show up");
    }

    private interface StringGenerator
    {
        String getAsString(char ch) throws UnsupportedEncodingException;
    }

    private static class CharStringGenerator implements StringGenerator
    {

        @Override
        public String getAsString(char ch) throws UnsupportedEncodingException
        {
            return String.valueOf(ch);
        }

    }

    private static class RandomStringGenerator implements StringGenerator
    {

        @Override
        public String getAsString(char ch)
        {
            return RandomStringUtils.randomAlphanumeric(1);
        }

    }
}
