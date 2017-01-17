/**
 * Created by doki on 06.05.16.
 */


import com.focusit.jsflight.script.jmeter.JMeterJSFlightBridge
import com.focusit.jsflight.script.player.PlayerContext
import org.json.JSONObject
import spock.lang.Specification

class PlayerContextSpec extends Specification {
    public static final String TEST_KEY = "testKey";
    public static final String TEST_VALUE = "testValue";

    PlayerContext ctx = new PlayerContext();

    def "can put an element to the context"() {
        when:
        ctx.put(TEST_KEY, TEST_VALUE);
        then:
        ctx.get(TEST_KEY) != null;
        ctx.get(TEST_KEY).equals(TEST_VALUE);
    }

    def "context is empty after clean"() {
        ctx.put("123", "321");
        when:
        ctx.put(TEST_KEY, TEST_VALUE);
        then:
        ctx.get(TEST_KEY) != null;

        when:
        ctx.reset();
        then:
        ctx.get(TEST_KEY) == null;
        ctx.get("123") == null;
    }

    def "can set current scenario step in jmeter's bridge"() {
        JMeterJSFlightBridge bridge = JMeterJSFlightBridge.getInstance();
        JSONObject step = new JSONObject();
        def current = bridge.getCurrentScenarioStep();

        when:
        ctx.setCurrentScenarioStep(step);
        then:
        bridge.getCurrentScenarioStep() == step;
        cleanup:
        bridge.setCurrentScenarioStep(current);
    }
}
