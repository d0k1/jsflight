package com.focusit.jsflight.player.cli;

import com.beust.jcommander.Parameter;

public class CliConfig
{
    public static final String DEFAULT_RECORDING_NAME = "test.jmx";

    @Parameter(names = { "-hl", "--headless" }, description = "Launching in headless mode")
    private boolean headless = false;

    @Parameter(names = { "-p", "--path" }, description = "Path to recording json")
    private String pathToRecording = "";

    @Parameter(names = { "-ph", "--proxyhost" }, description = "Selenium proxy host")
    private String proxyHost = "";

    @Parameter(names = { "-pp", "--proxyport" }, description = "Selenium proxy port")
    private String proxyPort = "";

    @Parameter(names = { "-tp", "--templatepath" }, description = "Path to jmx template to generate scenario")
    private String jmxTemplatePath = "";

    @Parameter(names = { "-r", "--recording" }, description = "Record browser interactions with Jmeter")
    private boolean enableRecording = false;

    @Parameter(names = { "-f", "--firefox" }, description = "Use Firefox as browser")
    private boolean useFifefox = true;

    @Parameter(names = { "-pj", "--phantomjs" }, description = "Use phantomjs as browser")
    private boolean usePhantomJS = false;

    @Parameter(names = { "-ffp", "--firefoxpath" }, description = "Path to firefox binary")
    private String ffPath = "";

    @Parameter(names = { "-pjp", "--phantomjspath" }, description = "Path to phantomjs binary")
    private String pjsPath = "";

    @Parameter(names = { "-s", "--shots" }, description = "Need to make screenshots")
    private boolean makeShots = true;

    @Parameter(names = { "-sp", "--screenshotspath" }, description = "Directory to store screenshots")
    private String pathToScreenShots = "shots";

    @Parameter(names = { "-jrn", "--jmeterrecordingname" }, description = "JMX recording file name")
    private String jmeterRecordingName = DEFAULT_RECORDING_NAME;

    @Parameter(names = { "-pt", "--pagereadytimout" }, description = "Timeout for page to get ready")
    private String pageReadyTimeout = "30";

    @Parameter(names = { "-h", "--help" }, description = "Show this help")
    private boolean help = false;

    public String getFfPath()
    {
        return ffPath;
    }

    public String getJmeterRecordingName()
    {
        return jmeterRecordingName;
    }

    public String getJmxTemplatePath()
    {
        return jmxTemplatePath;
    }

    public boolean getMakeShots()
    {
        return makeShots;
    }

    public String getPageReadyTimeout()
    {
        return pageReadyTimeout;
    }

    public String getPathToRecording()
    {
        return pathToRecording;
    }

    public String getPjsPath()
    {
        return pjsPath;
    }

    public String getProxyHost()
    {
        return proxyHost;
    }

    public String getProxyPort()
    {
        return proxyPort;
    }

    public String getScreenDir()
    {
        return pathToScreenShots;
    }

    public boolean isEnableRecording()
    {
        return enableRecording;
    }

    public boolean isHeadLess()
    {
        return headless;
    }

    public boolean isUseFifefox()
    {
        return useFifefox;
    }

    public boolean isUsePhantomJS()
    {
        return usePhantomJS;
    }

    public boolean showHelp()
    {
        return help;
    }
}
