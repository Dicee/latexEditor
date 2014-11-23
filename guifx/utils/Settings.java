package guifx.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class Settings {
	private static final Properties				properties	= new Properties();
	public static final ObservableProperties	strings		= new ObservableProperties();
	
	public static void init() {
		// load preferences and program constants
		loadProperties();	
		// load localised texts and descriptions
		loadLocalizedTexts(properties.getProperty(properties.getProperty("defaultLanguage")));	
	}

	private static void loadProperties() {
		try (InputStreamReader isr = new InputStreamReader(
				Settings.class.getResourceAsStream("/properties/config.properties"))) {
			properties.load(isr);
			isr.close();
		} catch (IOException e) {
			System.out.println("Error while retrieving the application properties");
			e.printStackTrace();
		}
	}
    
    private static void loadLocalizedTexts(String lang) {
        try (InputStreamReader isr = new InputStreamReader(Settings.class.getResourceAsStream(lang))) {
			strings.load(isr);
			isr.close();
		} catch (IOException e) {
			System.out.println("Error while retrieving the application texts and descriptions");
			e.printStackTrace();
		}
	}
}
