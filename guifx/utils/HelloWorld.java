//package guifx.utils;
//
//import static java.lang.Math.abs;
//import static java.lang.Math.cos;
//import static java.lang.Math.pow;
//import static java.lang.Math.sin;
//import static java.util.Arrays.asList;
//import impl.org.controlsfx.i18n.Localization;
//
//import java.awt.Point;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.HashMap;
//import java.util.Locale;
//import java.util.Map;
//import java.util.Properties;
//
//import javafx.application.Application;
//import javafx.application.Platform;
//import javafx.embed.swing.SwingFXUtils;
//import javafx.event.ActionEvent;
//import javafx.event.EventHandler;
//import javafx.scene.Node;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.control.Menu;
//import javafx.scene.control.MenuBar;
//import javafx.scene.control.MenuItem;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.image.WritableImage;
//import javafx.scene.input.KeyCharacterCombination;
//import javafx.scene.layout.BorderPane;
//import javafx.scene.paint.Color;
//import javafx.stage.FileChooser;
//import javafx.stage.Stage;
//import javafx.util.Pair;
//
//import javax.imageio.ImageIO;
//
//import org.controlsfx.dialog.Dialogs;
//
//import utils.ObservableProperties;
// 
//public class HelloWorld extends Application {
//	private static final Properties				properties			= new Properties();
//	public static final ObservableProperties	strings				= new ObservableProperties();
//	
//	private static final String					PREF_SKIN			= "skin";
//	private static final String					PREF_LANGUAGE		= "language";
//	
//	public static final double					PREFERRED_WIDTH		= 1100;
//	public static final double					PREFERRED_HEIGHT	= 600;
//
//	private final Map<String, MenuItem>			preferences			= new HashMap<>();
//	private File								currentFile;
//	
//	private BorderPane							root				= new BorderPane();
//	private WritableImage						wim;
//    
//    @Override
//    public void start(Stage primaryStage) {
//		// load preferences and program constants
//		loadProperties();	
//		
//		// load localised texts and descriptions
//		loadLocalizedTexts(properties.getProperty(properties.getProperty("defaultLanguage")));			
//		
//        MenuBar    menuBar = setMenuBar();        
//        root.setTop(menuBar);
//        
//        Scene scene = new Scene(root,PREFERRED_WIDTH,PREFERRED_HEIGHT,Color.WHITESMOKE);
//		primaryStage.setOnCloseRequest(ev -> Platform.exit());
//        primaryStage.titleProperty().bind(strings.getObservableProperty("frameTitle"));
//        primaryStage.setScene(scene);
//        primaryStage.show(); 
//    }
//    
//    public BufferedImage rotateImage(BufferedImage im, double alpha) {
//		// conversion to radians
//		alpha      = (alpha % 361)*Math.PI/180;
//		
//		// compute the dimension of the new image
//		double cos = cos(alpha)                     , sin = sin(alpha);
//		int    w   = im.getWidth()                  , h   = im.getHeight();
//		int    W   = (int) (abs(w*cos) + abs(h*sin)), H   = (int) (abs(h*cos) + abs(w*sin));
//		
//		System.out.println(String.format("(W,H) = (%d,%d)",W,H));
//		System.out.println(String.format("(w,h) = (%d,%d)\n",w,h));
//		
//		BufferedImage res = new BufferedImage(W,H,BufferedImage.TYPE_INT_ARGB);
//		// iterate over the pixels of the new image, which coordinates are taken relatively to 
//		// the center of the new image
//		for (int i=-W/2 ; i<W/2 ; i++)
//			for (int j=-H/2 ; j<H/2 ; j++) {
//				// apply the inverse rotation to find the point of the original image that falls
//				// on (i,j) after the rotation, and translate the resulting point to get its
//				// coordinates in the original image
//				double dx =  i*cos + j*sin + w/2;
//				double dy = -i*sin + j*cos + h/2;
//				
//				// find the discrete point that corresponds (dx,dy) the best in the least square sense
//				int cx = (int) Math.ceil (dx), cy = (int) Math.ceil (dy);
//				int fx = (int) Math.floor(dx), fy = (int) Math.floor(dy);
//				
//				final int ii = i + W/2, jj = j + H/2;
//				asList(new Point(cx,cy),new Point(cx,fy),new Point(fx,cy),new Point(fx,fy)).stream()
//					.filter(p -> 0 <= p.x && p.x < w && 0 <= p.y && p.y < h)
//					.map(p -> new Pair<>(p,pow(p.x - dx,2) + pow(p.y - dy,2)))
//					.min((x,y) -> Double.compare(x.getValue(),y.getValue()))
//					.ifPresent(kv -> {
//						Point bestMatch = kv.getKey();
//						res.setRGB(ii,jj,im.getRGB(bestMatch.x,bestMatch.y));
//					});
//			}
//		return res;
//	}
//    
//    private MenuBar setMenuBar() {
//    	MenuBar menuBar = new MenuBar();
//        Menu menuFile   = new Menu();
//        Menu menuEdit   = new Menu();
//        Menu menuPref   = new Menu();
//		
//		menuFile.textProperty().bind(strings.getObservableProperty("file"));
//		menuEdit.textProperty().bind(strings.getObservableProperty("edit"));
//		menuPref.textProperty().bind(strings.getObservableProperty("preferences"));
//        
//        MenuItem save     = new MenuItem();
//        MenuItem saveAs   = new MenuItem();
//        MenuItem load     = new MenuItem();
//        MenuItem quit     = new MenuItem();
//        menuFile.getItems().addAll(load,save,saveAs,quit);
//        
//        load  .textProperty().bind(strings.getObservableProperty("load"));
//		save  .textProperty().bind(strings.getObservableProperty("save"));
//		saveAs.textProperty().bind(strings.getObservableProperty("saveAs"));
//		quit  .textProperty().bind(strings.getObservableProperty("quit"));
//        
//		save.setAccelerator(new KeyCharacterCombination("S",KeyCharacterCombination.CONTROL_DOWN));
//		saveAs.setAccelerator(new KeyCharacterCombination("S",KeyCharacterCombination.CONTROL_DOWN,
//				KeyCharacterCombination.ALT_DOWN));
//		load.setAccelerator(new KeyCharacterCombination("L",KeyCharacterCombination.CONTROL_DOWN));
//		quit.setAccelerator(new KeyCharacterCombination("Q",KeyCharacterCombination.CONTROL_DOWN));
//
//		quit  .setOnAction(ev -> System.exit(0));
//		save  .setOnAction(ev -> save());
//		saveAs.setOnAction(ev -> saveAs());
//		load  .setOnAction(ev -> load());
//		menuBar.getMenus().addAll(menuFile,menuEdit,menuPref);
//        
//		final ImageView checkedIcon = new ImageView(
//                new Image(getClass().getResourceAsStream(properties.getProperty("checkedIcon"))));
//		
//        Menu chooseStyle    = setChooseStyle(checkedIcon);	
//		Menu chooseLanguage = setChooseLanguage(checkedIcon);	   
//		
//        menuPref.getItems().addAll(chooseStyle,chooseLanguage);
//    	return menuBar;
//    }
//    
//    private Menu setChooseLanguage(final ImageView checkedIcon) {
//		Menu chooseLanguage    = new Menu();
//		final MenuItem french  = new MenuItem();
//		final MenuItem english = new MenuItem();	
//        final MenuItem spanish = new MenuItem();
//		MenuItem selectedMenu;
//		
//		chooseLanguage.textProperty().bind(strings.getObservableProperty("lang"));
//		french        .textProperty().bind(strings.getObservableProperty("lang-fr"));
//		english       .textProperty().bind(strings.getObservableProperty("lang-en"));
//        spanish       .textProperty().bind(strings.getObservableProperty("lang-es"));
//		
//		switch (properties.getProperty("defaultLanguage")) {
//			case "FR" : selectedMenu = french;  break;
//            case "ES" : selectedMenu = spanish;	break;
//			default   :	selectedMenu = english;
//		}
//		
//		selectedMenu.setGraphic(checkedIcon);
//		chooseLanguage.getItems().addAll(french,english,spanish);
//		preferences.put(PREF_LANGUAGE,selectedMenu);
//		
//		french .setOnAction(languageChoiceAction(french,"FR","fr","FR",checkedIcon));   
//		english.setOnAction(languageChoiceAction(english,"EN","en","UK",checkedIcon));
//        spanish.setOnAction(languageChoiceAction(spanish,"ES","es","ES",checkedIcon));
//		return chooseLanguage;
//	}
//	
//    private EventHandler<ActionEvent> languageChoiceAction(MenuItem menu, String propertyName, 
//    	String lang, String country, ImageView checkedIcon) {
//      return (ActionEvent ev) -> {
//    	  if (menu != preferences.get(PREF_LANGUAGE)) {
//    		  loadLocalizedTexts(properties.getProperty(propertyName));
//    		  Localization.setLocale(new Locale(lang,country));				
//    	  }
//    	  changePreference(menu,PREF_LANGUAGE,checkedIcon);
//      };
//    }
//    
//	private Menu setChooseStyle(final ImageView checkedIcon) {
//		Menu chooseStyle       = new Menu();		
//		final MenuItem caspian = new MenuItem("Caspian");
//        final MenuItem modena  = new MenuItem("Modena");
//		MenuItem selectedMenu;
//		switch (properties.getProperty("defaultStyle")) {
//			case "CASPIAN" : selectedMenu = caspian; break;
//			default        : selectedMenu = modena;
//		}
//		
//		selectedMenu.setGraphic(checkedIcon);
//		preferences.put(PREF_SKIN,selectedMenu);
//		setUserAgentStylesheet(properties.getProperty("defaultStyle"));
//		
//		chooseStyle.textProperty().bind(strings.getObservableProperty("skin"));
//		
//		caspian.setOnAction((ActionEvent ev) -> {			
//			setUserAgentStylesheet(STYLESHEET_CASPIAN);
//			changePreference(caspian,PREF_SKIN,checkedIcon);
//		});
//		modena.setOnAction((ActionEvent ev) -> {
//			setUserAgentStylesheet(STYLESHEET_MODENA);
//			changePreference(modena,PREF_SKIN,checkedIcon);
//		});       
//        chooseStyle.getItems().addAll(caspian,modena);
//		return chooseStyle;
//	}
//	
//	private void changePreference(MenuItem clicked, String prefName, Node node) {
//		MenuItem checked = preferences.get(prefName);
//		if (checked != clicked) {
//			checked.setGraphic(null);
//			clicked.setGraphic(node);
//			preferences.put(prefName,clicked);
//		}
//	}
//    
//    private void save() {
//    	if (wim != null)
//    		try {
//    			ImageIO.write(SwingFXUtils.fromFXImage(wim,null),"png",currentFile);
//    		} catch (IOException io) {
//    			Dialogs.create().owner(this)
//					.title(strings.getProperty("error"))
//					.masthead(strings.getProperty("anErrorOccurredMessage"))
//					.message(strings.getProperty("ioSaveError"))
//					.showError();
//    		}	
//    }
//    
//    private void saveAs() {
//    	if (selectFile(strings.getProperty("imageFiles"),true,"*.png") != null) 
//    		save();
//    }
//    
//    private boolean dragging = false;
//    private double posX, posY;
//    
//    private void load() {
//    	try {
//    		File f = selectFile(strings.getProperty("imageFiles"),false,"*.png");
//    		if (f != null) {
//    			BufferedImage im = ImageIO.read(f);
//    			SwingFXUtils.toFXImage(im,wim = new WritableImage(im.getWidth(),im.getHeight()));
//    			
//    			ImageView view = new ImageView(wim);
//    			
//    			view.setOnMousePressed(ev -> { 
//    				System.out.println("Detected");
//    				posX = ev.getX();
//    				posY = ev.getY();
//    				ev.consume();
//    			});
//    			view.setOnMouseMoved(ev -> { 
//    				System.out.println("Moved");
//    				double x = ev.getX();
//    				double y = ev.getY();
//    				
//    				posX = x;
//    				posY = y;
//    				ev.consume();
//    			});
//    			view.setOnMouseReleased(ev -> { 
//    				System.out.println("Dropped");
//    				ev.consume();
//    			});
//    			root.setCenter(view);
//    		}
//    	} catch (IOException io) {
//    		Dialogs.create().owner(this)
//				.title(strings.getProperty("error"))
//				.masthead(strings.getProperty("anErrorOccurredMessage"))
//				.message(strings.getProperty("ioLoadError"))
//				.showError();
//    	}
//    }
//    
//    private File selectFile(String filterName, boolean save, String... extensions) {
//        FileChooser chooser = new FileChooser();
//		if (currentFile != null)
//			chooser.setInitialDirectory(currentFile.getParentFile());
//			
//		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(filterName,extensions));
//		File selectedFile = save ? chooser.showSaveDialog(null) : chooser.showOpenDialog(null);
//        currentFile       = selectedFile == null ? currentFile : selectedFile;
//        return selectedFile;
//    }
//    
//    private void loadProperties() {
//		try (InputStreamReader isr = new InputStreamReader(
//				getClass().getResourceAsStream("/properties/config.properties"))) {
//			properties.load(isr);
//			isr.close();
//		} catch (IOException e) {
//			System.out.println("Error while retrieving the application properties");
//			e.printStackTrace();
//		}
//	}
//    
//    private void loadLocalizedTexts(String lang) {
//        try (InputStreamReader isr = new InputStreamReader(getClass().getResourceAsStream(lang))) {
//			strings.load(isr);
//			isr.close();
//		} catch (IOException e) {
//			System.out.println("Error while retrieving the application texts and descriptions");
//			e.printStackTrace();
//		}
//	}
//    
//    public static void main(String[] args) {
//    	launch(args);
//    }
//}