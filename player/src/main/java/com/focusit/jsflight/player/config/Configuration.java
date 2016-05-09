package com.focusit.jsflight.player.config;

/**
 * Player configuration holder
 * Created by dkirpichenkov on 06.05.16.
 */
public class Configuration {
    private CommonConfiguration commonConfiguration = new CommonConfiguration();
    private JMeterConfiguration jMeterConfiguration = new JMeterConfiguration();
    private ScriptEventConfiguration scriptEventConfiguration = new ScriptEventConfiguration();
    private WebLookupConfiguration webLookupConfiguration = new WebLookupConfiguration();

    public CommonConfiguration getCommonConfiguration() {
        return commonConfiguration;
    }

    public void setCommonConfiguration(CommonConfiguration commonConfiguration) {
        this.commonConfiguration = commonConfiguration;
    }

    public JMeterConfiguration getjMeterConfiguration() {
        return jMeterConfiguration;
    }

    public void setjMeterConfiguration(JMeterConfiguration jMeterConfiguration) {
        this.jMeterConfiguration = jMeterConfiguration;
    }

    public ScriptEventConfiguration getScriptEventConfiguration() {
        return scriptEventConfiguration;
    }

    public void setScriptEventConfiguration(ScriptEventConfiguration scriptEventConfiguration) {
        this.scriptEventConfiguration = scriptEventConfiguration;
    }

    public WebLookupConfiguration getWebLookupConfiguration() {
        return webLookupConfiguration;
    }

    public void setWebLookupConfiguration(WebLookupConfiguration webLookupConfiguration) {
        this.webLookupConfiguration = webLookupConfiguration;
    }
}
