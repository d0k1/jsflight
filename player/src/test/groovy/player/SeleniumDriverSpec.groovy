package player

import org.json.JSONObject
import org.openqa.selenium.WebDriver

import spock.lang.Shared
import spock.lang.Specification

import com.focusit.jsflight.player.scenario.UserScenario
import com.focusit.jsflight.player.webdriver.SeleniumDriver
import com.focusit.script.jmeter.JMeterJSFlightBridge

class SeleniumDriverSpec extends Specification {

    @Shared formXp = "//*[@id='errorPageContainer']";
    @Shared testTag = 'testTag';

    def sd;

    def setup(){
        sd = new SeleniumDriver(new UserScenario())
    }

    def "tab containing form is not closed"(){

        JSONObject testEvent = new JSONObject();
        testEvent.put("tabuuid", "1")

        JSONObject testEvent2 = new JSONObject();
        testEvent2.put('tabuuid', '2')

        WebDriver wd = sd.getDriverForEvent(testEvent, true, '','','','')
        def firstHandle = wd.getWindowHandle();

        WebDriver wd2 = sd.getDriverForEvent(testEvent2, true, '','','','')
        def secondHandle = wd2.getWindowHandle()
        wd.get('http://localhost')

        when:
        sd.releaseBrowser(wd, formXp)

        then:
        wd.getWindowHandles().size() == 1
        wd.getWindowHandle().equals(secondHandle)
    }

    def "previous webdrivers without active form are closed"() {

        String tag1 = 'empl1'
        String tag2 = 'empl2'

        JMeterJSFlightBridge.TAG_FIELD = testTag

        JSONObject testEvent1Tag1 = new JSONObject();
        testEvent1Tag1.put("tabuuid", "1")
        testEvent1Tag1.put(testTag, tag1)

        JSONObject testEvent2Tag1 = new JSONObject();
        testEvent2Tag1.put('tabuuid', '2')
        testEvent1Tag1.put(testTag, tag1)

        JSONObject testEvent1Tag2 = new JSONObject();
        testEvent1Tag2.put('tabuuid', '3');
        testEvent1Tag2.put('testTag', tag2)

        JSONObject testEvent2Tag2 = new JSONObject();
        testEvent2Tag2.put('tabuuid', '4');
        testEvent2Tag2.put('testTag', tag2)

        def wd1 = getWd(testEvent1Tag1);
        def wd2 = getWd(testEvent2Tag1);

        def wd3 = getWd(testEvent1Tag2);

        def wd4 = getWd(testEvent2Tag2);

        when:
        sd.releaseBrowser(wd4, formXp)

        then:
        sd.drivers.get(tag1) == null
    }

    def "current active tab is not closed"(){
        JSONObject testEvent = new JSONObject();
        testEvent.put("tabuuid", "5")
        testEvent.put(testTag, "testActive")
        JSONObject testEvent2 = new JSONObject();
        testEvent2.put('tabuuid', '6')
        testEvent2.put(testTag, "testActive")

        def wdForTestEvent = getWd(testEvent);
        def firstHandle = wdForTestEvent.getWindowHandle();

        def wdForTestEvent2 = getWd(testEvent2)
        def secondHandle = wdForTestEvent2.getWindowHandle();

        when:
        sd.releaseBrowser(wdForTestEvent, formXp)

        then:
        wdForTestEvent.getWindowHandles().size() == 1 && wdForTestEvent.getWindowHandle().equals(secondHandle)
    }

    def "closed tabs window handle mappings are removed"(){
        JSONObject testEvent = new JSONObject();
        testEvent.put("tabuuid", "7")
        testEvent.put(testTag, "testClose")

        JSONObject testEvent2 = new JSONObject();
        testEvent2.put('tabuuid', '8')
        testEvent2.put(testTag, "testClose")

        def wdForTestEvent = getWd(testEvent);
        def firstHandle = wdForTestEvent.getWindowHandle();

        def wdForTestEvent2 = getWd(testEvent2)
        def secondHandle = wdForTestEvent2.getWindowHandle();

        when:
        sd.releaseBrowser(wdForTestEvent2, formXp)

        then:
        sd.tabsWindow.get('7') == null
    }

    def cleanup(){
        sd.drivers.values().each {it ->
            it.quit()
        }
        sd.drivers.clear()
    }

    WebDriver getWd(def event){
        return sd.getDriverForEvent(event, true, '','','','')
    }
}
