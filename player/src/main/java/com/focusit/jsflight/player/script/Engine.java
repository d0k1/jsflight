package com.focusit.jsflight.player.script;

import com.focusit.jmeter.ScriptsClassLoader;
import com.focusit.jsflight.player.context.PlayerContext;
import com.focusit.jsflight.player.scenario.UserScenario;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.text.SimpleTemplateEngine;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Engine that runs groovy scripts or GString templates
 * 
 * @author Denis V. Kirpichenkov
 *
 */
public class Engine
{
    private static final Logger LOG = LoggerFactory.getLogger(Engine.class);
    private static final ConcurrentHashMap<Object, Object> context = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Script> scripts = new ConcurrentHashMap<>();
    private GroovyShell shell;
    private String script;

    public Engine()
    {
        shell = new GroovyShell(new ScriptsClassLoader(this.getClass().getClassLoader()));
        this.script = null;
    }

    public Engine(String script)
    {
        this();
        this.script = script;
        compileScript(script);
    }

    public boolean executeDuplicateHandlerScript(JSONObject currentEvent, JSONObject prevEvent)
    {
        Binding binding = new Binding();
        binding.setVariable("current", currentEvent);
        binding.setVariable("previous", prevEvent);
        binding.setVariable("logger", LOG);
        binding.setVariable("classloader", shell.getClassLoader());
        Script scr = scripts.get(script);
        scr.setBinding(binding);
        return (boolean)scr.run();
    }

    public Object executeWebLookupScript(WebDriver wd, String target, JSONObject event)
    {
        Binding binding = new Binding();
        binding.setVariable("webdriver", wd);
        binding.setVariable("target", target);
        binding.setVariable("event", event);
        binding.setVariable("logger", LOG);
        binding.setVariable("classloader", shell.getClassLoader());
        Script scr = scripts.get(script);
        scr.setBinding(binding);
        return scr.run();

    }

    public String getScript()
    {
        return script;
    }

    /**
     * Modifies existing array of events.
     * Modification defined by script
     * @param events
     */
    public void postProcessScenario(List<JSONObject> events)
    {
        Binding binding = new Binding();
        binding.setVariable("context", context);
        binding.setVariable("events", events);
        binding.setVariable("classloader", shell.getClassLoader());
        Script s = scripts.get(script);
        s.setBinding(binding);
        s.run();
    }

    public void runStepPrePostScript(UserScenario scenario, int step, boolean pre)
    {
        String stepScript = "";
        JSONObject event = scenario.getStepAt(step);

        if (pre)
        {
            stepScript = event.has("pre") ? event.getString("pre") : "";
        }
        else
        {
            stepScript = event.has("post") ? event.getString("post") : "";
        }

        if (stepScript.trim().length() == 0)
        {
            return;
        }

        compileScript(stepScript);
        Binding binding = new Binding();
        binding.setVariable("context", PlayerContext.getInstance());
        binding.setVariable("scenario", scenario);
        binding.setVariable("step", step);
        binding.setVariable("pre", pre);
        binding.setVariable("post", !pre);
        binding.setVariable("classloader", shell.getClassLoader());
        Script s = scripts.get(stepScript);
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
                    LOG.error(e.toString(), e);
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
    public void testPostProcess(List<JSONObject> events)
    {
        Binding binding = new Binding();
        binding.setVariable("context", context);
        binding.setVariable("events", new ArrayList<>(events));
        binding.setVariable("classloader", shell.getClassLoader());
        Script s = scripts.get(script);
        s.setBinding(binding);
        s.run();
    }

    private void compileScript(String script)
    {
        if (script != null && !script.trim().isEmpty())
        {
            if (scripts.get(script) == null)
            {
                scripts.put(script, shell.parse(script));
            }
        }

    }
}
