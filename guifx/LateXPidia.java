package guifx;

import static guifx.utils.Settings.properties;
import static guifx.utils.Settings.strings;
import guifx.components.ContextToolBar;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.function.Function;

import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import utils.Encyclopedia;

public class LateXPidia extends Stage {
	private Encyclopedia<String> encyclopedia;
	
	public LateXPidia() {
		try {
			this.encyclopedia  = new Encyclopedia<>(Paths.get(LateXPidia.class.getResource("/data/documentation/encyclopedia.txt").toURI()),Function.identity());
			BorderPane root    = new BorderPane();
			
			ImageView  icon    = new ImageView(new Image(LateXPidia.class.getResourceAsStream(properties.getProperty("searchIcon"))));
			TextField  field   = new TextField();
			HBox       box     = new HBox(7,icon,field);
			
			ContextToolBar toolbar = new ContextToolBar(strings.getObservableProperty("help"),200,box);
			toolbar.setOrientation(Orientation.VERTICAL);
			root.setLeft(toolbar);
//		root.setCenter(new WebView());
			root.setCenter(new TextArea());
			Scene scene = new Scene(root,0,0);
			setScene(scene);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}