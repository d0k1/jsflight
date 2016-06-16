package player

import com.focusit.jsflight.player.scenario.UserScenario
import com.focusit.jsflight.player.webdriver.SeleniumDriver
import org.json.JSONObject
import org.openqa.selenium.WebDriver
import spock.lang.Shared
import spock.lang.Specification

class SeleniumDriverSpec extends Specification {

    @Shared
            formXp = "//*[@id='errorPageContainer']";


    def sd;

    def setup() {
        sd = new SeleniumDriver(new UserScenario())
    }

    def "browser containing form is not closed"() {

        JSONObject testEvent = new JSONObject();
        testEvent.put("tabuuid", "1")

        WebDriver wd = sd.getDriverForEvent(testEvent, true, '', '', '', '')

        wd.get('http://localhost')

        when:
        sd.releaseBrowser(wd, formXp, testEvent)

        then:
        !sd.drivers.isEmpty()
    }


    def "browser without form quits"() {
        JSONObject testEvent = new JSONObject();
        testEvent.put("tabuuid", "2")

        WebDriver wd = sd.getDriverForEvent(testEvent, true, '', '', '', '')

        when:
        sd.releaseBrowser(wd, formXp, testEvent)

        then:
        sd.drivers.isEmpty()
    }


    def cleanup() {
        sd.drivers.values().each { it ->
            it.quit()
        }
        sd.drivers.clear()
    }

    WebDriver getWd(def event) {
        return sd.getDriverForEvent(event, true, '', '', '', '')
    }
}
