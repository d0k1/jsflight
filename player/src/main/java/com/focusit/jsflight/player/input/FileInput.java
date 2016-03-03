package com.focusit.jsflight.player.input;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * File read / write
 * 
 * @author Denis V. Kirpichenkov
 *
 */
public class FileInput
{

    public static List<String> getContent(String file) throws IOException
    {
        return FileUtils.readLines(new File(file));
    }

    public static String getContentInString(String file) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(file));
        return new String(encoded, Charset.forName("UTF-8"));
    }

    public static void saveEvents(List<JSONObject> events, String string) throws IOException
    {
        JSONArray array = new JSONArray();
        for (JSONObject o : events)
        {
            array.put(o);
        }
        String data = array.toString();
        FileUtils.writeStringToFile(new File(string), data);
        data = array.toString(3);
        FileUtils.writeStringToFile(new File(string + ".pretty"), data);
    }

}
