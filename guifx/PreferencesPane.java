package guifx;

import static guifx.utils.Settings.bindProperty;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import latex.DocumentParameters;
import utils.GetterSetter;

public class PreferencesPane {
	private static final int	PREFERRED_WIDTH		= 500;
	private static final int	PREFERRED_HEIGHT	= 500;

	private DocumentParameters	params;
	
	public PreferencesPane(DocumentParameters params) {
		this.params        = params;
		Stage primaryStage = new Stage(StageStyle.DECORATED);
		
		Button close = new Button("");
		close.setOnAction(ev ->	primaryStage.hide());
		bindProperty(close.textProperty(),"close");
		
		AnchorPane footer = new AnchorPane(close);
		AnchorPane.setBottomAnchor(close,10d);
		AnchorPane.setRightAnchor(close,25d);
		
		TabPane tabPane = new TabPane();
		setOptionsPane(tabPane);
		setPackagesPane(tabPane);
		setCommandsPane(tabPane);
		
		BorderPane root = new BorderPane();
		root.setBottom(footer);
		root.setCenter(tabPane);
		footer.setPadding(new Insets(5,0,0,0));
		
		Scene scene = new Scene(root,PREFERRED_WIDTH,PREFERRED_HEIGHT,Color.WHITESMOKE);
		bindProperty(primaryStage.titleProperty(),"documentOptions");
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.setOnCloseRequest(ev -> primaryStage.hide());
		primaryStage.show();
	}
	
	private void setOptionsPane(TabPane tabPane) {
		GridPane grid = new GridPane();
		grid.setPadding(new Insets(10));
		grid.setVgap(12);
		grid.setHgap(5);
		
		List<String>               options    = Arrays.asList("documentClass","alineaLength","chapterName");
		List<GetterSetter<String>> getSetters = Arrays.asList(
			new GetterSetter<>(params::getDocumentClass,params::setDocumentClass),
			new GetterSetter<>(params::getAlinea       ,params::setAlinea       ),
			new GetterSetter<>(params::getChapterName  ,params::setChapterName  ));
		
		IntStream.range(0,options.size()).forEach(i -> { 
			Label     label = new Label();
			label.setFont(LatexEditor.subtitlesFont);
			bindProperty(label.textProperty(),options.get(i));
			
			TextField field = new TextField(getSetters.get(i).get());
			field.textProperty().addListener((obs,oldValue,newValue) -> getSetters.get(i).set(newValue));
			
			grid.add(label,0,i);
			grid.add(field,1,i);
		});
		
		Tab optionsTab = new Tab();
		bindProperty(optionsTab.textProperty(),"documentSettings");
		optionsTab.setContent(grid);
		optionsTab.setClosable(false);
		tabPane.getTabs().add(optionsTab);
	}
	
	private void setPackagesPane(TabPane tabPane) {
		setGenericOptionPane(tabPane,"packages",params.getPackagesView(),params::removePackage,params::addPackage);
	}
	
	private void setCommandsPane(TabPane tabPane) {
		setGenericOptionPane(tabPane,"includes",params.getIncludesView(),params::removeInclude,params::include);
	}
	
	private <T> void setGenericOptionPane(TabPane tabPane, String tabName, 
			ObservableList<T> items, Consumer<String> removeOp, Consumer<String> addOp) {
		ListView<T> listView = new ListView<>(items);
		BorderPane  pane     = new BorderPane();
		
		// set context menu
		ContextMenu deleteMenu  = new ContextMenu();
		MenuItem delete         = new MenuItem("Supprimer");
    	deleteMenu.getItems().add(delete);
    	
    	delete.setOnAction(ev -> removeOp.accept(listView.getSelectionModel().getSelectedItem().toString()));
		
		listView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent ev) {
                if (ev.getButton().equals(MouseButton.SECONDARY))
                	deleteMenu.show(listView,ev.getScreenX() + 5, ev.getScreenY() + 5);
                else 
                	deleteMenu.hide();
            }
		});
		
		listView.setOnKeyPressed(ev -> {
			int selected = listView.getSelectionModel().getSelectedIndex();
			int insert   = selected;
			if (ev.isControlDown() && ev.isAltDown()) {
				if      (ev.getCode() == KeyCode.DOWN) insert++;
				else if (ev.getCode() == KeyCode.UP  ) insert--;
			}
			if (0 <= insert && insert <= items.size()) {
				T tmp = items.get(selected);
				items.set(selected,items.get(insert));
				items.set(insert,tmp);
				listView.getSelectionModel().select(insert);
			}
		});
		
		// set "add element" bar
		TextField field     = new TextField();
		Button    add       = new Button();
		HBox      searchBar = new HBox(5,field,add);
		searchBar.setPadding(new Insets(10,0,0,0));
		
		bindProperty(add.textProperty(),"add");
		add.setOnAction(ev -> { 
			if (!field.getText().isEmpty()) {
				addOp.accept(field.getText());
				field.setText("");
			}
		});
		field.setOnAction(add.getOnAction());
		
		// set package view
		pane.setCenter(listView);
		pane.setBottom(searchBar);
		pane.setPadding(new Insets(10,20,10,20));
		HBox.setHgrow(field,Priority.ALWAYS);
		
		Tab packageTab = new Tab();
		bindProperty(packageTab.textProperty(),tabName);
		packageTab.setContent(pane);
		packageTab.setClosable(false);
		tabPane.getTabs().add(packageTab);
	}
}