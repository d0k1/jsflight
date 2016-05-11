package com.focusit.jsflight.player.fileconfigholder;


public class InputFileConfigHolder extends UIFileConfigHolder
{
    private static final long serialVersionUID = 1L;

    private final static InputFileConfigHolder instance = new InputFileConfigHolder();

    public static InputFileConfigHolder getInstance()
    {
        return instance;
    }

    private String filename = "";

    private InputFileConfigHolder()
    {
    }

    public String getFilename()
    {
        return filename;
    }

    @Override
    public void load(String file) throws Exception
    {
        filename = (String)getInputStream(file).readObject();
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    @Override
    public void store(String file) throws Exception
    {
        getOutputStream(file).writeObject(filename);
    }

    @Override
    protected String getControllerDataFilename()
    {
        return "input";
    }
}
