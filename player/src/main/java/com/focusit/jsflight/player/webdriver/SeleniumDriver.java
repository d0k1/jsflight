package com.focusit.jsflight.player.webdriver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.focusit.jsflight.player.constants.EventType;
import com.focusit.jsflight.player.scenario.UserScenario;
import com.focusit.jsflight.player.script.PlayerScriptProcessor;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

/**
 * Selenium webdriver proxy: runs a browser, sends events, make screenshots
 * 
 * @author Denis V. Kirpichenkov
 *
 */
public class SeleniumDriver
{

    private static class CharStringGenerator implements StringGenerator
    {

        @Override
        public String getAsString(char ch) throws UnsupportedEncodingException
        {
            return new String(new byte[] { (byte)ch }, StandardCharsets.UTF_8);
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

    private interface StringGenerator
    {
        String getAsString(char ch) throws UnsupportedEncodingException;
    }

    private static final Logger log = LoggerFactory.getLogger(SeleniumDriver.class);

    private HashMap<String, WebDriver> drivers = new HashMap<>();

    /**
     * Using BiMap for removing entries by value, because tabUuids and Windowhandles are unique
     */
    private BiMap<String, String> tabsWindow = HashBiMap.create();

    private HashMap<String, String> lastUrls = new HashMap<>();

    private StringGenerator stringGen;

    private UserScenario scenario;

    public SeleniumDriver(UserScenario scenario)
    {
        this.scenario = scenario;
    }

    public void closeWebDrivers()
    {
        drivers.values().forEach(WebDriver::close);
    }

    public WebElement findTargetWebElement(String script, WebDriver wd, JSONObject event, String target)
    {
        try
        {
            log.info("looking for " + target);
            return wd.findElement(By.xpath(target));
        }
        catch (NoSuchElementException e)
        {
            log.info("failed looking for {}. trying to restore xpath");
            return (WebElement)new PlayerScriptProcessor().executeWebLookupScript(script, wd, target, event);
        }
    }

    public WebDriver getDriverForEvent(JSONObject event, boolean firefox, String path, String display, String proxyHost,
            String proxyPort)
    {
        String tag = scenario.getTagForEvent(event);

        WebDriver driver = drivers.get(tag);

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
            drivers.put(tag, driver);
            return driver;
        }
        catch (Throwable ex)
        {
            log.error(ex.toString(), ex);
            throw ex;
        }
        finally
        {
            String tabUuid = event.getString("tabuuid");
            String window = tabsWindow.get(tabUuid);
            if (window == null)
            {
                ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
                if (tabsWindow.values().contains(tabs.get(0)))
                {
                    ((JavascriptExecutor)driver).executeScript("window.open()");
                    tabs = new ArrayList<String>(driver.getWindowHandles());
                }
                window = tabs.get(tabs.size() - 1);
                tabsWindow.put(tabUuid, window);
            }
            if (!driver.getWindowHandle().equals(window))
            {
                driver.switchTo().window(window);
            }
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
        TakesScreenshot shoter = (TakesScreenshot)wd;
        byte[] shot = shoter.getScreenshotAs(OutputType.BYTES);
        File dir = new File(
                screenDir + File.separator + Paths.get(scenario.getScenarioFilename()).getFileName().toString());

        if (!dir.exists() && !dir.mkdirs())
        {
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(new String(
                dir.getAbsolutePath() + File.separator + String.format("%05d", scenario.getPosition()) + ".png")))
        {
            fos.write(shot);
        }
        catch (IOException e)
        {
            log.error(e.toString(), e);
        }
    }

    public void openEventUrl(WebDriver wd, JSONObject event, int pageTimeoutMs, String checkPageJs,
            String uiShownScript)
    {
        String event_url = event.getString("url");

        resizeForEvent(wd, event);
        if (wd.getCurrentUrl().equals("about:blank") || !getLastUrl(event).equals(event_url))
        {
            wd.get(event_url);

            waitUiShow(uiShownScript, wd);
            waitPageReady(wd, event, pageTimeoutMs, checkPageJs);
            updateLastUrl(event, event_url);
        }
    }

    public void processKeyboardEvent(WebDriver wd, JSONObject event, WebElement element, boolean useRandomChars)
            throws UnsupportedEncodingException
    {
        ensureStringGeneratorInitialized(useRandomChars);
        if (event.getString("type").equalsIgnoreCase(EventType.KEY_PRESS))
        {
            if (event.has("charCode"))
            {
                char ch = (char)event.getBigInteger(("charCode")).intValue();
                String keys = stringGen.getAsString(ch);
                if (!element.getTagName().contains("iframe"))
                {
                    element.sendKeys(keys);
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
                    case 127:
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

    public void processMouseEvent(WebDriver wd, JSONObject event, WebElement element)
    {
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
                        log.warn("Error simulation right click. Retrying after 2 sec.");
                        Thread.sleep(2000);

                        new Actions(wd).contextClick(element).perform();
                    }
                    catch (Exception e)
                    {
                        log.error(e.toString(), e);
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
                    log.warn(ex.toString(), ex);

                    try
                    {
                        JavascriptExecutor executor = (JavascriptExecutor)wd;
                        executor.executeScript("arguments[0].click();", element);
                    }
                    catch (Exception ex1)
                    {
                        log.error(ex1.toString(), ex1);
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

    public void processMouseWheel(String script, WebDriver wd, JSONObject event, String target)
    {
        WebElement el = (WebElement)new PlayerScriptProcessor().executeWebLookupScript(script, wd, target, event);
        //Web lookup script MUST return /html element if scroll occurs not in a popup
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

    public void processScroll(WebDriver wd, JSONObject event, String target, int pageTimeoutMs, String checkPageJs,
            String getMaxElementGroovy)
    {
        long timeout = System.currentTimeMillis() + 20000l;
        if (checkElementPresent(wd, target))
        {
            return;
        }
        do
        {
            waitPageReady(wd, event, pageTimeoutMs, checkPageJs);
            // TODO WebLookup script must return the element
            try
            {
                WebElement el = getMax(wd, getMaxElementGroovy);
                scroll((JavascriptExecutor)wd, el);
                if (checkElementPresent(wd, target))
                {
                    return;
                }
            }
            catch (Exception ex)
            {
                log.error(ex.toString(), ex);
            }
        }
        while (System.currentTimeMillis() < timeout);
        throw new NoSuchElementException("Element was not found during scroll");
    }

    public void releaseBrowser(WebDriver driver, String formOrDialogXpath)
    {
        if (!(null != formOrDialogXpath && !formOrDialogXpath.isEmpty()))
        {
            log.debug("Form or dialog xpath is not specified. Aborting release process");
            return;
        }
        String xpath = formOrDialogXpath;
        if (driver.getWindowHandles().size() > 1)
        {
            List<String> tabsToCheck = Lists.newArrayList(driver.getWindowHandles());
            String handle = driver.getWindowHandle();
            tabsToCheck.remove(handle);
            closeTabs(driver, tabsToCheck, xpath);
            driver.switchTo().window(handle);
        }
        //Check if others are not necessary 
        HashMap<String, WebDriver> m = new HashMap<>();

        drivers.entrySet().forEach(e -> {
            if (!e.getValue().equals(driver))
            {
                m.put(e.getKey(), e.getValue());
            }
        });

        m.forEach((k, d) -> {
            List<String> surv = closeTabs(d, d.getWindowHandles(), xpath);
            //It is crucial to switch driver to a survived tab if none survived -
            //driver is dead and won`t respond to commands leading to UnreachebleBrowserException
            if (!surv.isEmpty())
            {
                d.switchTo().window(surv.get(0));
            }
            else
            {
                //Nothing survived - remove webdriver
                drivers.remove(k);
            }
        });
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

    public void waitPageReady(WebDriver wd, JSONObject event, int pageTimeoutMs, String checkPageJs)
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
            log.error(e.toString(), e);
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

    private List<String> closeTabs(WebDriver driver, Collection<String> tabs, String formDialogXpath)
    {
        List<String> survived = new ArrayList<>();
        tabs.forEach(tab -> {
            WebDriver tabDriver = driver.switchTo().window(tab);
            if (tabDriver.findElements(By.xpath(formDialogXpath)).size() == 0)
            {
                tabsWindow.inverse().remove(tab);
                tabDriver.close();
            }
            else
            {
                survived.add(tab);
            }
        });
        return survived;
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

    private WebElement getMax(WebDriver wd, String script)
    {
        return (WebElement)new PlayerScriptProcessor().executeWebLookupScript(script, wd, null, null);
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
                if (new PlayerScriptProcessor().executeWebLookupScript(script, wd, null, null) != null)
                {
                    log.debug("Yeeepeee UI showed up!");
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
                    log.error(e1.toString(), e1);
                }
            }
        }
        throw new NoSuchElementException("UI didn't show up");
    }
}
