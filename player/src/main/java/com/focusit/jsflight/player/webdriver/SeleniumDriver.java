package com.focusit.jsflight.player.webdriver;

import com.focusit.jsflight.player.constants.EventType;
import com.focusit.jsflight.player.scenario.UserScenario;
import org.json.JSONArray;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Selenium webdriver helper: runs a browser, sends events, make screenshots
 * 
 * @author Denis V. Kirpichenkov
 *
 */
public class SeleniumDriver
{
    private static final String SET_ELEMENT_VISIBLE_JS = "var e = document.evaluate('%s' ,document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null ).singleNodeValue; if(e!== null) {e.style.visibility='visible';};";

    private static final Logger log = LoggerFactory.getLogger(SeleniumDriver.class);

    private static final SeleniumDriverConfig CONFIG = SeleniumDriverConfig.get();

    private static HashMap<String, WebDriver> drivers = new HashMap<>();
    private static HashMap<String, String> tabsWindow = new HashMap<>();

    private static HashMap<String, String> lastUrls = new HashMap<>();

    public static void closeWebDrivers()
    {
        drivers.values().stream().forEach(WebDriver::close);
        drivers.clear();
        lastUrls.clear();
    }

    public static WebElement findTargetWebElement(JSONObject event, String target)
    {
        //makeElementVisibleByJS(event, target);
        WebDriver wd = getDriverForEvent(event);
        try
        {
            log.info("looking for " + target);
            return wd.findElement(By.xpath(target));
        }
        catch (org.openqa.selenium.NoSuchElementException e)
        {
            log.info("failed looking for " + getCSSSelector(event));
            return wd.findElement(By.cssSelector(getCSSSelector(event)));
        }
    }

    private static WebDriver getDriverForEvent(JSONObject event)
    {

        String tag = UserScenario.getTagForEvent(event);

        WebDriver driver = drivers.get(tag);

        try
        {
            if (driver != null)
            {
                return driver;
            }

            FirefoxProfile profile = new FirefoxProfile();
            DesiredCapabilities cap = new DesiredCapabilities();
            String proxyHost = CONFIG.getProxyHost();
            if (proxyHost.trim().length() > 0)
            {
                String host = proxyHost;
                String proxyPort = CONFIG.getProxyPort();
                if (proxyPort.trim().length() > 0)
                {
                    host += ":" + proxyPort;
                }
                org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
                proxy.setHttpProxy(host).setFtpProxy(host).setSslProxy(host);
                cap.setCapability(CapabilityType.PROXY, proxy);
            }
            boolean useFirefox = CONFIG.isUseFirefox();
            boolean usePhantomJs = CONFIG.isUsePhantomJs();
            if (useFirefox)
            {
                String ffPath = CONFIG.getFfPath();
                if (ffPath != null && ffPath.trim().length() > 0)
                {
                    FirefoxBinary binary = new FirefoxBinary(new File(ffPath));
                    driver = new FirefoxDriver(binary, profile, cap);
                }
                else
                {
                    driver = new FirefoxDriver(new FirefoxBinary(), profile, cap);
                }
            }
            else if (usePhantomJs)
            {
                String pjsPath = CONFIG.getPjsPath();
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
            try
            {
                Thread.sleep(3000);
            }
            catch (InterruptedException e)
            {
                log.error(e.toString(), e);
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

    public static String getLastUrl(JSONObject event)
    {
        String no_result = "";
        String result = lastUrls.get(UserScenario.getTagForEvent(event));
        if (result == null)
        {
            lastUrls.put(UserScenario.getTagForEvent(event), no_result);
            result = no_result;
        }

        return result;
    }

    public static void makeAShot(JSONObject event)
    {
        if (CONFIG.isMakeShots())
        {
            TakesScreenshot shoter = (TakesScreenshot)getDriverForEvent(event);
            byte[] shot = shoter.getScreenshotAs(OutputType.BYTES);
            File dir = new File(CONFIG.getScreenDir() + File.separator
                    + Paths.get(UserScenario.getScenarioFilename()).getFileName().toString());

            if(!dir.mkdirs()){
                return;
            }

            try (FileOutputStream fos = new FileOutputStream(new String(dir.getAbsolutePath() + File.separator
                    + String.format("%05d", UserScenario.getPosition()) + ".png")))
            {
                fos.write(shot);
            }
            catch (IOException e)
            {
                log.error(e.toString(), e);
            }
        }
    }

    public static void openEventUrl(JSONObject event)
    {
        String event_url = event.getString("url");
        WebDriver wd = getDriverForEvent(event);

        resizeForEvent(wd, event);
        if (wd.getCurrentUrl().equals("about:blank") || !getLastUrl(event).equals(event_url))
        {
            wd.get(event_url);

            waitUiShow(wd);
            waitPageReady(event);
            updateLastUrl(event, event_url);
        }
    }

    public static void processKeyboardEvent(JSONObject event, WebElement element)
    {
        if (event.getString("type").equalsIgnoreCase(EventType.KEY_PRESS))
        {
            if (event.has("charCode"))
            {
                if (!element.getTagName().contains("iframe"))
                {
                    char ch = (char)event.getBigInteger(("charCode")).intValue();
                    char keys[] = new char[1];
                    keys[0] = ch;
                    //element.sendKeys(new String(keys));
                    element.sendKeys("UI Recording" + System.currentTimeMillis());
                }
                else
                {
                    WebDriver wd = getDriverForEvent(event);
                    WebDriver frame = wd.switchTo().frame(element);
                    WebElement editor = frame.findElement(By.tagName("body"));
                    editor.sendKeys("UI Recording" + System.currentTimeMillis());
                    wd.switchTo().defaultContent();
                }
            }
        }

        if (event.getString("type").equalsIgnoreCase(EventType.KEY_UP))
        {
            if (event.has("charCode"))
            {
                int code = event.getBigInteger(("charCode")).intValue();

                if (event.getBoolean("ctrlKey") == true)
                {
                    element.sendKeys(Keys.chord(Keys.CONTROL, new String(new byte[] { (byte)code })));
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
                    }
                }
            }
        }
    }

    public static void processMouseEvent(JSONObject event, WebElement element)
    {
        WebDriver wd = getDriverForEvent(event);
        ensureElementInWindow(wd, element);
        if (element.isDisplayed())
        {
            if (event.getInt("button") == 2)
            {
                new Actions(wd).contextClick(element).perform();
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
            log.warn("Element is not visible : " + element.toString());
        }
    }

    public static void processScroll(JSONObject event, String target)
    {
        WebDriver wd = getDriverForEvent(event);
        long timeout = System.currentTimeMillis() + 20000l;
        if (checkElementPresent(wd, target))
        {
            return;
        }
        do
        {
            waitPageReady(event);
            WebElement el = getMax(wd);
            scroll((JavascriptExecutor)wd, el);
            if (checkElementPresent(wd, target))
            {
                return;
            }
        }
        while (System.currentTimeMillis() < timeout);
        throw new RuntimeException("Element was not found during scroll");
    }

    public static void resetLastUrls()
    {
        lastUrls.clear();
    }

    public static void updateLastUrl(JSONObject event, String url)
    {
        lastUrls.put(UserScenario.getTagForEvent(event), url);
    }

    public static void waitPageReady(JSONObject event)
    {
        if (event.getString("type").equalsIgnoreCase(EventType.XHR))
        {
            return;
        }
        int timeout = Integer.parseInt(CONFIG.getPageReadyTimeout()) * 1000;
        try
        {
            int sleeps = 0;
            JavascriptExecutor js = (JavascriptExecutor)getDriverForEvent(event);
            while (sleeps < timeout)
            {
                Thread.sleep(2000);
                String CHECK_PAGE_READY_JS = "return (document.getElementById('state.dispatch')==null || document.getElementById('state.dispatch').getAttribute('value')==0) &&  (document.getElementById('state.context')==null ||  document.getElementById('state.context').getAttribute('value')=='ready');";
                Object result = js.executeScript(CHECK_PAGE_READY_JS);
                if (result != null && Boolean.parseBoolean(result.toString().toLowerCase()) == true)
                {
                    break;
                }
                Thread.sleep(1 * 1000);
                sleeps += 1000;
            }
            Thread.sleep(2 * 1000);
        }
        catch (InterruptedException e)
        {
            log.error(e.toString(), e);
            throw new RuntimeException(e);
        }
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

    private static String getCSSSelector(JSONObject event)
    {
        if (!event.has("target1"))
        {
            return "";
        }
        JSONArray array = event.getJSONArray("target1");
        if (array.isNull(0))
        {
            return "";
        }

        String target = array.getJSONObject(0).getString("gecp");
        return target;
    }

    private static String getDomain(String url) throws MalformedURLException
    {
        URL aURL = new URL(url);
        return aURL.getHost() + ":" + aURL.getPort();
        /*
        URL aURL = new URL("http://example.com:80/docs/books/tutorial" + "/index.html?name=networking#DOWNLOADING");
        
        System.out.println("protocol = " + aURL.getProtocol()); //http
        System.out.println("authority = " + aURL.getAuthority()); //example.com:80
        System.out.println("host = " + aURL.getHost()); //example.com
        System.out.println("port = " + aURL.getPort()); //80
        System.out.println("path = " + aURL.getPath()); //  /docs/books/tutorial/index.html
        System.out.println("query = " + aURL.getQuery()); //name=networking
        System.out.println("filename = " + aURL.getFile()); ///docs/books/tutorial/index.html?name=networking
        System.out.println("ref = " + aURL.getRef()); //DOWNLOADING
        */
    }

    private static String getDomainPath(String url) throws MalformedURLException
    {
        URL aURL = new URL(url);
        return aURL.getPath();
        /*
        URL aURL = new URL("http://example.com:80/docs/books/tutorial" + "/index.html?name=networking#DOWNLOADING");
        
        System.out.println("protocol = " + aURL.getProtocol()); //http
        System.out.println("authority = " + aURL.getAuthority()); //example.com:80
        System.out.println("host = " + aURL.getHost()); //example.com
        System.out.println("port = " + aURL.getPort()); //80
        System.out.println("path = " + aURL.getPath()); //  /docs/books/tutorial/index.html
        System.out.println("query = " + aURL.getQuery()); //name=networking
        System.out.println("filename = " + aURL.getFile()); ///docs/books/tutorial/index.html?name=networking
        System.out.println("ref = " + aURL.getRef()); //DOWNLOADING
        */
    }

    private static WebElement getMax(WebDriver wd)
    {
        List<WebElement> els = wd.findElements(By.xpath("//div[@id='gwt-debug-PopupListSelect']//div[@__idx]"));
        els.sort((WebElement el1, WebElement el2) -> Integer.valueOf(el1.getAttribute("__idx"))
                .compareTo(Integer.valueOf(el2.getAttribute("__idx"))));
        return els.get(els.size() - 1);
    }

    private static void makeElementVisibleByJS(JSONObject event, String target)
    {
        JavascriptExecutor js = (JavascriptExecutor)getDriverForEvent(event);
        js.executeScript(String.format(SET_ELEMENT_VISIBLE_JS, target));
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

        if(w==0) {
            w = 1000;
        }

        if(h==0) {
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
        throw new RuntimeException("UI didn`t show up. =(");
    }
}
