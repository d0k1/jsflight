package com.focusit.jsflight.player.config;

/**
 * Created by dkirpichenkov on 06.05.16.
 */
public class Configuration {
    private CommonConfiguration commonConfiguration;
    private JMeterConfiguration jMeterConfiguration;
    private ScriptEventConfiguration scriptEventConfiguration;
    private WebLookupConfiguration webLookupConfiguration;

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
