package guifx;

import static guifx.utils.DialogsFactory.showError;
import static guifx.utils.DialogsFactory.showPreFormattedError;
import static guifx.utils.Settings.bindProperty;
import static guifx.utils.Settings.properties;
import static guifx.utils.Settings.strings;
import static java.util.Arrays.asList;
import static javafx.scene.input.KeyCombination.ALT_DOWN;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;
import static latex.elements.Templates.TEMPLATES;
import guifx.actions.ActionManager;
import guifx.components.CodeEditor;
import guifx.components.ControlledTreeView;
import guifx.utils.JavatexIO;
import guifx.utils.NamedObject;
import guifx.utils.Settings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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

import org.controlsfx.dialog.Dialogs;

import scala.collection.mutable.StringBuilder;
import scala.io.Codec;
import scala.io.Source;
import utils.StreamPrinter;
import utils.TokenReader;

public class LatexEditor extends Application {
	private static final Map<Integer, List<String>>	NODES_TYPES_MAP;
	private static final Map<String, String>		LANGUAGES;
	public static final String						LATEX_HOME			= System.getenv("LATEX_HOME").replace(System.getProperty("file.separator"),"/");

	private static final int						INSERT_HEAD			= 0;
	private static final int						INSERT_TAIL			= 1;

	public static final Font						subtitlesFont		= Font.font(null,FontWeight.BOLD,13);

	private File									currentDir			= new File(LATEX_HOME);
	private File									currentFile			= null;

	private boolean									saved				= false;
	private List<LateXElement>						savedlateXElements	= new ArrayList<>();

	private List<LateXElement>						lateXElements		= new ArrayList<>();
	private final LateXMaker						lm					= new LateXMaker();

	private Stage									primaryStage;

	private ControlledTreeView<NamedObject<LateXElement>> treeView;
	
	private ContextMenu								templatesList		= new ContextMenu();
	private MenuBar									menuBar;

	private TextArea								userTextArea;
	private TextArea								outputTextArea;
	private CodeEditor								outputCode;
	private MenuItem								generate;
	private Label									info;

	private Consumer<Node>							setEditorZone;
	private Node									textMode;
	private SplitPane								splitPane;

	private final LateXPidia						encyclopedia		= new LateXPidia();
	private final ActionManager						actionManager		= new ActionManager();

	@Override
	public void start(Stage primaryStage) {
		setTree();

		this.primaryStage = primaryStage;
		VBox root         = new VBox(10);
		Node editZone     = setEditZone();
		setMenuBar();

		VBox header = setHeader();
		root.getChildren().addAll(header,editZone);
		setGlobalEventHandler(root);

		Scene scene = new Scene(root,0,0);
		primaryStage.setTitle(strings.getProperty("frameTitle"));
		primaryStage.setScene(scene);

		Screen      screen = Screen.getPrimary();
		Rectangle2D bounds = screen.getVisualBounds();

		primaryStage.setX(bounds.getMinX());
		primaryStage.setY(bounds.getMinY());
		primaryStage.setWidth(bounds.getWidth());
		primaryStage.setHeight(bounds.getHeight());
		primaryStage.show();
	}
	
	private void setTree() {
		treeView = new LateXEditorTreeView(newTreeItem(new PreprocessorCommand("")),actionManager);
		treeView.setMinSize(200,50);
		treeView.getRoot().setExpanded(true);
		treeView.getSelectionModel().selectedItemProperty().addListener(updateTreeOnChange());

		treeView.getRoot().getChildren().add(newTreeItem(new Title()));
		// tree.setCellFactory(new Callback<TreeView<NamedObject<LateXElement>>,
		// TreeCell<NamedObject<LateXElement>>>() {
		// public TreeCell<NamedObject<LateXElement>>
		// call(TreeView<NamedObject<LateXElement>> param) {
		// final TreeCell<NamedObject<LateXElement>> cell = new
		// TreeCell<NamedObject<LateXElement>>() {
		// @Override
		// public void updateItem(NamedObject<LateXElement> item, boolean empty)
		// {
		// super.updateItem(item,empty);
		// if (!empty && item != null) {
		// textProperty().bind(item.nameProperty());
		// String url;
		// if ((url = icons.get(item.bean.getType())) != null) {
		// ImageView icon = new ImageView(new
		// Image(getClass().getResourceAsStream(url)));
		// if (icon != null) {
		// setGraphic(icon);
		// System.out.println(getUserData());
		// }
		// }
		// } else {
		// textProperty().unbind();
		// setText("");
		// }
		// }
		// };
		// return cell;
		// }
		// });
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
					buildAvailableTemplatesList((Template) newItem.getValue().bean);
				else {
					userTextArea.setText(newItem.getValue().bean.getText());
					setEditorZone.accept(textMode);
				}
				splitPane.setDividerPositions(0.5);
				splitPane.autosize();
				bindProperty(info.textProperty(),newItem.getValue().bean.getType() + "Tip");
				currentNode = newItem;
			}
		};
	}
	
	private void buildAvailableTemplatesList(Template t) {
		templatesList.getItems().clear();
		// creation of the UI elements
		Button showMenu = new Button();
		bindProperty(showMenu.textProperty(),"showAvailableTemplates");
		showMenu.setOnAction(ev -> {
			if (!templatesList.isShowing())
				templatesList.show(showMenu,Side.RIGHT,0,0);
			else
				templatesList.hide();
		});

		BorderPane borderPane = new BorderPane();
		borderPane.setTop(showMenu);
		borderPane.setPadding(new Insets(15));
		borderPane.setPrefHeight(300);
		BorderPane.setAlignment(showMenu,Pos.CENTER);
		BorderPane.setMargin(showMenu,new Insets(10,10,30,10));

		// creation of the popup menu
		Function<String, Consumer<List<Template>>> createMenu = title -> {
			Menu menu = new Menu(title);
			return templates -> {
				templates.stream().forEach(template -> {
					MenuItem item = new MenuItem(template.getTemplateName());
					item.setOnAction(ev -> {
						t.copyFrom(template);
						TemplateForm form = new TemplateForm(t);
						borderPane.setCenter(form);
						BorderPane.setAlignment(form,Pos.CENTER);
					});
					menu.getItems().add(item);
				});
				templatesList.getItems().add(menu);
			};
		};

		switch (t.getType()) {
			case "template":
				for (Map.Entry<String, List<Template>> entry : TEMPLATES.entrySet())
					if (!entry.getKey().equals("title"))
						createMenu.apply(entry.getKey()).accept(entry.getValue());
				break;
			case "title":
				createMenu.apply("titlePage").accept(TEMPLATES.get("titlePage"));
				break;
			default:
				throw new IllegalArgumentException(String.format("Unkown type %s",t.getType()));
		}

		TemplateForm form = new TemplateForm(t);
		borderPane.setCenter(form);
		BorderPane.setAlignment(form,Pos.CENTER);

		setEditorZone.accept(borderPane);
	}
	
	private Node setEditZone() {
		VBox      textEditor  = setTextEditor();
		Accordion leftToolbar = setLeftToolbar();
		SplitPane splitPane   = setSplitPane(textEditor);

		BorderPane borderPane = new BorderPane();
		borderPane.setLeft(leftToolbar);
		borderPane.setCenter(splitPane);
		borderPane.setPadding(new Insets(15));

		HBox.setHgrow(textEditor        ,Priority.ALWAYS);
		HBox.setHgrow(treeView,Priority.NEVER);
		treeView.setMinWidth(210);
		treeView.setMinHeight(500);

		return borderPane;
	}

	private SplitPane setSplitPane(VBox textEditor) {
		outputTextArea = new TextArea();
		outputTextArea.setMinHeight(50);
		outputTextArea.setEditable(false);
		outputTextArea.setPrefHeight(100);

		splitPane = new SplitPane();
		splitPane.setOrientation(Orientation.VERTICAL);
		splitPane.getItems().addAll(textMode = new HBox(textEditor,setOutputCode()),outputTextArea);
		splitPane.setDividerPositions(0.5);
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

		bindProperty(label.textProperty(),"selectLanguage");
		bindProperty(clear.textProperty(),"clear");
		bindProperty(paste.textProperty(),"pasteToEditor");

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
		bindProperty(info.textProperty(),"editZoneTip");
		info.setFont(subtitlesFont);
		
		userTextArea = new TextArea();
		userTextArea.setPrefSize(600,550);
		userTextArea.setPrefHeight(420);

		VBox textEditor = new VBox(info,userTextArea);
		textEditor.setPadding(new Insets(5));
		textEditor.setSpacing(5);
		textEditor.setMinWidth(420);
		return textEditor;
	}

	private Accordion setLeftToolbar() {
		TitledPane shortcutsPane = setSpecialCharsShortcut();
		TitledPane treePane      = setTreePane();

		Accordion accordion = new Accordion();
		accordion.getPanes().addAll(treePane,shortcutsPane);
		accordion.setExpandedPane(treePane);
		accordion.setPadding(new Insets(10));
		
		return accordion;
	}
	
	private TitledPane setTreePane() {
		TitledPane treePane = new TitledPane("",treeView);
		bindProperty(treePane.textProperty(),"treeTitle");
		treeView.setPadding(new Insets(50,5,5,5));
		return treePane;
	}

	private TitledPane setSpecialCharsShortcut() {
		String[] ops = { 
			"\\cdot","+","-","\\frac{}{}","\\sqrt[]{}",
			"\\forall","\\partial","\\exists","\\nexists","\\varnothing",
			 "\\bigcap","\\bigcup","\\bigint","\\prod","\\sum",
			 "\\nabla","\\in","\\notin","\\ni","",
			 "^{}","_{}","\\leq","\\geq","\\neq",
			"\\mid\\mid.\\mid\\mid"
		};

		String[] ctes = { 
			"\\alpha","\\beta","\\gamma","\\delta","\\epsilon","\\mu","\\nu","\\xi","\\pi","\\rho",
	        "\\omega","\\Omega","\\theta","\\Delta","\\Psi","\\eta","\\lambda","\\sigma","\\tau",
			"\\chi","\\phi","\\infty"
	    };

		Image img = getResourceImage(properties.getProperty("operatorsIcon"));
		IconSelectionView operators = new IconSelectionView(img,6,5,ops,strings.getObservableProperty("operators"));
		operators.setActionListener(e -> {
			userTextArea.cut();
			userTextArea.insertText(userTextArea.getCaretPosition(),e.getActionCommand());
		});

		img = getResourceImage(properties.getProperty("alphabetIcon"));
		IconSelectionView greekAlphabet = new IconSelectionView(img,5,5,ctes,strings.getObservableProperty("greekAlphabet"));
		greekAlphabet.setActionListener(e -> {
			userTextArea.cut();
			userTextArea.insertText(userTextArea.getCaretPosition(),e.getActionCommand());
		});

		IconSelectionBox box = new IconSelectionBox();
		box.addSelectionView(operators);
		box.addSelectionView(greekAlphabet);
		box.setPadding(new Insets(5,5,5,20));
		
		TitledPane boxPane = new TitledPane("",box);
		bindProperty(boxPane.textProperty(),"boxTitle");
		return boxPane;
	}
	
	private void setMenuBar() {
		// set main menu bar
		menuBar          = new MenuBar();
		Menu menuFile    = new Menu();
		Menu menuEdit    = new Menu();
		Menu menuOptions = new Menu();
		Menu menuHelp    = new Menu();

		// set submenu File
		MenuItem newDoc = new MenuItem();
		MenuItem save   = new MenuItem();
		MenuItem saveAs = new MenuItem();
		MenuItem load   = new MenuItem();
		MenuItem quit   = new MenuItem();
		generate        = new MenuItem();
		menuFile.getItems().addAll(newDoc,load,save,saveAs,generate,quit);
		
		// set submenu Edit
		MenuItem undo = new MenuItem();
		MenuItem redo = new MenuItem();
		undo.disableProperty().bind(actionManager.hasPreviousProperty().not());
		redo.disableProperty().bind(actionManager.hasNextProperty().not());
		menuEdit.getItems().addAll(undo,redo);

		// set submenu Options
		MenuItem  settings    = new MenuItem();
		ImageView checkedIcon = new ImageView(getResourceImage(properties.getProperty("checkedIcon")));
		menuOptions.getItems().addAll(settings,Settings.getChooseLanguageMenu(checkedIcon),Settings.getChooseStyleMenu(checkedIcon),
				Settings.getChooseThemeMenu(checkedIcon,s -> outputCode.refresh()));

		// set submenu Help
		MenuItem doc = new MenuItem();
		doc.setOnAction(ev -> {
			if (!encyclopedia.isShowing())
				encyclopedia.show();
		});
		menuHelp.getItems().add(doc);

		// set accelerators for all menu items
		newDoc  .setAccelerator(new KeyCharacterCombination("N",CONTROL_DOWN         ));
		save    .setAccelerator(new KeyCharacterCombination("S",CONTROL_DOWN         ));
		saveAs  .setAccelerator(new KeyCharacterCombination("S",CONTROL_DOWN,ALT_DOWN));
		load    .setAccelerator(new KeyCharacterCombination("L",CONTROL_DOWN         ));
		generate.setAccelerator(new KeyCharacterCombination("G",CONTROL_DOWN         ));
		quit    .setAccelerator(new KeyCharacterCombination("Q",CONTROL_DOWN         ));
		undo    .setAccelerator(new KeyCharacterCombination("Z",CONTROL_DOWN         ));
		redo    .setAccelerator(new KeyCharacterCombination("Y",CONTROL_DOWN         ));
		settings.setAccelerator(new KeyCharacterCombination("O",CONTROL_DOWN         ));
		doc     .setAccelerator(new KeyCharacterCombination("H",CONTROL_DOWN         ));
		generate.setDisable(true);

		// set actions
		newDoc  .setOnAction(ev -> { 
			createDocument();
			lateXElements = new ArrayList<>();
			lateXElements.add(new PreprocessorCommand(""));
			lateXElements.add(new Title());
			setElements(IntStream.range(0,2).mapToObj(k -> new Pair<>(k,lateXElements.get(k))).collect(Collectors.toList()));
			
			setSaved(false);
			save();
		});
		save    .setOnAction(ev -> save());
		saveAs  .setOnAction(ev -> { createDocument(); save(); });
		load    .setOnAction(ev -> load());
		generate.setOnAction(ev -> generate());
		quit    .setOnAction(ev -> System.exit(0));
		undo    .setOnAction(ev -> actionManager.undo());
		redo    .setOnAction(ev -> actionManager.redo());
		settings.setOnAction(ev -> new PreferencesPane(lm.getParameters()));

		// bind the text properties
		List<String> properties = Arrays.asList("file","edit","options","help","documentation","newDocument","save","saveAs","load",
				"generate","quit","undo","redo","settings");
		List<MenuItem> menus = Arrays.asList(menuFile,menuEdit,menuOptions,menuHelp,doc,newDoc,save,saveAs,load,generate,quit,undo,redo,settings);
		IntStream.range(0,menus.size()).forEach(i -> bindProperty(menus.get(i).textProperty(),properties.get(i)));
		
		menuBar.getMenus().addAll(menuFile,menuEdit,menuOptions,menuHelp);
	}

	private VBox setHeader() {
		VBox header           = new VBox();
		ImageView pdfIcon     = new ImageView(getResourceImage(properties.getProperty("pdfIcon"    )));
		ImageView texIcon     = new ImageView(getResourceImage(properties.getProperty("texIcon"    )));
		ImageView previewIcon = new ImageView(getResourceImage(properties.getProperty("previewIcon")));
		
		Button tex     = new Button("",texIcon);
		Button preview = new Button("",previewIcon);
		Button pdf     = new Button("",pdfIcon);
		bindProperty(tex    .textProperty(),"generateLatex");
		bindProperty(pdf    .textProperty(),"generatePdf");
		bindProperty(preview.textProperty(),"preview");
		
		tex.setOnAction(ev -> generate());
		preview.setOnAction(ev -> {
			try {
				preview();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		});
		pdf.setOnAction(ev -> toPdf());
		
		ToolBar tb = new ToolBar(tex,preview,pdf);
		header.getChildren().addAll(menuBar,tb);
		return header;
	}
	
	private void setGlobalEventHandler(Node root) {
		root.addEventHandler(KeyEvent.KEY_PRESSED,new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ev) {
				if      (ev.getCode() == KeyCode.DELETE && treeView.getCurrentNode() != null) { treeView.cutSelectedNode                 (false); ev.consume(); }
				else if (ev.getText().equalsIgnoreCase("X") && ev.isControlDown())            { treeView.cutSelectedNode                 (true ); ev.consume(); }
				else if (ev.getText().equalsIgnoreCase("C") && ev.isControlDown())            { treeView.copySelectedNode                (     ); ev.consume(); }
				else if (ev.getText().equalsIgnoreCase("V") && ev.isControlDown())            { treeView.pasteFromClipboardToSelectedNode(     ); ev.consume(); }
			}
		});
	}

	private TreeItem<NamedObject<LateXElement>> newTreeItem(LateXElement elt) {
		String command = elt.getType();
		String url     = properties.getProperty(command + "Icon");
		Node   icon    = new ImageView(getResourceImage(url != null ? url : properties.getProperty("leafIcon")));

		NamedObject<LateXElement> no = new NamedObject<LateXElement>(strings.getObservableProperty(command),elt);
		return icon == null ? new TreeItem<>(no) : new TreeItem<>(no,icon);
	}

	private void save() {
		try {
			TreeItem<NamedObject<LateXElement>> currentNode = treeView.getCurrentNode();
			if (!(currentNode.getValue().bean instanceof Template))
				currentNode.getValue().bean.setText(userTextArea.getText());

			if (currentFile != null) {
				File f                           = new File(currentFile.getAbsolutePath());
				BufferedWriter fw                = new BufferedWriter(new FileWriter(f));
				NamedList<LateXElement> elements = getElements();
				Iterator<String>        names    = elements.getKey().iterator();
				lateXElements                    = new ArrayList<>(elements.getValue());
				List<LateXElement>      state    = new ArrayList<>();

				fw.write(lm.getParameters().textify(new StringBuilder()).toString());
				for (LateXElement l : lateXElements) {
					fw.write(String.format("%s %s\n",names.next(),l.textify()));
					state.add(l.clone());
				}
				fw.flush();
				fw.close();
				setSaved(true);
				savedlateXElements = state;
			}
		} catch (IOException e) {
			Dialogs.create().owner(primaryStage).title(strings.getProperty("error"))
					.masthead(strings.getProperty("anErrorOccurredMessage")).message(String.format(strings.getProperty("ioSaveError")))
					.showError();
		}
	}

	public void setSaved(boolean b) {
		if (!saved && b)      primaryStage.setTitle(primaryStage.getTitle().substring(1));
		else if (saved && !b) primaryStage.setTitle("*" + primaryStage.getTitle());
		saved = b;
	}

	private void generate() {
		try {
			if (savedlateXElements.isEmpty()) save();
			String path = currentFile.getAbsolutePath();
			JavatexIO.toTex(lm,savedlateXElements,path);

			outputCode.setLanguage(LANGUAGES.get("LaTeX"));
			outputCode.setCode(Source.fromFile(new File(path),Codec.UTF8()).mkString());
		} catch (Exception e) {
			Dialogs.create().owner(primaryStage).title(strings.getProperty("error"))
				.masthead(strings.getProperty("anErrorOccurredMessage")).message(strings.getProperty("unfoundFileErrorMessage"))
				.showError();
			e.printStackTrace();
			// Dialogs.create().owner(primaryStage).
			// title(strings.getProperty("error")).
			// masthead(strings.getProperty("anErrorOccurredMessage")).
			// showException(e);
		}
	}

	private void createDocument() {
		File file = chooseFile(primaryStage,true,"javatex",strings.getProperty("javatexFiles"),"*.javatex");
		if (file != null) {
			primaryStage.setTitle(currentFile.getName() + " - LateXEditor 4.0");
			generate.setDisable(false);
			actionManager.reset();
		}
	}
	
	private File chooseFile(Window window, boolean save, String wantedExtension, String filterName, String... extensions) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(strings.getProperty("newDocument"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(filterName,extensions));
        if (currentDir != null) chooser.setInitialDirectory(currentDir);
			
		File selectedFile = save ? chooser.showSaveDialog(window) : chooser.showOpenDialog(window);
		if (selectedFile != null) {
			currentFile = selectedFile;
			currentDir  = currentFile.getParentFile();
			
			String s = selectedFile.getPath();
			int i = s.indexOf(".");
			if (i == -1 || !s.substring(i).equals(wantedExtension)) selectedFile = new File(s + wantedExtension);
		}
        return selectedFile;
    }

	private class NamedList<E> extends Pair<List<String>,List<E>> {
		private static final long serialVersionUID = 1L;
		public NamedList(List<String> a, List<E> b) { super(a,b); }
	}

	private NamedList<LateXElement> getElements(TreeItem<NamedObject<LateXElement>> node, String level) {
		List<LateXElement> elements = new LinkedList<>();
		List<String>       names    = new LinkedList<>();
		elements.add(node.getValue().bean);
		names   .add(level);
		if (!node.isLeaf()) {
			for (TreeItem<NamedObject<LateXElement>> elt : node.getChildren()) {
				NamedList<LateXElement> childResult = getElements(elt,level + ">");
				names   .addAll(childResult.getKey());
				elements.addAll(childResult.getValue());
			}
		}
		return new NamedList<>(names,elements);
	}

	public NamedList<LateXElement> getElements() {
		return getElements(treeView.getRoot(),"");
	}
	
	private void setElements(List<Pair<Integer,LateXElement>> elts) {
		treeView.setElements(
			newTreeItem(elts.isEmpty() ? new PreprocessorCommand("") : elts.get(0).getValue()),
			elts.stream().map(pair -> new Pair<>(pair.getKey(),LateXEditorTreeView.newNamedObject(pair.getValue()))).collect(Collectors.toList()),
			LateXEditorTreeView::newTreeItem);
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
			} catch (IOException | InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void preview() throws IOException {
		if (currentFile != null) {
			generate();

			String         path = currentFile.getCanonicalPath();
			ProcessBuilder pb   = new ProcessBuilder("latex","-halt-on-error",String.format("%s.tex",path.substring(0,path.lastIndexOf("."))));
			pb.directory(currentDir.getAbsoluteFile());

			StringBuilder sb = new StringBuilder();
			Function<String, Consumer<String>> consumerFactory = s -> str -> {
				sb.append(str);
				sb.append("\n");
			};

			try {
				Process p = pb.start();
				StreamPrinter inputStream = new StreamPrinter(p.getInputStream(),consumerFactory.apply(""));
				StreamPrinter errorStream = new StreamPrinter(p.getErrorStream(),consumerFactory.apply(""));
				new Thread(inputStream).start();
				new Thread(errorStream).start();
				p.waitFor();

				outputTextArea.clear();
				outputTextArea.setText(sb.toString());

				pb = new ProcessBuilder("yap",String.format("%s.dvi",path.substring(0,path.lastIndexOf("."))));
				pb.start();
			} catch (IOException | InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void load() {
		saved = false;
		FileChooser f = new FileChooser();
		f.setTitle("Charger un document");
		f.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Fichier JavateX","*.javatex"));

		if (currentDir != null)
			f.setInitialDirectory(currentDir);

		File file = f.showOpenDialog(primaryStage);
		try {
			if (file != null) {
				currentDir  = file.getParentFile();
				currentFile = file;
				lm.getParameters().clear();
				
				List<Pair<Integer,LateXElement>> elts = readFromJavatex(file);
				setElements(elts);

				primaryStage.setTitle(currentFile.getName() + " - LateXEditor 4.1");
				savedlateXElements = elts.stream().map(kv -> kv.getValue()).collect(Collectors.toList());
				setSaved(true);
				generate.setDisable(false);
			}
			actionManager.reset();
		} catch (FileNotFoundException e) {
			showError(
				primaryStage,
				strings.getProperty("error"),
				strings.getProperty("anErrorOccurredMessage"),
				String.format(strings.getProperty("unfoundFileError"),file.getAbsolutePath()));
		} catch (IOException e) {
			showPreFormattedError(primaryStage,"error","anErrorOccurredMessage","ioLoadError");
		} catch (WrongFormatException e) {
			showError(
				primaryStage,
				strings.getProperty("error"),
				strings.getProperty("anErrorOccurredMessage"),
				String.format(strings.getProperty("malformedJavatexError"),e.getMessage()));
		}
	}
	
	private List<Pair<Integer,LateXElement>> readFromJavatex(File f) 
			throws IOException, FileNotFoundException, WrongFormatException {
		TokenReader                      tr   = new TokenReader(new FileReader(f),"##");
		List<Pair<Integer,LateXElement>> res  = new LinkedList<>();  
		
		String s;
		while ((s = tr.readToNextToken()) != null) {
			String decl    = s.trim();
			String content = tr.readToNextToken().trim();
			
			switch (decl) {
				case "packages"        : lm.getParameters().addPackages(content.split("[;\\s+]|;\\s+")); break;
				case "commands"        : lm.getParameters().include(content.split("[;\\s+]|;\\s+"    )); break;
				case "documentSettings": lm.getParameters().loadSettings(content);                       break;
				default:
					Pattern p = Pattern.compile("(>*)\\s*(\\w+)\\s*(\\[(.*)\\])?");
					Matcher m = p.matcher(decl);

					if (m.matches()) {
						String param = (m.group(4) == null || m.group(4).isEmpty()) ? "" : m.group(3);
						res.add(new Pair<>(m.group(1).length(),LateXElement.newLateXElement(m.group(2) + param,content)));
					} else
						throw new WrongFormatException(decl);
			}
		}
		tr.close();
		return res;
	}
	
	/**
	 * I have been forced to create it to run it under Eclipse after some strange bug... nevermind
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}

	// initializers and utilitary methods/classes
	static {
		NODES_TYPES_MAP = new HashMap<>();
		NODES_TYPES_MAP.put(0,asList("title"));
		NODES_TYPES_MAP.put(1,asList("chapter"));
		NODES_TYPES_MAP.put(2,asList("section"));
		NODES_TYPES_MAP.put(3,asList("subsection"));
		NODES_TYPES_MAP.put(4,asList("subsubsection"));
		NODES_TYPES_MAP.put(5,asList("paragraph","list","image","code","latex","template"));
	}

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
		if (!success) 
			Dialogs.create().owner(null)
				.title(strings.getProperty("error")).masthead(strings.getProperty("anErrorOccurredMessage"))
				.message(strings.getProperty("undefinedHome")).showError();
	}
	
	private static Image getResourceImage(String path) {
		return new Image(LatexEditor.class.getResourceAsStream(path));
	}
	
	private class WrongFormatException extends Exception { 
		private static final long	serialVersionUID	= 1L;
		public WrongFormatException(String msg) { super(msg); }
	}
}