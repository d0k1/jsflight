package com.focusit.jsflight.player;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Player {
	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	public static void main(String[] args) throws IOException {
		System.out.println("opening " + args[0]);
		String scenario = readFile(args[0], Charset.forName("UTF-8"));
		JSONArray rawevents = new JSONArray(scenario);
		List<JSONObject> events = new ArrayList<>();
		for(int i=0;i<rawevents.length();i++){
			String event = rawevents.getString(i);
			if(!event.contains("flight-cp")){
				events.add(new JSONObject(event));
			}
		}

		WebDriver driver = new FirefoxDriver();

		String url = "";
		for(JSONObject event:events){
			String event_url = event.getString("url");
			if(!url.equalsIgnoreCase(event_url)){
				url = event_url;
				driver.get(url);
			}
			WebElement element = driver.findElement(By.xpath(event.getString("target")));
			
			if(event.has("charCode")){
				char ch = (char) event.getBigInteger(("charCode")).intValue();
				char keys[] = new char[1];
				keys[0] = ch;
				element.sendKeys(new String(keys));
			} else {
				// It can emulate just left button clicks
				element.click();
			}
		}
		driver.close();
		System.out.println(events);
	}
}
