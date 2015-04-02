package guifx;

import static guifx.utils.Settings.properties;
import static guifx.utils.Settings.strings;
import static java.util.Arrays.asList;
import static javafx.scene.input.KeyCombination.ALT_DOWN;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;
import static latex.elements.Templates.TEMPLATES;
import guifx.actions.ActionManager;
import guifx.actions.CancelableAction;
import guifx.utils.CodeEditor;
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
import java.util.Deque;
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
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
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
	public static final String						LATEX_HOME		= 
		System.getenv("LATEX_HOME").replace(System.getProperty("file.separator"),"/");

	private static final int						INSERT_HEAD		= 0;
	private static final int						INSERT_TAIL		= 1;

	public static final Font						subtitlesFont	= Font.font(null,FontWeight.BOLD,13);

	private File									currentDir		= new File(LATEX_HOME);
	private File									currentFile		= null;

	private boolean									saved			= false;
	private DocumentState							savedState		= new DocumentState(new ArrayList<>());

	private List<LateXElement>						lateXElements	= new ArrayList<>();
	private final LateXMaker						lm				= new LateXMaker();

	private Stage									primaryStage;
	private TreeView<NamedObject<LateXElement>>		tree;
	private TreeItem<NamedObject<LateXElement>>		treeRoot;
	private TreeItem<NamedObject<LateXElement>>		currentNode		= null;
	private TreeItem<NamedObject<LateXElement>>		clipBoard		= null;

	private ContextMenu								addMenu			= new ContextMenu();
	private ContextMenu								templatesList	= new ContextMenu();
	private MenuBar									menuBar;

	private TextArea								userTextArea;
	private TextArea								outputTextArea;
	private CodeEditor								outputCode;
	private MenuItem								generate;
	private Label									info;

	private Consumer<Node>							setEditorZone;
	private Node									textMode;
	private SplitPane								splitPane;

	private final LateXPidia						encyclopedia	= new LateXPidia();
	private final ActionManager						actionManager	= new ActionManager();

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

	private void setGlobalEventHandler(Node root) {
		root.addEventHandler(KeyEvent.KEY_PRESSED,new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ev) {
				if      (ev.getCode() == KeyCode.DELETE && currentNode != null)    { cutNode(false); ev.consume(); }
				else if (ev.getText().equalsIgnoreCase("X") && ev.isControlDown()) { cutNode(true ); ev.consume(); }
				else if (ev.getText().equalsIgnoreCase("C") && ev.isControlDown()) { copyNode ()   ; ev.consume(); }
				else if (ev.getText().equalsIgnoreCase("V") && ev.isControlDown()) { pasteNode()   ; ev.consume(); }
			}
		});
	}
	
	private VBox setHeader() {
		VBox header           = new VBox();
		ImageView pdfIcon     = new ImageView(new Image(LatexEditor.class.getResourceAsStream(properties.getProperty("pdfIcon"    ))));
		ImageView texIcon     = new ImageView(new Image(LatexEditor.class.getResourceAsStream(properties.getProperty("texIcon"    ))));
		ImageView previewIcon = new ImageView(new Image(LatexEditor.class.getResourceAsStream(properties.getProperty("previewIcon"))));

		Button tex     = new Button("",texIcon);
		Button preview = new Button("",previewIcon);
		Button pdf     = new Button("",pdfIcon);
		tex    .textProperty().bind(strings.getObservableProperty("generateLatex"));
		pdf    .textProperty().bind(strings.getObservableProperty("generatePdf"));
		preview.textProperty().bind(strings.getObservableProperty("preview"));

		tex.setOnAction(ev -> generate());
		preview.setOnAction(ev -> {
			try {
				preview();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		});
		pdf.setOnAction(ev -> {
			try {
				toPdf();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		});
		ToolBar tb = new ToolBar(tex,preview,pdf);
		header.getChildren().addAll(menuBar,tb);
		return header;
	}

	private TreeItem<NamedObject<LateXElement>> newTreeItem(LateXElement l) {
		String command = l.getType();
		String url     = properties.getProperty(command + "Icon");
		Node   icon    = new ImageView(new Image(getClass().getResourceAsStream(url != null ? url : properties.getProperty("leafIcon"))));

		NamedObject<LateXElement> no = new NamedObject<LateXElement>(strings.getObservableProperty(command),l);
		return icon == null ? new TreeItem<>(no) : new TreeItem<>(no,icon);
	}

	private void setTree() {
		treeRoot  = newTreeItem(new PreprocessorCommand(""));
		tree      = new TreeView<>(treeRoot);
		treeRoot.getChildren().add(newTreeItem(new Title()));

		tree.setMinSize(200,50);
		treeRoot.setExpanded(true);
		currentNode = treeRoot;
		tree.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mev) {
				if (mev.getButton().equals(MouseButton.SECONDARY))
					openContextMenu(new Point2D(mev.getScreenX(),mev.getScreenY()));
				else
					addMenu.hide();
			}

			private void openContextMenu(Point2D pt) {
				// creation of the relevant contextual popup
				addMenu.hide();
				addMenu.getItems().clear();
				LateXElement elt = currentNode.getValue().bean;

				buildAddMenus(elt);
				buildClipboardMenus(elt);
				buildDeleteMenu();

				// display the popup
				addMenu.show(tree,pt.getX() + 10,pt.getY() + 10);
			}
		});

		tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		tree.getSelectionModel().selectedItemProperty().addListener(
			(ObservableValue<? extends TreeItem<NamedObject<LateXElement>>> ov, TreeItem<NamedObject<LateXElement>> formerItem,
				TreeItem<NamedObject<LateXElement>> newItem) -> {
				if (formerItem != null && currentNode != null) {
					String text = userTextArea.getText();
					if (!currentNode.getValue().bean.getText().equals(text))
						setSaved(false);
					if (!(formerItem.getValue().bean instanceof Template))
						formerItem.getValue().bean.setText(userTextArea.getText());
				}
				if (newItem != null && newItem.getValue() != null) {
					if (newItem.getValue().bean instanceof Template)
						buildAvailableTemplatesList((Template) newItem.getValue().bean);
					else {
						userTextArea.setText(newItem.getValue().bean.getText());
						setEditorZone.accept(textMode);
					}
					splitPane.setDividerPositions(0.5);
					splitPane.autosize();
					info.textProperty().bind(strings.getObservableProperty(newItem.getValue().bean.getType() + "Tip"));
					currentNode = newItem;
				}
			});

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

	private void buildAvailableTemplatesList(Template t) {
		templatesList.getItems().clear();
		// creation of the UI elements
		Button showMenu = new Button();
		showMenu.textProperty().bind(strings.getObservableProperty("showAvailableTemplates"));
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

	private void buildClipboardMenus(LateXElement elt) {
		if (!(elt instanceof Title)) {
			MenuItem copy  = new MenuItem();
			MenuItem cut   = new MenuItem();
			MenuItem paste = new MenuItem();
			
			copy .textProperty().bind(strings.getObservableProperty("copy" ));
			cut  .textProperty().bind(strings.getObservableProperty("cut"  ));
			paste.textProperty().bind(strings.getObservableProperty("paste"));

			copy .setOnAction(ev -> copyNode());
			cut  .setOnAction(ev -> cutNode(true));
			paste.setOnAction(ev -> pasteNode());
			addMenu.getItems().addAll(copy,cut,paste);
		}
	}
	
	private void buildDeleteMenu() {
		MenuItem delete = new MenuItem();
		addMenu.getItems().add(delete);
		delete.textProperty().bind(strings.getObservableProperty("delete"));
		delete.setOnAction(ev -> cutNode(false));
		if (currentNode.getParent() == null) delete.setDisable(true);
	}

	private void buildAddMenus(LateXElement elt) {
		Menu addChildHead, addChildTail, addSibling = null;
		Map<Menu, Integer> map = null;

		// determine the main elements of the popup
		if (elt.getDepth() != LateXElement.DEPTH_MAX) {
			addMenu.getItems().add(addChildHead = new Menu());
			addMenu.getItems().add(addChildTail = new Menu());
			map = new HashMap<>();
			map.put(addChildHead,INSERT_HEAD);
			map.put(addChildTail,INSERT_TAIL);

			addChildHead.textProperty().bind(strings.getObservableProperty("addChildHead"));
			addChildTail.textProperty().bind(strings.getObservableProperty("addChildTail"));
		}

		if (elt.getDepth() != LateXElement.DEPTH_MIN) {
			addMenu.getItems().add(addSibling = new Menu());
			addSibling.textProperty().bind(strings.getObservableProperty("addSibling"));
		}

		// determine the secondary elements of the popup
		for (Integer depth : NODES_TYPES_MAP.keySet()) {
			// first, the children elements
			if (map != null) {
				map.entrySet().stream().forEach(entry -> {
					Menu addChild = entry.getKey();
					if (depth > elt.getDepth()) {
						if (!addChild.getItems().isEmpty())
							addChild.getItems().add(new SeparatorMenuItem());

						for (String type : NODES_TYPES_MAP.get(depth)) {
							MenuItem item = new MenuItem();
							item.textProperty().bind(strings.getObservableProperty(type));
							addChild.getItems().add(item);
							item.setOnAction(ev -> {
								addChild(type,entry.getValue());
								addMenu.hide();
							});
						}
					}
				});
			}

			// then, the sibling elements
			if (addSibling != null && depth == elt.getDepth()) {
				for (String type : NODES_TYPES_MAP.get(depth)) {
					MenuItem item = new MenuItem();
					item.textProperty().bind(strings.getObservableProperty(type));
					addSibling.getItems().add(item);
					item.setOnAction(event -> addSibling(type));
				}
			}
		}
	}

	private Node setEditZone() {
		// set the text editor
		info = new Label();
		userTextArea = new TextArea();
		userTextArea.setPrefSize(600,550);
		info.textProperty().bind(strings.getObservableProperty("editZoneTip"));
		info.setFont(subtitlesFont);

		VBox textEditor = new VBox(info,userTextArea);
		textEditor.setPadding(new Insets(5));
		textEditor.setSpacing(5);

		// set the left area (shortcuts for special characters)
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

		Image img = new Image(LatexEditor.class.getResourceAsStream("/data/Operateurs.png"));
		IconSelectionView operators = new IconSelectionView(img,6,5,ops,strings.getObservableProperty("operators"));
		operators.setActionListener(e -> {
			userTextArea.cut();
			userTextArea.insertText(userTextArea.getCaretPosition(),e.getActionCommand());
		});

		img = new Image(LatexEditor.class.getResourceAsStream("/data/AlphabetGrec.png"));
		IconSelectionView greekAlphabet = new IconSelectionView(img,5,5,ctes,strings.getObservableProperty("greekAlphabet"));
		greekAlphabet.setActionListener(e -> {
			userTextArea.cut();
			userTextArea.insertText(userTextArea.getCaretPosition(),e.getActionCommand());
		});

		IconSelectionBox box = new IconSelectionBox();
		box.addSelectionView(operators);
		box.addSelectionView(greekAlphabet);

		// set the left area (tree arborescence of the document)
		TitledPane treePane = new TitledPane("",tree);
		treePane.textProperty().bind(strings.getObservableProperty("treeTitle"));
		// tree.setPadding(new Insets(5,5,5,5));
		tree.setPadding(new Insets(50,5,5,5));

		TitledPane boxPane = new TitledPane("",box);
		boxPane.textProperty().bind(strings.getObservableProperty("boxTitle"));
		// box.setPadding(new Insets(5,5,5,5));
		box.setPadding(new Insets(5,5,5,20));

		// set the left area (add th tree and the shortcuts in an accordion)
		Accordion accordion = new Accordion();
		accordion.getPanes().addAll(treePane,boxPane);
		accordion.setExpandedPane(treePane);
		accordion.setPadding(new Insets(10));

		// set the code editor
		outputCode = new CodeEditor("");
		outputCode.setMinHeight(20);

		Label  label = new Label();
		Button clear = new Button();
		Button paste = new Button();
		ComboBox<String> languages = new ComboBox<>();
		languages.getItems().addAll(LANGUAGES.keySet());
		languages.setOnAction(ev -> outputCode.setLanguage(LANGUAGES.get(languages.getSelectionModel().getSelectedItem())));

		label.textProperty().bind(strings.getObservableProperty("selectLanguage"));
		clear.textProperty().bind(strings.getObservableProperty("clear"));
		paste.textProperty().bind(strings.getObservableProperty("pasteToEditor"));

		clear.setOnAction(ev -> outputCode.setCode(""));
		paste.setOnAction(ev -> {
			userTextArea.cut();
			userTextArea.insertText(userTextArea.getCaretPosition(),outputCode.getCodeAndSnapshot());
		});

		HBox buttons = new HBox(10,label,languages,clear,paste);
		buttons.setPadding(new Insets(10));
		outputCode.setBottom(buttons);

		outputTextArea = new TextArea();
		outputTextArea.setMinHeight(50);
		outputTextArea.setEditable(false);

		// merge all the elements
		textMode = new HBox(textEditor,outputCode);
		splitPane = new SplitPane();
		splitPane.setOrientation(Orientation.VERTICAL);
		splitPane.getItems().addAll(textMode,outputTextArea);

		this.setEditorZone = nodes -> splitPane.getItems().set(0,nodes);

		BorderPane borderPane = new BorderPane();
		borderPane.setLeft(accordion);
		borderPane.setCenter(splitPane);
		borderPane.setPadding(new Insets(15));

		// handle resize events
		HBox.setHgrow(textEditor,Priority.ALWAYS);
		HBox.setHgrow(tree      ,Priority.NEVER);
		tree.setMinWidth(210);
		tree.setMinHeight(500);
		textEditor.setMinWidth(420);
		userTextArea.setPrefHeight(420);
		outputTextArea.setPrefHeight(100);

		splitPane.setDividerPositions(0.5);
		splitPane.autosize();

		return borderPane;
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
		ImageView checkedIcon = new ImageView(new Image(getClass().getResourceAsStream(Settings.properties.getProperty("checkedIcon"))));
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
		IntStream.range(0,menus.size()).forEach(i -> menus.get(i).textProperty().bind(strings.getObservableProperty(properties.get(i))));
		
		menuBar.getMenus().addAll(menuFile,menuEdit,menuOptions,menuHelp);
	}

	private void save() {
		try {
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
				savedState = new DocumentState(state);
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
			if (savedState.getCurrentState().isEmpty())
				save();
			String path = currentFile.getAbsolutePath();
			int    i    = path.lastIndexOf(".");
			path        = i == -1 ? path + ".tex" : path.substring(0,i) + ".tex";
			lm.makeDocument(new File(path),savedState.getCurrentState());

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
		FileChooser f = new FileChooser();
		f.setTitle("Nouveau document");
		f.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier JavateX","*.javatex"));

		if (currentDir != null)
			f.setInitialDirectory(currentDir);

		File file = f.showSaveDialog(primaryStage);
		if (file != null) {
			currentDir = file.getParentFile();
			String s = file.getPath();
			int i = s.indexOf(".");
			if (i == -1 || !s.substring(i).equals(".javatex"))
				file = new File(s + ".javatex");

			currentFile = file;
			primaryStage.setTitle(currentFile.getName() + " - LateXEditor 4.0");
			generate.setDisable(false);
			actionManager.reset();
		}
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
		return getElements(treeRoot,"");
	}
	
	private void setElements(List<Pair<Integer,LateXElement>> elts) {
		tree.getSelectionModel().clearSelection();
		if (elts.isEmpty()) treeRoot = newTreeItem(new PreprocessorCommand(""));
		else {
			treeRoot = newTreeItem(elts.get(0).getValue());
			Deque<Pair<Integer,TreeItem<NamedObject<LateXElement>>>> stack = new LinkedList<>();
			stack.push(new Pair<>(elts.get(0).getKey(),treeRoot));
			
			for (Pair<Integer,LateXElement> elt : elts.subList(1,elts.size())) {
				TreeItem<NamedObject<LateXElement>> node = newTreeItem(elt.getValue());
				
				while (stack.peek().getKey() >= elt.getKey()) stack.pop();
				stack.peek().getValue().getChildren().add(node);
				stack.push(new Pair<>(elt.getKey(),node));
			}
		}
		
		tree.setRoot(treeRoot);
		treeRoot.setExpanded(true);
		userTextArea.setDisable(false);
		tree.getSelectionModel().select(treeRoot);
	}

	public void toPdf() throws IOException {
		if (currentFile != null) {
			generate();

			String path = currentFile.getCanonicalPath();
			ProcessBuilder pb = new ProcessBuilder("pdflatex","-halt-on-error",String.format("%s.tex",
					path.substring(0,path.lastIndexOf("."))));
			pb.directory(currentDir.getAbsoluteFile());

			StringBuilder sb = new StringBuilder();
			Function<String, Consumer<String>> consumerFactory = s -> str -> {
				sb.append(str);
				sb.append("\n");
			};

			try {
				Process       p           = pb.start();
				StreamPrinter inputStream = new StreamPrinter(p.getInputStream(),consumerFactory.apply(""));
				StreamPrinter errorStream = new StreamPrinter(p.getErrorStream(),consumerFactory.apply(""));
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
				savedState = new DocumentState(elts.stream().map(kv -> kv.getValue()).collect(Collectors.toList()));
				setSaved(true);
				generate.setDisable(false);
			}
			actionManager.reset();
		} catch (FileNotFoundException e) {
			Dialogs.create().owner(primaryStage).title(strings.getProperty("error"))
				.masthead(strings.getProperty("anErrorOccurredMessage"))
				.message(String.format(strings.getProperty("unfoundFileError"),file.getAbsolutePath()))
				.showError();
		} catch (IOException e) {
			Dialogs.create().owner(primaryStage).title(strings.getProperty("error"))
				.masthead(strings.getProperty("anErrorOccurredMessage"))
				.message(strings.getProperty("ioLoadError")).showError();
		} catch (WrongFormatException e) {
			Dialogs.create().owner(primaryStage).title(strings.getProperty("error"))
				.masthead(strings.getProperty("anErrorOccurredMessage"))
				.message(String.format(strings.getProperty("malformedJavatexError"),e.getMessage()))
				.showError();
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
	
	// controls
	private void addChild(String command, int option) {
		TreeItem<NamedObject<LateXElement>> newElt = newTreeItem(LateXElement.newLateXElement(command,""));
		TreeItem<NamedObject<LateXElement>> parent = currentNode;
		
		actionManager.perform(new CancelableAction() {
			@Override
			public void peform() {
				if (option == INSERT_TAIL) currentNode.getChildren().add(  newElt);
				else                       currentNode.getChildren().add(0,newElt);
				currentNode.setExpanded(true);
				setSaved(false);
			}
			
			@Override
			public void cancel() {
				parent.getChildren().remove(newElt);
			}
		});
	}
	
	private void addSibling(String command) {
		TreeItem<NamedObject<LateXElement>> newElt = newTreeItem(LateXElement.newLateXElement(command,""));
		TreeItem<NamedObject<LateXElement>> parent = currentNode.getParent();
		TreeItem<NamedObject<LateXElement>> node   = currentNode;
		
		actionManager.perform(new CancelableAction() {
			@Override
			public void peform() {
				int i = parent.getChildren().indexOf(node);
				if (i == parent.getChildren().size() - 1) parent.getChildren().add(newElt);
				else                                      parent.getChildren().add(i + 1,newElt);
				setSaved(false);
			}
			
			@Override
			public void cancel() {
				parent.getChildren().remove(newElt);
			}
		});
	} 
	
	private void cutNode(boolean saveToClipboard) {
		TreeItem<NamedObject<LateXElement>>	parent = currentNode.getParent();
		TreeItem<NamedObject<LateXElement>> node   = currentNode;
		int                                 index  = parent.getChildren().indexOf(node);
		if (saveToClipboard) 
			clipBoard = currentNode;
		
		actionManager.perform(new CancelableAction() {
			@Override
			public void peform() {
				
				TreeItem<NamedObject<LateXElement>> next;
				if      (parent.getChildren().size() == 1        ) next = parent;
				else if (index != parent.getChildren().size() - 1) next = parent.getChildren().get(index + 1);
				else                                               next = parent.getChildren().get(index - 1);
				parent.getChildren().remove(node);
				tree.getSelectionModel().select(next);
			}
			
			@Override
			public void cancel() {
				parent.getChildren().add(index,node);
				tree.getSelectionModel().select(node);
			}
		});
	}
	
	private void copyNode() {
		clipBoard = cloneNode(currentNode);
	}
	
	private TreeItem<NamedObject<LateXElement>> cloneNode(TreeItem<NamedObject<LateXElement>> node) {
		TreeItem<NamedObject<LateXElement>> root = newTreeItem(node.getValue().bean.clone());
		root.getChildren().addAll(node.getChildren().stream().map(n -> cloneNode(n)).collect(Collectors.toList()));
		return root;
	}
	
	private void pasteNode() {
		TreeItem<NamedObject<LateXElement>> toPaste      = cloneNode(clipBoard);
		TreeItem<NamedObject<LateXElement>> current      = currentNode;
		LateXElement                        clipboardElt = toPaste    .getValue().bean;
		LateXElement                        elt          = currentNode.getValue().bean;
		TreeItem<NamedObject<LateXElement>>	parent       = elt.getDepth() < clipboardElt.getDepth() ? currentNode : currentNode.getParent();
		
		if (elt.getDepth() <= clipboardElt.getDepth())
			actionManager.perform(new CancelableAction() {
				@Override
				public void peform() {
					if (toPaste != null) {
						if (elt.getDepth() < clipboardElt.getDepth())
							parent.getChildren().add(0,toPaste);
						else if (elt.getDepth() == clipboardElt.getDepth()) {
							int index = parent.getChildren().indexOf(current);
							if (index < parent.getChildren().size() - 1) parent.getChildren().add(index + 1,toPaste);
							else                                         parent.getChildren().add(toPaste);
						} 
						tree.getSelectionModel().select(toPaste);
					}
				}
				
				@Override
				public void cancel() {
					parent.getChildren().remove(toPaste);
				}
			});
	}
	
	/**
	 * I have been forced to create it to run it under Eclipse after some strange bug... nevermind
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}

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
		LANGUAGES.put("Java","text/x-java");
		LANGUAGES.put("C++","text/x-c++src");
		LANGUAGES.put("C","text/x-csrc");
		LANGUAGES.put("Scala","text/x-scala");
		LANGUAGES.put("LaTeX","text/x-stex");
		LANGUAGES.put("Javascript","text/javascript");
		LANGUAGES.put("Python","text/x-python");
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
	
	private class WrongFormatException extends Exception { 
		private static final long	serialVersionUID	= 1L;
		public WrongFormatException(String msg) { super(msg); }
	}
}