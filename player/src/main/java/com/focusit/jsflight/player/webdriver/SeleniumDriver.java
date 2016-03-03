package com.focusit.jsflight.player.webdriver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
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

import com.focusit.jsflight.player.scenario.UserScenario;

/**
 * Selenium webdriver helper: runs a browser, sends events, make screenshots
 * 
 * @author Denis V. Kirpichenkov
 *
 */
public class SeleniumDriver
{
    public static String CHECK_PAGE_READY_JS = "return (document.getElementById('state.dispatch')==null || document.getElementById('state.dispatch').getAttribute('value')==0) &&  (document.getElementById('state.context')==null ||  document.getElementById('state.context').getAttribute('value')=='ready');";
    private static final String SET_ELEMENT_VISIBLE_JS = "var e = document.evaluate('%s' ,document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null ).singleNodeValue; if(e!== null) {e.style.visibility='visible';};";

    private static final Logger log = LoggerFactory.getLogger(SeleniumDriver.class);
    private static boolean useFirefox = true;
    private static boolean usePhantomJs = false;
    private static String proxyHost = "";
    private static String proxyPort = "";
    private static String ffPath = "";
    private static String pjsPath = "";
    private static boolean makeShots = true;
    private static String screenDir = "shots";
    private static String maxDelayTime = "30";

    private static HashMap<String, WebDriver> drivers = new HashMap<>();
    private static HashMap<String, String> tabsWindow = new HashMap<>();

    private static HashMap<String, String> lastUrls = new HashMap<>();

    public static WebElement findTargetWebElement(JSONObject event, String target)
    {
        makeElementVisibleByJS(event, target);
        return getDriverForEvent(event).findElement(By.xpath(target));
    }

    public static WebDriver getDriverForEvent(JSONObject event)
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
            if (proxyHost.trim().length() > 0)
            {
                String host = proxyHost;
                if (proxyPort.trim().length() > 0)
                {
                    host += ":" + proxyPort;
                }
                org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
                proxy.setHttpProxy(host).setFtpProxy(host).setSslProxy(host);
                cap.setCapability(CapabilityType.PROXY, proxy);
            }
            if (useFirefox)
            {
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
                Thread.sleep(7000);
            }
            catch (InterruptedException e)
            {
                log.error(e.toString(), e);
            }

            drivers.put(tag, driver);
            return driver;
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
            driver.switchTo().window(window);
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
        if (makeShots)
        {
            TakesScreenshot shoter = (TakesScreenshot)getDriverForEvent(event);
            byte[] shot = shoter.getScreenshotAs(OutputType.BYTES);
            File dir = new File(screenDir + File.separator
                    + Paths.get(UserScenario.getScenarioFilename()).getFileName().toString());
            dir.mkdirs();

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

        if (!getLastUrl(event).equals(event_url))
        {
            getDriverForEvent(event).get(event_url);
            int maxDelay = Integer.parseInt(maxDelayTime) * 1000;
            try
            {
                int sleeps = 0;
                while (sleeps < maxDelay)
                {
                    JavascriptExecutor js = (JavascriptExecutor)getDriverForEvent(event);
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
            updateLastUrl(event, event_url);
        }
    }

    public static void processKeyboardEvent(JSONObject event, WebElement element)
    {
        if (event.getString("type").equalsIgnoreCase("keypress"))
        {
            if (event.has("charCode"))
            {
                char ch = (char)event.getBigInteger(("charCode")).intValue();
                char keys[] = new char[1];
                keys[0] = ch;
                element.sendKeys(new String(keys));
            }
        }

        if (event.getString("type").equalsIgnoreCase("keyup"))
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
        if (event.getString("type").equalsIgnoreCase("mousedown"))
        {
            if (event.getInt("button") == 2)
            {
                new Actions(getDriverForEvent(event)).contextClick(element).perform();
            }
            else
            {
                element.click();
            }
        }
    }

    public static void resetLastUrls()
    {
        lastUrls.clear();
    }

    public static void updateLastUrl(JSONObject event, String url)
    {
        lastUrls.put(UserScenario.getTagForEvent(event), url);
    }

    private static void makeElementVisibleByJS(JSONObject event, String target)
    {
        JavascriptExecutor js = (JavascriptExecutor)getDriverForEvent(event);
        js.executeScript(String.format(SET_ELEMENT_VISIBLE_JS, target));
    }

}
