package guifx.utils;

import static javafx.application.Application.STYLESHEET_CASPIAN;
import static javafx.application.Application.STYLESHEET_MODENA;
import static javafx.application.Application.setUserAgentStylesheet;
import impl.org.controlsfx.i18n.Localization;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;

public class Settings {
	public static final Properties				properties	= new Properties();
	public static final ObservableProperties	strings		= new ObservableProperties();
	public static final Map<String,MenuItem>	preferences	= new HashMap<>();
	
	private static final String					PREF_SKIN			= "skin";
	private static final String					PREF_LANGUAGE		= "language";

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
    
    public static Menu getChooseStyleMenu(final ImageView checkedIcon) {
		Menu chooseStyle       = new Menu();		
		final MenuItem caspian = new MenuItem("Caspian");
        final MenuItem modena  = new MenuItem("Modena");
		MenuItem selectedMenu;
		switch (properties.getProperty("defaultStyle")) {
			case "CASPIAN" : selectedMenu = caspian; break;
			default        : selectedMenu = modena;
		}
		
		selectedMenu.setGraphic(checkedIcon);
		preferences.put(PREF_SKIN,selectedMenu);
		setUserAgentStylesheet(properties.getProperty("defaultStyle"));
		
		chooseStyle.textProperty().bind(strings.getObservableProperty("skin"));
		
		caspian.setOnAction((ActionEvent ev) -> {			
			setUserAgentStylesheet(STYLESHEET_CASPIAN);
			changePreference(caspian,PREF_SKIN,checkedIcon);
		});
		modena.setOnAction((ActionEvent ev) -> {
			setUserAgentStylesheet(STYLESHEET_MODENA);
			changePreference(modena,PREF_SKIN,checkedIcon);
		});       
        chooseStyle.getItems().addAll(caspian,modena);
		return chooseStyle;
	}
    
    public static Menu getChooseLanguageMenu(final ImageView checkedIcon) {
		Menu chooseLanguage    = new Menu();
		final MenuItem french  = new MenuItem();
		final MenuItem english = new MenuItem();	
        final MenuItem spanish = new MenuItem();
		MenuItem selectedMenu;
		
		chooseLanguage.textProperty().bind(strings.getObservableProperty("lang"));
		french        .textProperty().bind(strings.getObservableProperty("lang-fr"));
		english       .textProperty().bind(strings.getObservableProperty("lang-en"));
        spanish       .textProperty().bind(strings.getObservableProperty("lang-es"));
		
		switch (properties.getProperty("defaultLanguage")) {
			case "FR" : selectedMenu = french ; break;
            case "ES" : selectedMenu = spanish;	break;
			default   :	selectedMenu = english; break;
		}
		
		selectedMenu.setGraphic(checkedIcon);
		chooseLanguage.getItems().addAll(french,english,spanish);
		preferences.put(PREF_LANGUAGE,selectedMenu);
		
		french .setOnAction(languageChoiceAction(french,"FR","fr","FR",checkedIcon));   
		english.setOnAction(languageChoiceAction(english,"EN","en","UK",checkedIcon));
        spanish.setOnAction(languageChoiceAction(spanish,"ES","es","ES",checkedIcon));
		return chooseLanguage;
	}
	
    private static void changePreference(MenuItem clicked, String prefName, Node node) {
		MenuItem checked = preferences.get(prefName);
		if (checked != clicked) {
			checked.setGraphic(null);
			clicked.setGraphic(node);
			preferences.put(prefName,clicked);
		}
	}	
    
	private static EventHandler<ActionEvent> languageChoiceAction(MenuItem menu, String propertyName, String lang, String country,
			ImageView checkedIcon) {
		return (ActionEvent ev) -> {
			if (menu != preferences.get(PREF_LANGUAGE)) {
				loadLocalizedTexts(properties.getProperty(propertyName));
				Localization.setLocale(new Locale(lang,country));
			}
			changePreference(menu,PREF_LANGUAGE,checkedIcon);
		};
	}
}
