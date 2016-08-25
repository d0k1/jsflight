package com.focusit.jsflight.recorder.internalevent;

/**
 * Created by dkolmogortsev on 22.08.16.
 */
public class IdRecordInfo
{
    public static final String ID_RECORD_TAG = "idRecording";

    private String tag;
    private long generatedId;
    private String type;

    public IdRecordInfo()
    {

    }

    public IdRecordInfo(String tag, String type, long generatedId)
    {
        this.tag = tag;
        this.type = type;
        this.generatedId = generatedId;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    public long getGeneratedId()
    {
        return generatedId;
    }

    public void setGeneratedId(long generatedId)
    {
        this.generatedId = generatedId;
    }

}
