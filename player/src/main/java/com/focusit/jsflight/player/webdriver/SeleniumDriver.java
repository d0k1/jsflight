package com.focusit.jsflight.player.webdriver;

import com.focusit.jsflight.player.constants.EventType;
import com.focusit.jsflight.player.controller.OptionsController;
import com.focusit.jsflight.player.controller.WebLookupController;
import com.focusit.jsflight.player.scenario.UserScenario;
import com.focusit.jsflight.player.script.PlayerScriptProcessor;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Selenium webdriver proxy: runs a browser, sends events, make screenshots
 * 
 * @author Denis V. Kirpichenkov
 *
 */
public class SeleniumDriver
{
    private static final Logger log = LoggerFactory.getLogger(SeleniumDriver.class);
    private static OptionsController config = OptionsController.getInstance();
    private HashMap<String, WebDriver> drivers = new HashMap<>();
    private HashMap<String, String> tabsWindow = new HashMap<>();
    private HashMap<String, String> lastUrls = new HashMap<>();
    private StringGenerator stringGen;
    private UserScenario scenario;

    public SeleniumDriver(UserScenario scenario) {
        this.scenario = scenario;
    }

    private static boolean checkElementPresent(WebDriver wd, String target)
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

    private static void ensureElementInWindow(WebDriver wd, WebElement element)
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

    private static WebElement getMax(WebDriver wd)
    {
        List<WebElement> els = wd.findElements(By.xpath("//div[@id='gwt-debug-PopupListSelect']//div[@__idx]"));
        els.sort((WebElement el1, WebElement el2) -> Integer.valueOf(el1.getAttribute("__idx"))
                .compareTo(Integer.valueOf(el2.getAttribute("__idx"))));
        return els.get(els.size() - 1);
    }

    private static void resizeForEvent(WebDriver wd, JSONObject event)
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

    private static void scroll(JavascriptExecutor js, WebElement element)
    {
        js.executeScript("arguments[0].scrollIntoView(true)", element);
    }

    private static void waitUiShow(WebDriver wd)
    {
        long timeout = System.currentTimeMillis() + 20000L;
        while (System.currentTimeMillis() < timeout)
        {
            try
            {
                wd.findElement(By.xpath("//*[@id='gwt-debug-editProfile']"));
                log.debug("Yeeepeee UI showed up!");
                return;
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
        throw new NoSuchElementException("UI didn`t show up. =(");
    }

    public void closeWebDrivers()
    {
        drivers.values().stream().forEach(WebDriver::close);
        drivers.clear();
        lastUrls.clear();
    }

    public WebElement findTargetWebElement(WebDriver wd, JSONObject event, String target)
    {
        try
        {
            log.info("looking for " + target);
            return wd.findElement(By.xpath(target));
        }
        catch (NoSuchElementException e)
        {
            log.info("failed looking for {}. trying to restore xpath");
            return (WebElement)new PlayerScriptProcessor().executeWebLookupScript(WebLookupController.getInstance().getScript(), wd,
                    target, event);
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
        File dir = new File(screenDir + File.separator
                + Paths.get(scenario.getScenarioFilename()).getFileName().toString());

        if (!dir.exists() && !dir.mkdirs())
        {
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(new String(dir.getAbsolutePath() + File.separator
                + String.format("%05d", scenario.getPosition()) + ".png")))
        {
            fos.write(shot);
        }
        catch (IOException e)
        {
            log.error(e.toString(), e);
        }
    }

    public void openEventUrl(WebDriver wd, JSONObject event, int pageTimeoutMs)
    {
        String event_url = event.getString("url");

        resizeForEvent(wd, event);
        if (wd.getCurrentUrl().equals("about:blank") || !getLastUrl(event).equals(event_url))
        {
            wd.get(event_url);

            waitUiShow(wd);
            waitPageReady(wd, event, pageTimeoutMs);
            updateLastUrl(event, event_url);
        }
    }

    public void processKeyboardEvent(WebDriver wd, JSONObject event, WebElement element, boolean useRandomChars) throws UnsupportedEncodingException
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
                try {
                    new Actions(wd).contextClick(element).perform();
                } catch (WebDriverException ex){
                    try {
                        log.warn("Error simulation right click. Retrying after 2 sec.");
                        Thread.sleep(2000);

                        new Actions(wd).contextClick(element).perform();
                    } catch (Exception e) {
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

    public void processMouseWheel(WebDriver wd, JSONObject event, String target)
    {
        WebElement el = (WebElement)new PlayerScriptProcessor().executeWebLookupScript(WebLookupController.getInstance().getScript(), wd,
                target, event);
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

    public void processScroll(WebDriver wd, JSONObject event, String target, int pageTimeoutMs)
    {
        long timeout = System.currentTimeMillis() + 20000l;
        if (checkElementPresent(wd, target))
        {
            return;
        }
        do
        {
            waitPageReady(wd, event, pageTimeoutMs);
            // TODO WebLookup script must return the element
            WebElement el = getMax(wd);
            scroll((JavascriptExecutor)wd, el);
            if (checkElementPresent(wd, target))
            {
                return;
            }
        }
        while (System.currentTimeMillis() < timeout);
        throw new NoSuchElementException("Element was not found during scroll");
    }

    public void resetLastUrls()
    {
        lastUrls.clear();
    }

    public void updateLastUrl(JSONObject event, String url)
    {
        lastUrls.put(scenario.getTagForEvent(event), url);
    }

    public void waitPageReady(WebDriver wd, JSONObject event, int pageTimeoutMs)
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
                String CHECK_PAGE_READY_JS = "return (document.getElementById('state.dispatch')==null || document.getElementById('state.dispatch').getAttribute('value')==0) &&  (document.getElementById('state.context')==null ||  document.getElementById('state.context').getAttribute('value')=='ready');";
                Object result = js.executeScript(CHECK_PAGE_READY_JS);
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

    public WebDriver getDriverForEvent(JSONObject event, boolean firefox, String path, String display, String proxyHost, String proxyPort)
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
                if(display!=null && !display.trim().isEmpty()) {
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
                    String newTabKey = Keys.chord(Keys.CONTROL, "n");
                    driver.findElement(By.tagName("body")).sendKeys(newTabKey);
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

    public void setScenario(UserScenario scenario) {
        this.scenario = scenario;
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
}
