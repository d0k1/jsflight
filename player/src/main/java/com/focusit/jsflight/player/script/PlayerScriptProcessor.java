package com.focusit.jsflight.player.script;

import com.focusit.jsflight.player.scenario.UserScenario;
import com.focusit.script.ScriptEngine;
import com.focusit.script.player.PlayerContext;
import groovy.lang.Binding;
import groovy.lang.Script;
import groovy.text.SimpleTemplateEngine;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PlayerScriptProcessor that runs groovy scripts or GString templates
 * 
 * @author Denis V. Kirpichenkov
 *
 */
public class PlayerScriptProcessor
{
    private static final Logger log = LoggerFactory.getLogger(PlayerScriptProcessor.class);

    public PlayerScriptProcessor()
    {
    }

    public PlayerScriptProcessor(String script)
    {
        this();
    }

    public boolean executeDuplicateHandlerScript(String script, JSONObject currentEvent, JSONObject prevEvent)
    {
        Binding binding = new Binding();
        binding.setVariable("current", currentEvent);
        binding.setVariable("previous", prevEvent);
        binding.setVariable("logger", log);
        binding.setVariable("classloader", ScriptEngine.getInstance().getClassLoader());
        Script scr = ScriptEngine.getInstance().getScript(script);
        scr.setBinding(binding);
        return (boolean)scr.run();
    }

    public Object executeWebLookupScript(String script, WebDriver wd, String target, JSONObject event)
    {
        Binding binding = new Binding();
        binding.setVariable("webdriver", wd);
        binding.setVariable("target", target);
        binding.setVariable("event", event);
        binding.setVariable("logger", log);
        binding.setVariable("classloader", ScriptEngine.getInstance().getClassLoader());
        Script scr = ScriptEngine.getInstance().getScript(script);
        scr.setBinding(binding);
        return scr.run();

    }

    /**
     * Modifies existing array of events.
     * Modification defined by script
     * @param events
     */
    public void postProcessScenario(String script, List<JSONObject> events)
    {
        Binding binding = new Binding();
        //binding.setVariable("context", context);
        binding.setVariable("events", events);
        binding.setVariable("classloader", ScriptEngine.getInstance().getClassLoader());
        Script s = ScriptEngine.getInstance().getScript(script);
        s.setBinding(binding);
        s.run();
    }

    public void runStepPrePostScript(UserScenario scenario, int step, boolean pre)
    {
        String script = "";
        JSONObject event = scenario.getStepAt(step);

        if (pre)
        {
            script = event.has("pre") ? event.getString("pre") : "";
        }
        else
        {
            script = event.has("post") ? event.getString("post") : "";
        }

        if (script.trim().length() == 0)
        {
            return;
        }

        Binding binding = new Binding();
        binding.setVariable("context", PlayerContext.getInstance());
        binding.setVariable("scenario", scenario);
        binding.setVariable("step", step);
        binding.setVariable("pre", pre);
        binding.setVariable("post", !pre);
        binding.setVariable("classloader", ScriptEngine.getInstance().getClassLoader());
        Script s = ScriptEngine.getInstance().getScript(script);
        s.setBinding(binding);
        s.run();
    }

    public JSONObject runStepTemplating(JSONObject step)
    {
        SimpleTemplateEngine templateEngine = new SimpleTemplateEngine();
        Binding binding = PlayerContext.getInstance().asBindings();
        JSONObject result = new JSONObject(step.toString());
        result.keySet().forEach(key -> {
            if (result.get(key) instanceof String)
            {
                try
                {
                    String source = result.getString(key);

                    String pattern = "[^\\\\](\\$)(.)";
                    Pattern r = Pattern.compile(pattern);
                    Matcher m = r.matcher(source);
                    while (m.find())
                    {
                        if (!m.group(1).equalsIgnoreCase("{"))
                        {
                            int start = m.start(0);
                            source = source.substring(0, start + 1) + "\\$"
                                    + source.substring(start + 2, source.length());
                        }
                        m = r.matcher(source);
                    }

                    String parsed = templateEngine.createTemplate(source).make(binding.getVariables()).toString();
                    result.put(key, parsed);
                }
                catch (Exception e)
                {
                    log.error(e.toString(), e);
                }
            }
        });
        return result;
    }

    /**
     * Test of events modification script.
     * Doesn't modify real loaded events. just use it's clone, modify and println every event to stdout
     * @param events
     */
    public void testPostProcess(String script, List<JSONObject> events)
    {
        Binding binding = new Binding();
//        binding.setVariable("context", context);
        binding.setVariable("events", new ArrayList<>(events));
        binding.setVariable("classloader", ScriptEngine.getInstance().getClassLoader());
        Script s = ScriptEngine.getInstance().getScript(script);
        s.setBinding(binding);
        s.run();
    }

}
