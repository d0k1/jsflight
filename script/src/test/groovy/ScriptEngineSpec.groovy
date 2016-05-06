import com.focusit.script.ScriptEngine
import spock.lang.Specification

/**
 * Created by dkirpichenkov on 06.05.16.
 */

class ScriptEngineSpec extends  Specification {
    public final static SCRIPT_WITH_RETURN = "return 1;";
    public static final String MULTITHREAD_SCRIPT = "return Thread.currentThread().getId();"
    public static final String VAR1 = "var1"
    public static final String VAL1 = "val1"

    ScriptEngine engine = ScriptEngine.getInstance();

    def "scripts with the same body will be cached"(){
        def result = null;
        when:
            result = engine.getScript(SCRIPT_WITH_RETURN);
        then:
            engine.getScript(SCRIPT_WITH_RETURN).equals(result);
            engine.getScript(SCRIPT_WITH_RETURN) == result;
    }

    def "call getScript with null returns null"(){
        def script;
        when:
            script = engine.getScript(null);
        then:
            script == null;
    }

    def "call getScript with empty string returns null"(){
        def script;
        when:
            script = engine.getScript("");
        then:
            script == null;
    }

    def "call getScript with blank string returns NO_SCRIPT object"(){
        def script;
        when:
            script = engine.getScript("    ");
        then:
            script == null;
    }

    def "call getScript from diffrent threads returns same object"(){
        def script1;
        def scriptFromThread = [];

        when:
            script1 = engine.getScript(MULTITHREAD_SCRIPT);
            script1.getBinding().setVariable(VAR1, VAL1);

            def thread = new Thread(new Runnable() {
                @Override
                void run() {
                    scriptFromThread << engine.getScript(MULTITHREAD_SCRIPT);
                }
            });
            thread.start();
            thread.join();

        then:
            def script2 = scriptFromThread.get(0) as Script;
            script1==script2;
            script2.getBinding().getVariable(VAR1)==VAL1;
    }

    def "call getThreadBindedScript from diffrent threads returns different objects"(){
        def scriptFromThread1 = [];
        def scriptFromThread2 = [];

        when:
            def thread = new Thread(new Runnable() {
                @Override
                void run() {
                    def script = engine.getThreadBindedScript(MULTITHREAD_SCRIPT);
                    script.getBinding().setVariable(VAR1, VAL1);
                    scriptFromThread1 << script;
                }
            });
            thread.start();
            thread.join();

            def thread2 = new Thread(new Runnable() {
                @Override
                void run() {
                    scriptFromThread2 << engine.getThreadBindedScript(MULTITHREAD_SCRIPT);
                }
            });
            thread2.start();
            thread2.join();
        then:
            def script1 = scriptFromThread1.get(0) as Script;
            def script2 = scriptFromThread2.get(0) as Script;
            script1!=script2;
            script1.getBinding().getVariable(VAR1)==VAL1;
            script2.getBinding().hasVariable(VAR1)==false;
    }
}