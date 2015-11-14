package com.focusit.jsflight.player;

import java.awt.EventQueue;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.UIManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;
import com.focusit.jsflight.player.ui.MainFrame;

public class Player {
	private static final Logger log = LoggerFactory.getLogger(Player.class);

	public static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	public static void main(String[] args) throws IOException {
//		System.out.println("opening " + args[0]);
//		String scenario = readFile(args[0], Charset.forName("UTF-8"));
//		JSONArray rawevents = new JSONArray(scenario);
//		List<JSONObject> events = new ArrayList<>();
//		for(int i=0;i<rawevents.length();i++){
//			String event = rawevents.getString(i);
//			if(!event.contains("flight-cp")){
//				events.add(new JSONObject(event));
//			}
//		}
//
//		WebDriver driver = new FirefoxDriver();
//
//		String url = "";
//		for(JSONObject event:events){
//			String event_url = event.getString("url");
//			if(!url.equalsIgnoreCase(event_url)){
//				url = event_url;
//				driver.get(url);
//			}
//			WebElement element = driver.findElement(By.xpath(event.getString("target")));
//			
//			if(event.has("charCode")){
//				char ch = (char) event.getBigInteger(("charCode")).intValue();
//				char keys[] = new char[1];
//				keys[0] = ch;
//				element.sendKeys(new String(keys));
//			} else {
//				// It can emulate just left button clicks
//				element.click();
//			}
//		}
//		driver.close();
//		System.out.println(events);
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				log.error(e.toString(), e);
				TaskDialogs.showException(e);
			}
		});
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
//					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					MainFrame window = new MainFrame();
					window.getFrame().setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}
}
