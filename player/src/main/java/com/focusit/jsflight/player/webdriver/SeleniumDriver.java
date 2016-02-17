package com.focusit.jsflight.player.webdriver;

import org.json.JSONObject;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class SeleniumDriver
{

    public static WebDriver getDriverForEvent(JSONObject event)
    {
        /*
        String tag = getTagForEvent(event);

        WebDriver driver = drivers.get(tag);

        try
        {
            if (driver != null)
            {
                return driver;
            }

            FirefoxProfile profile = new FirefoxProfile();
            DesiredCapabilities cap = new DesiredCapabilities();
            if (proxyHost.getText().trim().length() > 0)
            {
                String host = proxyHost.getText();
                if (proxyPort.getText().trim().length() > 0)
                {
                    host += ":" + proxyPort.getText();
                }
                org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
                proxy.setHttpProxy(host).setFtpProxy(host).setSslProxy(host);
                cap.setCapability(CapabilityType.PROXY, proxy);
            }
            if (useFirefoxButton.isSelected())
            {
                if (ffPath.getText() != null && ffPath.getText().trim().length() > 0)
                {
                    FirefoxBinary binary = new FirefoxBinary(new File(ffPath.getText()));
                    driver = new FirefoxDriver(binary, profile, cap);
                }
                else
                {
                    driver = new FirefoxDriver(new FirefoxBinary(), profile, cap);
                }
            }
            else if (usePhantomButton.isSelected())
            {
                if (pjsPath.getText() != null && pjsPath.getText().trim().length() > 0)
                {
                    cap.setCapability("phantomjs.binary.path", pjsPath.getText());
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
        */
        return null;
    }

    private static WebElement findTargetWebElement(JSONObject event, String target)
    {
        /*
        makeElementVisibleByJS(event, target);
        return getDriverForEvent(event).findElement(By.xpath(target));
        */
        return null;
    }

    private void makeAShot(JSONObject event)
    {
        /*
        if (makeShots.isSelected())
        {
            TakesScreenshot shoter = (TakesScreenshot)getDriverForEvent(event);
            byte[] shot = shoter.getScreenshotAs(OutputType.BYTES);
            File dir = new File(screenDirTextField.getText() + File.separator
                    + Paths.get(filenameField.getText()).getFileName().toString());
            dir.mkdirs();

            try (FileOutputStream fos = new FileOutputStream(new String(dir.getAbsolutePath() + File.separator
                    + String.format("%05d", position) + ".png")))
            {
                fos.write(shot);
            }
            catch (IOException e)
            {
                log.error(e.toString(), e);
            }
        }
        */
    }

    private void makeElementVisibleByJS(JSONObject event, String target)
    {
        /*
        JavascriptExecutor js = (JavascriptExecutor)getDriverForEvent(event);
        js.executeScript(String.format(SET_ELEMENT_VISIBLE_JS, target));
        */
    }

    private void openEventUrl(JSONObject event)
    {
        /*
        String event_url = event.getString("url");

        if (!getLastUrl(event).equals(event_url))
        {
            getDriverForEvent(event).get(event_url);
            int maxDelay = Integer.parseInt(maxStepDelayField.getText()) * 1000;
            try
            {
                int sleeps = 0;
                while (sleeps < maxDelay)
                {
                    JavascriptExecutor js = (JavascriptExecutor)getDriverForEvent(event);
                    Object result = js.executeScript(checkPageJs.getText());
                    if (result != null && result.toString().toLowerCase().equals("ready"))
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
        */
    }

    private void processKeyboardEvent(JSONObject event, WebElement element)
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

    private void processMouseEvent(JSONObject event, WebElement element)
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

}
