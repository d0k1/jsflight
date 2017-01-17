import com.focusit.jsflight.script.jmeter.JMeterJSFlightBridge
import org.json.JSONObject
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * Created by dkirpichenkov on 06.05.16.
 */

@Stepwise
class JMeterJSFlightBridgeSpec extends Specification {

    def "current step after construcor is literally null"() {
        JMeterJSFlightBridge bridge = JMeterJSFlightBridge.getInstance();
        when:
        bridge;
        then:
        bridge.getCurrentScenarioStep() == bridge.NO_SCENARIO_STEP;
        bridge.isCurrentStepEmpty();
    }

    def "can change current step"() {
        JMeterJSFlightBridge bridge = JMeterJSFlightBridge.getInstance();
        def current = bridge.getCurrentScenarioStep();
        def step = new JSONObject();

        when:
        bridge.setCurrentScenarioStep(step);
        then:
        bridge.getCurrentScenarioStep() == step;
        cleanup:
        bridge.setCurrentScenarioStep(current);
    }

    def "can save current step by given sampler"() {
        JMeterJSFlightBridge bridge = JMeterJSFlightBridge.getInstance();
        def step = new JSONObject();
        def sampler = new Object();
        when:
        bridge.setCurrentScenarioStep(step);
        bridge.addSampler(sampler);
        then:
        bridge.getSourceEvent(sampler) == step;
    }
}