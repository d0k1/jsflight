import spock.lang.Specification
import com.focusit.script.ScriptEngine;
/**
 * Created by dkirpichenkov on 06.05.16.
 */

class ScriptEngineSpec extends Specification {
    public static final String SCRIPT_WITH_RETURN = "return 1;";
    public static final String MULTITHREAD_SCRIPT = "return Thread.currentThread().getId();"
    public static final String VAR1 = "var1"
    public static final String VAL1 = "val1"

    def setup() {
        ScriptEngine.init(this.getClass().getClassLoader());
    }

    def "call getScript with null returns null"() {
        def script;
        when:
        script = ScriptEngine.getScript(null);
        then:
        script == null;
    }

    def "call getScript with empty string returns null"() {
        def script;
        when:
        script = ScriptEngine.getScript("");
        then:
        script == null;
    }

    def "call getScript with blank string returns null"() {
        def script;
        when:
        script = ScriptEngine.getScript("    ");
        then:
        script == null;
    }
}