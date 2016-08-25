package com.focusit.recorder.internalevent.httprequest;

import java.util.HashMap;

public class HttpRecordInformation
{
    public byte[] params;
    public byte[] payload;
    public HashMap<String, String> additional = new HashMap<>();
}
