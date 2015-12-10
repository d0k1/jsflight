package com.focusit.jsflight.player.input;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class FileInput {

	public static List<String> getContent(String file) throws IOException{
		return FileUtils.readLines(new File(file));
	}
	
	public static String getContentInString(String file) throws IOException{
        byte[] encoded = Files.readAllBytes(Paths.get(file));
        return new String(encoded, Charset.forName("UTF-8"));
	}
	
}
