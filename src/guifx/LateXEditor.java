package guifx;

import static com.dici.javafx.actions.NonCancelableAction.nonCancelableAction;
import static com.dici.javafx.actions.SaveAction.saveAction;
import static guifx.utils.DialogsFactory.showError;
import static guifx.utils.DialogsFactory.showPreFormattedError;
import static guifx.utils.LateXEditorTreeUtils.namedLateXElements;
import static guifx.utils.LateXEditorTreeUtils.newTreeItem;
import static guifx.utils.Settings.bindProperty;
import static guifx.utils.Settings.getChooseLanguageMenu;
import static guifx.utils.Settings.getChooseStyleMenu;
import static guifx.utils.Settings.getChooseThemeMenu;
import static guifx.utils.Settings.properties;
import static guifx.utils.Settings.strings;
import static java.util.Arrays.asList;
import static javafx.scene.input.KeyCode.B;
import static javafx.scene.input.KeyCode.I;
import static javafx.scene.input.KeyCombination.ALT_DOWN;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;
import static javafx.scene.text.Font.font;
import static javafx.scene.text.FontPosture.ITALIC;
import static javafx.scene.text.FontWeight.BOLD;
import static properties.ConfigProperties.CHECKED_ICON;
import static properties.LanguageProperties.AN_ERROR_OCCURRED_MESSAGE;
import static properties.LanguageProperties.APP_NAME;
import static properties.LanguageProperties.CLEAR;
import static properties.LanguageProperties.DOCUMENTATION;
import static properties.LanguageProperties.EDIT;
import static properties.LanguageProperties.EDIT_ZONE_TIP;
import static properties.LanguageProperties.ERROR;
import static properties.LanguageProperties.FILE;
import static properties.LanguageProperties.FRAME_TITLE;
import static properties.LanguageProperties.GENERATE;
import static properties.LanguageProperties.GENERATE_LATEX;
import static properties.LanguageProperties.GENERATE_PDF;
import static properties.LanguageProperties.HELP;
import static properties.LanguageProperties.IO_LOAD_ERROR;
import static properties.LanguageProperties.IO_SAVE_ERROR;
import static properties.LanguageProperties.JAVATEX_FILES;
import static properties.LanguageProperties.LATEX_VIEW;
import static properties.LanguageProperties.LOAD;
import static properties.LanguageProperties.MALFORMED_JAVATEX_ERROR;
import static properties.LanguageProperties.NEW_DOCUMENT;
import static properties.LanguageProperties.OPTIONS;
import static properties.LanguageProperties.PASTE_TO_EDITOR;
import static properties.LanguageProperties.PREVIEW;
import static properties.LanguageProperties.QUIT;
import static properties.LanguageProperties.READ_THIS_MESSAGE;
import static properties.LanguageProperties.REDO;
import static properties.LanguageProperties.REFRESH;
import static properties.LanguageProperties.SAVE;
import static properties.LanguageProperties.SAVE_AS;
import static properties.LanguageProperties.SELECT_LANGUAGE;
import static properties.LanguageProperties.SETTINGS;
import static properties.LanguageProperties.TEXT_EDITOR;
import static properties.LanguageProperties.TREE_TITLE;
import static properties.LanguageProperties.UNDEFINED_HOME;
import static properties.LanguageProperties.UNDO;
import static properties.LanguageProperties.UNFOUND_FILE_ERROR;
import static properties.LanguageProperties.WARNING;
import guifx.components.generics.CodeEditor;
import guifx.components.latexEditor.LateXEditorShortcutsPane;
import guifx.components.latexEditor.LateXEditorTemplateChooser;
import guifx.components.latexEditor.LateXEditorTreeView;
import guifx.components.latexEditor.LateXPidia;
import guifx.components.latexEditor.PreferencesPane;
import guifx.utils.DialogsFactory;
import guifx.utils.JavatexIO;
import guifx.utils.Settings;
import guifx.utils.WrongFormatException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import properties.LanguageProperties;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;
import latex.LateXMaker;
import latex.elements.LateXElement;
import latex.elements.PreprocessorCommand;
import latex.elements.Template;
import latex.elements.Templates;
import latex.elements.Title;
import scala.io.Codec;
import scala.io.Source;
import utils.StreamPrinter;

import com.dici.collection.richIterator.RichIterators;
import com.dici.files.FileUtils;
import com.dici.javafx.NamedObject;
import com.dici.javafx.actions.ActionManager;
import com.dici.javafx.actions.ActionManagerImpl;
import com.dici.javafx.actions.NonCancelableAction;
import com.dici.javafx.actions.SaveAction;

public class LateXEditor extends Application {
    public static final Map<String, String> LANGUAGES;
    public static final String              LATEX_HOME        = FileUtils.toCanonicalPath(System.getenv("LATEX_HOME"));

    public static final int                 DEFAULT_FONT_SIZE = 13;
    public static final Font                subtitlesFont     = Font.font(null,BOLD,DEFAULT_FONT_SIZE);

    private File                            currentDir        = new File(LATEX_HOME);
    private File                            currentFile       = null;

    private Stage                           primaryStage;
    private Rectangle2D                     screenBounds;

    private final LateXMaker                lm                = new LateXMaker();
    private LateXEditorTreeView             treeView;

    private MenuBar                         menuBar;

    private TextArea                        userTextArea;
    private CodeEditor                      outputCode;
    private Label                           info;
    private Node                            textMode;

    private TextArea                        outputTextArea;

    private Consumer<Node>                  setEditorZone;
    private SplitPane                       splitPane;

    private final LateXPidia                encyclopedia      = new LateXPidia();
    private final ActionManager             actionManager     = new ActionManagerImpl();

	public static final Image getResourceImage(String path) { return new Image(LateXEditor.class.getResourceAsStream(path)); }
	
	@Override
	public void start(Stage primaryStage) {
		setTree();

		Screen screen     = Screen.getPrimary();
		this.screenBounds = screen.getVisualBounds();
		this.primaryStage = primaryStage;
		VBox root         = new VBox(10);

		Scene scene = new Scene(root,0,0);
		primaryStage.setScene(scene);

		Node editZone = setEditZone();
		setMenuBar();
		
		VBox header = setHeader();
		root.getChildren().addAll(header,editZone);
		setGlobalEventHandler(root);

		primaryStage.setTitle(strings.getProperty(FRAME_TITLE)); 
		actionManager.isSavedProperty().addListener((ov, oldValue, newValue) -> primaryStage.setTitle((currentFile == null ? "" : newValue ? "" : "*" + currentFile.getAbsolutePath() + " ") + APP_NAME));
		primaryStage.setX     (screenBounds.getMinX  ());
		primaryStage.setY     (screenBounds.getMinY  ());
		primaryStage.setWidth (screenBounds.getWidth ());
		primaryStage.setHeight(screenBounds.getHeight());
		primaryStage.show();
	}
	
	private void setTree() {
		treeView = new LateXEditorTreeView(new PreprocessorCommand(""), actionManager);
		treeView.setMinWidth(300);
		treeView.getRoot().setExpanded(true);
		treeView.getSelectionModel().selectedItemProperty().addListener(updateTreeOnChange());
		treeView.getRoot().getChildren().add(newTreeItem(new Title()));
	}
	
	private ChangeListener<TreeItem<NamedObject<LateXElement>>> updateTreeOnChange() {
		return (ObservableValue<? extends TreeItem<NamedObject<LateXElement>>> ov, TreeItem<NamedObject<LateXElement>> formerItem,
			TreeItem<NamedObject<LateXElement>> newItem) -> {
			TreeItem<NamedObject<LateXElement>> currentNode = treeView.getCurrentNode();
			if (formerItem != null &&  currentNode != null) 
				if (!(formerItem.getValue().bean instanceof Template))
					formerItem.getValue().bean.setText(userTextArea.getText());
			
			if (newItem != null && newItem.getValue() != null) {
				if (newItem.getValue().bean instanceof Template)
					setEditorZone.accept(new LateXEditorTemplateChooser((Template) newItem.getValue().bean));
				else {
					userTextArea.setText(newItem.getValue().bean.getText());
					setEditorZone.accept(textMode);
				}
				splitPane.setDividerPositions(0.40);
				splitPane.autosize();
				bindProperty(info.textProperty(),newItem.getValue().bean.getType() + "Tip");
				currentNode = newItem;
			}
		};
	}
	
	private Node setEditZone() {
		VBox      textEditor  = setTextEditor();
		Accordion leftToolbar = setLeftToolbar();
		SplitPane splitPane   = setSplitPane(textEditor);

		BorderPane borderPane = new BorderPane();
		borderPane.setLeft(leftToolbar);
		borderPane.setCenter(splitPane);
		borderPane.setPadding(new Insets(15));

		HBox.setHgrow(textEditor,Priority.ALWAYS);
		HBox.setHgrow(treeView  ,Priority.NEVER);
		treeView.setMinWidth (210);

		return borderPane;
	}

	private SplitPane setSplitPane(VBox textEditor) {
		outputTextArea = new TextArea();
		outputTextArea.setMinHeight(50);
		outputTextArea.setEditable(false);
		outputTextArea.setPrefHeight(screenBounds.getHeight()/2);

		TitledPane textEditorPane = new TitledPane("",textEditor);
		TitledPane outputCodePane = new TitledPane("",setOutputCode());
		bindProperty(textEditorPane.textProperty(),TEXT_EDITOR);
		bindProperty(outputCodePane.textProperty(),LATEX_VIEW);
		
		Accordion accordion = new Accordion(textEditorPane,outputCodePane);
		accordion.setExpandedPane(textEditorPane);

		splitPane = new SplitPane();
		splitPane.setOrientation(Orientation.VERTICAL);
		splitPane.getItems().addAll(textMode = accordion,outputTextArea);
		splitPane.setDividerPositions(0.40);
		splitPane.autosize();

		this.setEditorZone = nodes -> splitPane.getItems().set(0,nodes);
		return splitPane;
	}

	private CodeEditor setOutputCode() {
		outputCode = new CodeEditor("");
		outputCode.setMinHeight(20);

		Label  label = new Label();
		Button clear = new Button();
		Button paste = new Button();
		ComboBox<String> languages = new ComboBox<>();
		languages.getItems().addAll(LANGUAGES.keySet());
		languages.setOnAction(ev -> outputCode.setLanguage(LANGUAGES.get(languages.getSelectionModel().getSelectedItem())));

		bindProperty(label.textProperty(),SELECT_LANGUAGE);
		bindProperty(clear.textProperty(),CLEAR);
		bindProperty(paste.textProperty(),PASTE_TO_EDITOR);

		clear.setOnAction(ev -> outputCode.setCode(""));
		paste.setOnAction(ev -> {
			userTextArea.cut();
			userTextArea.insertText(userTextArea.getCaretPosition(),outputCode.getCodeAndSnapshot());
		});

		HBox buttons = new HBox(10,label,languages,clear,paste);
		buttons.setPadding(new Insets(10));
		outputCode.setBottom(buttons);
		
		return outputCode;
	}

	private VBox setTextEditor() {
		info = new Label();
		bindProperty(info.textProperty(), EDIT_ZONE_TIP);
		info.setFont(subtitlesFont);
		info.setPadding(new Insets(4, 0, 0, 0));
		
		userTextArea = new TextArea();
		userTextArea.setPrefSize(screenBounds.getWidth()/6, screenBounds.getHeight()/2);
		userTextArea.textProperty().addListener((ov, oldValue, newValue) -> { 
			if (newValue != null) 
				treeView.getCurrentNode().getValue().bean.setText(newValue);
		});
		
        Button bold       = setLateXCommandWrapperButton("B", "textbf", font(null, BOLD  , DEFAULT_FONT_SIZE), B);
        Button italic     = setLateXCommandWrapperButton("I", "textit", font(null, ITALIC, DEFAULT_FONT_SIZE), I);
        VBox   textEditor = new VBox(new HBox(5, new HBox(bold, italic), info), userTextArea);
		textEditor.setPadding(new Insets(5));
		textEditor.setSpacing(5);
		textEditor.setMinWidth(420);
		return textEditor;
	}
	
	private Button setLateXCommandWrapperButton(String text, String command, Font font, KeyCode shortcutCode) {
	    Button button = new Button(text);
	    button.setFont(font);
	    button.setOnAction(ev -> userTextArea.replaceSelection(String.format("\\%s{%s}", command, userTextArea.getSelectedText())));
	    primaryStage.getScene().getAccelerators().put(new KeyCodeCombination(shortcutCode, CONTROL_DOWN), button::fire);
	    return button;
	}
	
	private Accordion setLeftToolbar() {
		TitledPane shortcutsPane = setSpecialCharsShortcut();
		TitledPane treePane      = setTreePane();

		Accordion accordion = new Accordion();
		accordion.getPanes().addAll(treePane,shortcutsPane);
		accordion.setExpandedPane(treePane);
		accordion.setPadding(new Insets(10));
		accordion.setPrefHeight(screenBounds.getHeight() * 0.90);
		accordion.setMinWidth(250);
		return accordion;
	}
	
	private TitledPane setTreePane() {
		TitledPane treePane = new TitledPane("", treeView);
		bindProperty(treePane.textProperty(),TREE_TITLE);
		treeView.setPadding(new Insets(50, 5, 5, 5));
		return treePane;
	}

	private TitledPane setSpecialCharsShortcut() {
		LateXEditorShortcutsPane res = new LateXEditorShortcutsPane();
		res.setOnClick(e -> {
			userTextArea.cut();
			userTextArea.insertText(userTextArea.getCaretPosition(), e.getActionCommand());
		});
		return res;
	}
	
	private void setMenuBar() {
		// set main menu bar
		menuBar           = new MenuBar();
		Menu menuFile     = new Menu();
		Menu menuEdit     = new Menu();
		Menu menuOptions  = new Menu();
		Menu menuHelp     = new Menu();

		// set submenu File
		MenuItem newDoc   = new MenuItem();
		MenuItem load     = new MenuItem();
		MenuItem refresh  = new MenuItem();
		MenuItem save     = new MenuItem();
		MenuItem saveAs   = new MenuItem();
		MenuItem generate = new MenuItem();
		MenuItem quit     = new MenuItem();
		menuFile.getItems().addAll(newDoc, load, refresh, save, saveAs, generate, quit);
		
		// set submenu Edit
		MenuItem undo = new MenuItem();
		MenuItem redo = new MenuItem();
		undo.disableProperty().bind(actionManager.hasPreviousProperty().not());
		redo.disableProperty().bind(actionManager.hasNextProperty    ().not());
		menuEdit.getItems().addAll(undo, redo);

		// set submenu Options
		MenuItem  settings    = new MenuItem();
		ImageView checkedIcon = new ImageView(getResourceImage(properties.getProperty(CHECKED_ICON)));
		menuOptions.getItems().addAll(settings, getChooseLanguageMenu(checkedIcon), getChooseStyleMenu(checkedIcon), getChooseThemeMenu(checkedIcon,s -> outputCode.refresh()));

		// set submenu Help
		MenuItem doc = new MenuItem();
		menuHelp.getItems().add(doc);

        setMenusAccelerator(newDoc, load, refresh, save, saveAs, generate, quit, undo, redo, settings, doc);
        setMenusAction     (newDoc, load, refresh, save, saveAs, generate, quit, undo, redo, settings, doc);
        setMenusText       (menuFile, menuEdit, menuOptions, menuHelp, newDoc, load, refresh, save, saveAs, generate, quit, undo, redo, settings, doc);
        
        menuBar.getMenus().addAll(menuFile, menuEdit, menuOptions, menuHelp);
	}

	private void setMenusAccelerator(MenuItem newDoc, MenuItem load, MenuItem refresh, MenuItem save, MenuItem saveAs, MenuItem generate, MenuItem quit,
			MenuItem undo, MenuItem redo, MenuItem settings, MenuItem doc) {
        newDoc  .setAccelerator(new KeyCharacterCombination("N", CONTROL_DOWN          ));
        save    .setAccelerator(new KeyCharacterCombination("S", CONTROL_DOWN          ));
        saveAs  .setAccelerator(new KeyCharacterCombination("S", CONTROL_DOWN, ALT_DOWN));
        load    .setAccelerator(new KeyCharacterCombination("L", CONTROL_DOWN          ));
        refresh .setAccelerator(new KeyCharacterCombination("R", CONTROL_DOWN          ));
        generate.setAccelerator(new KeyCharacterCombination("G", CONTROL_DOWN          ));
        quit    .setAccelerator(new KeyCharacterCombination("Q", CONTROL_DOWN          ));
        undo    .setAccelerator(new KeyCharacterCombination("Z", CONTROL_DOWN          ));
        redo    .setAccelerator(new KeyCharacterCombination("Y", CONTROL_DOWN          ));
        settings.setAccelerator(new KeyCharacterCombination("O", CONTROL_DOWN          ));
        doc     .setAccelerator(new KeyCharacterCombination("H", CONTROL_DOWN          ));
	}

	private void setMenusAction(MenuItem newDoc, MenuItem load, MenuItem refresh, MenuItem save, MenuItem saveAs, MenuItem generate, MenuItem quit,
			MenuItem undo, MenuItem redo, MenuItem settings, MenuItem doc) {
		doc     .setOnAction(ev -> { if (!encyclopedia.isShowing()) encyclopedia.show(); });
		newDoc  .setOnAction(ev -> newDocument()); 
		save    .setOnAction(ev -> save());
		saveAs  .setOnAction(ev -> { createDocument(); save(); });
		load    .setOnAction(ev -> load());
		refresh .setOnAction(ev -> loadFile(currentFile));
		generate.setOnAction(ev -> generate());
		quit    .setOnAction(ev -> System.exit(0));
		undo    .setOnAction(ev -> actionManager.undo());
		redo    .setOnAction(ev -> actionManager.redo());
		settings.setOnAction(ev -> new PreferencesPane(lm.getParameters()));
	}

	private void setMenusText(Menu menuFile, Menu menuEdit, Menu menuOptions, Menu menuHelp, MenuItem newDoc, MenuItem load, MenuItem refresh, MenuItem save,
			MenuItem saveAs, MenuItem generate, MenuItem quit, MenuItem undo, MenuItem redo, MenuItem settings, MenuItem doc) {
        List<String>   properties = asList(FILE, EDIT, OPTIONS, HELP, NEW_DOCUMENT, LOAD, REFRESH, SAVE, SAVE_AS, GENERATE, QUIT, UNDO, REDO, SETTINGS, DOCUMENTATION);
        List<MenuItem> menus      = asList(menuFile, menuEdit, menuOptions, menuHelp, newDoc, load, refresh, save, saveAs, generate, quit, undo, redo, settings, doc);
		IntStream.range(0,menus.size()).forEach(i -> bindProperty(menus.get(i).textProperty(),properties.get(i)));
	}

	private VBox setHeader() {
		VBox header           = new VBox();
		ImageView pdfIcon     = new ImageView(getResourceImage(properties.getProperty("pdfIcon"    )));
		ImageView texIcon     = new ImageView(getResourceImage(properties.getProperty("texIcon"    )));
		ImageView previewIcon = new ImageView(getResourceImage(properties.getProperty("previewIcon")));
		
		Button tex     = new Button("", texIcon);
		Button preview = new Button("", previewIcon);
		Button pdf     = new Button("", pdfIcon);
		bindProperty(tex    .textProperty(), GENERATE_LATEX);
		bindProperty(pdf    .textProperty(), GENERATE_PDF);
		bindProperty(preview.textProperty(), PREVIEW);
		
		tex.setOnAction(ev -> generate());
//		preview.setOnAction(ev -> {
//			try {
//				preview();
//			} catch (IOException ex) {
//				ex.printStackTrace();
//			}
//		});
		pdf.setOnAction(ev -> toPdf());
		
		ToolBar tb = new ToolBar(tex, preview, pdf);
		header.getChildren().addAll(menuBar, tb);
		return header;
	}
	
	private void setGlobalEventHandler(Node root) {
		root.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
			if (ev.getCode() == KeyCode.DELETE && treeView.getCurrentNode() != null) { consumeEventAfterAction(ev, () -> treeView.cutSelectedNode(false)); }
			else if (isControlShortcut(ev, "C") && ev.isShiftDown()) { 
			    consumeEventAfterAction(ev, () -> {
			        if (treeView.isSelectedItemRawContentCopiable()) treeView.copySelectedNodeRawContent();
			        else DialogsFactory.showPreFormattedWarning(primaryStage, WARNING, READ_THIS_MESSAGE, "");
			    }); 
			}
			else if (isControlShortcut(ev, "V") && ev.isShiftDown()) { consumeEventAfterAction(ev, treeView::pasteRawContentToSelectedNode); }
			else if (isControlShortcut(ev, "X")) { consumeEventAfterAction(ev, () -> treeView.cutSelectedNode                 (true)); }
			else if (isControlShortcut(ev, "C")) { consumeEventAfterAction(ev, () -> treeView.copySelectedNode                (    )); }
			else if (isControlShortcut(ev, "V")) { consumeEventAfterAction(ev, () -> treeView.pasteFromClipboardToSelectedNode(    )); }
		});
	}

	private static boolean isControlShortcut   (KeyEvent ev, String   symbol) { return ev.getText().equalsIgnoreCase(symbol) && ev.isControlDown(); }
	private static void consumeEventAfterAction(KeyEvent ev, Runnable action) { action.run(); ev.consume(); }
	
	private void setElements(List<Pair<Integer, LateXElement>> elts) {
		treeView.setElements(
			newTreeItem(elts.isEmpty() ? new PreprocessorCommand("") : elts.get(0).getValue()),
			namedLateXElements(elts.isEmpty() ? elts : elts.subList(1, elts.size())));
	}
	
	private void newDocument() {
		actionManager.perform(nonCancelableAction(() -> {
			createDocument();
			setElements(RichIterators.<LateXElement> of(new PreprocessorCommand(""), new Title()).zipWithIndex().toList());
		}).before(saveAction(() -> { save(); actionManager.reset(); })));
	}
	
	private void createDocument() {
		File file = chooseFile(primaryStage,true,"javatex",strings.getProperty(JAVATEX_FILES),"*.javatex");
		if (file != null) primaryStage.setTitle(currentFile.getName() + " - LateXEditor 4.0");
	}
	
	private void load() {
		actionManager.perform(new NonCancelableAction() {
			@Override
			protected void doAction() {
				File file = chooseFile(primaryStage,false,"javatex",strings.getProperty(JAVATEX_FILES),"*.javatex");
				loadFile(file);
			}
		});
	}

	private void loadFile(File file) {
		try {
			loadElements(file);
		} catch (FileNotFoundException e) {
			showError(
					primaryStage,
					strings.getProperty("error"),
					strings.getProperty("anErrorOccurredMessage"),
					String.format(strings.getProperty("unfoundFileError"),file.getAbsolutePath()));
		} catch (IOException e) {
			showPreFormattedError(primaryStage,ERROR,AN_ERROR_OCCURRED_MESSAGE,IO_LOAD_ERROR);
		} catch (WrongFormatException e) {
			showError(
					primaryStage,
					strings.getProperty(ERROR),
					strings.getProperty(AN_ERROR_OCCURRED_MESSAGE),
					String.format(strings.getProperty(MALFORMED_JAVATEX_ERROR),e.getMessage()));
		}
	}
	
	private void loadElements(File file) throws IOException, FileNotFoundException, WrongFormatException {
		if (file != null) {
			currentDir  = file.getParentFile();
			currentFile = file;
			lm.getParameters().clear();
			
			List<Pair<Integer,LateXElement>> elts = JavatexIO.readFromJavatex(file,lm.getParameters());
			setElements(elts);
			
			primaryStage.setTitle(currentFile.getName() + " - LateXEditor 4.1");
			actionManager.isSavedProperty().set(true);
			actionManager.reset();
		}
	}
	
	private void save() {
		actionManager.perform(new SaveAction() {
			@Override
			public void save() {
				try {
					TreeItem<NamedObject<LateXElement>> currentNode = treeView.getCurrentNode();
					if (!(currentNode.getValue().bean instanceof Template))
						currentNode.getValue().bean.setText(userTextArea.getText());
		
					if (currentFile != null) 
						JavatexIO.saveAsJavatex(currentFile,treeView.getLateXElements(),lm); 
				} catch (IOException e) {
					DialogsFactory.showPreFormattedError(primaryStage,ERROR,AN_ERROR_OCCURRED_MESSAGE,IO_SAVE_ERROR);
				}
			}
		});
	}
	
	private File chooseFile(Window window, boolean save, String wantedExtension, String filterName, String... extensions) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(strings.getProperty(NEW_DOCUMENT));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(filterName,extensions));
        if (currentDir != null) chooser.setInitialDirectory(currentDir);
			
		File selectedFile = save ? chooser.showSaveDialog(window) : chooser.showOpenDialog(window);
		if (selectedFile != null) {
			currentFile = selectedFile;
			currentDir  = currentFile.getParentFile();			
			return JavatexIO.fixExtension(selectedFile,wantedExtension);
		}
		return null;
    }
	
	public void toPdf() {
		if (currentFile != null) {
			generate();

			StringBuilder sb = new StringBuilder();
			Function<String, Consumer<String>> consumerFactory = s -> str -> {
				sb.append(str);
				sb.append("\n");
			};

			try {
				ProcessBuilder pb          = JavatexIO.toPdfProcessBuilder(currentDir,currentFile);
				Process        p           = pb.start();
				StreamPrinter  inputStream = new StreamPrinter(p.getInputStream(),consumerFactory.apply(""));
				StreamPrinter  errorStream = new StreamPrinter(p.getErrorStream(),consumerFactory.apply(""));
				new Thread(inputStream).start();
				new Thread(errorStream).start();
				p.waitFor();

				outputTextArea.clear();
				outputTextArea.setText(sb.toString());
				outputTextArea.positionCaret(outputTextArea.getLength());
			} catch (IOException | InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private void generate() {
		try {
			List<LateXElement> lateXElements = treeView.getLateXElements().getValue();
			if (lateXElements.isEmpty()) save();
			String path = currentFile.getAbsolutePath();
			JavatexIO.toTex(lm,lateXElements,path);

			outputCode.setLanguage(LANGUAGES.get("LaTeX"));
			outputCode.setCode(Source.fromFile(FileUtils.toExtension(path,".tex"),Codec.UTF8()).mkString());
		} catch (Exception e) {
			DialogsFactory.showPreFormattedError(primaryStage,ERROR,AN_ERROR_OCCURRED_MESSAGE,UNFOUND_FILE_ERROR);
			e.printStackTrace();
		}
	}

	/**
	 * I have been forced to create it to run it under Eclipse after some strange bug... nevermind
	 * @param args
	 */
	public static void main(String[] args) { launch(args); }

	static {
		LANGUAGES = new HashMap<>();
		LANGUAGES.put("Java"      ,"text/x-java"    );
		LANGUAGES.put("C++"       ,"text/x-c++src"  );
		LANGUAGES.put("C"         ,"text/x-csrc"    );
		LANGUAGES.put("Scala"     ,"text/x-scala"   );
		LANGUAGES.put("LaTeX"     ,"text/x-stex"    );
		LANGUAGES.put("Javascript","text/javascript");
		LANGUAGES.put("Python"    ,"text/x-python"  );
	}

	// load the templates and all associated localized texts
	static {
		Settings.init();
		boolean success = Templates.init();
		if (!success) showError(null,ERROR,AN_ERROR_OCCURRED_MESSAGE,UNDEFINED_HOME);
	}
}