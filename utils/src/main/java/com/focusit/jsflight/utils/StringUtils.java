package com.focusit.jsflight.utils;

/**
 * This class contains static utility methods to deal with strings
 * Created by Gallyam Biktashev on 22.08.16.
 */
// TODO Rewrite to Use Apache Commons Utils. Methods below method has been written a thousand times at least!
public class StringUtils
{
    public static boolean isNullOrEmptyOrWhiteSpace(String path)
    {
        return path == null || isEmptyOrWhiteSpace(path);
    }

    public static boolean isEmptyOrWhiteSpace(String str)
    {

        return str.isEmpty() || isWhiteSpace(str);
    }

    public static boolean isWhiteSpace(String str)
    {
        int length = str.length();
        int middle = length / 2;
        if (!Character.isWhitespace(str.charAt(middle)))
        {
            return false;
        }
        for (int i = 0; i < middle; i++)
        {
            if (!Character.isWhitespace(str.charAt(i)) || !Character.isWhitespace(str.charAt(length - 1 - i)))
                return false;
        }
        return true;
    }
}
