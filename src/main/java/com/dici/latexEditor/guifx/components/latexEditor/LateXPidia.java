package com.dici.latexEditor.guifx.components.latexEditor;

import static com.dici.latexEditor.guifx.utils.Settings.strings;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.function.Function;

import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import com.dici.javafx.components.ContextToolBar;
import com.dici.latexEditor.guifx.LateXEditor;
import com.dici.latexEditor.properties.ConfigProperties;
import com.dici.latexEditor.utils.Encyclopedia;

public class LateXPidia extends Stage {
	@SuppressWarnings("unused")
	private Encyclopedia<String> encyclopedia;
	
	public LateXPidia() {
		try {
			this.encyclopedia  = new Encyclopedia<>(Paths.get(LateXPidia.class.getResource("documentation/encyclopedia.txt").toURI()),Function.identity());
			BorderPane root    = new BorderPane();
			
			ImageView  icon    = new ImageView(LateXEditor.getResourceImage(ConfigProperties.SEARCH_ICON));
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