package guifx;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import latex.DocumentParameters;

public class PreferencesPane {
	private static final int	PREFERRED_WIDTH		= 500;
	private static final int	PREFERRED_HEIGHT	= 500;
	private DocumentParameters	params;
	
	public PreferencesPane(DocumentParameters params) {
		this.params        = params;
		Stage primaryStage = new Stage(StageStyle.DECORATED);
		
		Button close = new Button("Fermer");
		close.setOnAction(ev ->	primaryStage.hide());
		
		AnchorPane footer = new AnchorPane(close);
		AnchorPane.setBottomAnchor(close,10d);
		AnchorPane.setRightAnchor(close,25d);
		
		TabPane tabPane = new TabPane();
		setPackagesPane(tabPane);
		setCommandsPane(tabPane);
		
		BorderPane root = new BorderPane();
		root.setBottom(footer);
		root.setCenter(tabPane);
		footer.setPadding(new Insets(5,0,0,0));
		
		Scene scene = new Scene(root,PREFERRED_WIDTH,PREFERRED_HEIGHT,Color.WHITESMOKE);
		primaryStage.setTitle("Options du document");
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.setOnCloseRequest(ev -> primaryStage.hide());
		primaryStage.show();
	}
	
	private void setPackagesPane(TabPane tabPane) {
		ListView<String> packages = new ListView<>(FXCollections.observableArrayList(params.getPackages()));
		BorderPane       pane     = new BorderPane();
		
		TextField field     = new TextField();
		Button    add       = new Button("Ajouter");
		HBox      searchBar = new HBox(5,field,add);
		searchBar.setPadding(new Insets(10,0,0,0));
		
		pane.setCenter(packages);
		pane.setBottom(searchBar);
		pane.setPadding(new Insets(10,20,10,20));
		HBox.setHgrow(field,Priority.ALWAYS);
		
		Tab packageTab = new Tab("Packages");
		packageTab.setContent(pane);
		packageTab.setClosable(false);
		tabPane.getTabs().add(packageTab);
	}
	
	private void setCommandsPane(TabPane tabPane) {
		
	}
}
