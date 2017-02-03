package player

import com.focusit.jsflight.player.scenario.UserScenario
import com.focusit.jsflight.player.script.PlayerScriptProcessor
import com.focusit.jsflight.script.ScriptEngine
import org.bson.types.ObjectId
import org.json.JSONObject
import spock.lang.Specification


/**
 * Created by doki on 03.02.17.
 */
class PlayerScriptProcessorSpec extends Specification {

    def "every step field could be evaluated by template engine"() {
        given:
        ScriptEngine.init(System.getClassLoader());
        PlayerScriptProcessor proc = new PlayerScriptProcessor();
        UserScenario scenario = new UserScenario();
        scenario.getContext().put("variable", "ya.ru");

        JSONObject event = new JSONObject();
        event.put("id", new ObjectId());
        event.put("url", 'http://${variable}');
        when:
        JSONObject result = proc.runStepTemplating(scenario, event);
        then:
        result.get("url") == "http://ya.ru"
    }
}