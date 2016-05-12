package com.focusit.jsflight.player.config;

/**
 * Created by dkirpichenkov on 06.05.16.
 */
public class WebConfiguration
{

    private String lookupScriptFilename = "";
    private String lookupScript = "";

    private String duplicationScriptFilename = "";
    private String duplicationScript = "";

    private String findBrowserErrorScript = "return false;";

    public String getLookupScriptFilename()
    {
        return lookupScriptFilename;
    }

    public void setLookupScriptFilename(String filename)
    {
        this.lookupScriptFilename = filename;
    }

    public String getLookupScript()
    {
        return lookupScript;
    }

    public void setLookupScript(String script)
    {
        this.lookupScript = script;
    }

    public String getDuplicationScriptFilename()
    {
        return duplicationScriptFilename;
    }

    public void setDuplicationScriptFilename(String duplicationScriptFilename)
    {
        this.duplicationScriptFilename = duplicationScriptFilename;
    }

    public String getDuplicationScript()
    {
        return duplicationScript;
    }

    public void setDuplicationScript(String duplicationScript)
    {
        this.duplicationScript = duplicationScript;
    }

    public void loadDefaults(){

    }

    public String getFindBrowserErrorScript() {
        return findBrowserErrorScript;
    }

    public void setFindBrowserErrorScript(String findBrowserErrorScript) {
        this.findBrowserErrorScript = findBrowserErrorScript;
    }
}
