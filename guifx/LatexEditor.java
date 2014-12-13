package guifx;

import static guifx.utils.Settings.properties;
import static guifx.utils.Settings.strings;
import static java.util.Arrays.asList;
import static javafx.scene.input.KeyCombination.ALT_DOWN;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Pair;

import javax.swing.JOptionPane;

import latex.LateXFilter;
import latex.LateXMaker;
import latex.elements.LateXElement;
import latex.elements.Title;

import org.controlsfx.dialog.Dialogs;

import scala.collection.mutable.StringBuilder;
import scala.io.Codec;
import scala.io.Source;
import utils.FilterWriter;
import utils.StreamPrinter;
import utils.TokenReader;

public class LatexEditor extends Application {
	private static final HashMap<Integer,List<String>>	nodesTypesMap;
	
	private static final int						INSERT_HEAD		= 0;
	private static final int						INSERT_TAIL		= 1;

	public static final Font						subtitlesFont	= Font.font(null,FontWeight.BOLD,13);

	private final Node								rootIcon		= new ImageView(new Image(getClass().getResourceAsStream("/data/texIcon.png")));
	private File									currentDir		= System.getenv("LATEX_HOME") == null ? null : new File(System.getenv("LATEX_HOME"));
	private File									currentFile		= null;

	private boolean									saved			= true;
	private DocumentState							savedState		= new DocumentState(new ArrayList<>());

	private List<LateXElement>						lateXElements	= new ArrayList<>();
	private final LateXMaker						lm				= new LateXMaker();
	
	private Stage									primaryStage;
	private TreeView<NamedObject<LateXElement>>		tree;
	private TreeItem<NamedObject<LateXElement>>		treeRoot;
	private TreeItem<NamedObject<LateXElement>>		currentNode		= null;
	private TreeItem<NamedObject<LateXElement>>		clipBoard  		= null;
	
	
	private ContextMenu								addMenu			= new ContextMenu();
	private MenuBar									menuBar;
	
	private TextArea								userTextArea;
	private TextArea								outputTextArea;
	private CodeEditor								outputCode;
	private MenuItem								generate;
	private Label									info;

    @Override
    public void start(Stage primaryStage) {
    	Settings.init();
    	
        VBox root = new VBox(10);
        setTree();
        HBox editZone = setEditZone();
        setMenuBar();
        this.primaryStage = primaryStage;
        
        VBox header = setHeader();
        root.getChildren().addAll(header,editZone);
		
        Scene scene   = new Scene(root,0,0);
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

    private VBox setHeader() {
        VBox header           = new VBox();
        ImageView pdfIcon     = new ImageView(
                new Image(LatexEditor.class.getResourceAsStream("/data/pdfIcon.png")));
        ImageView texIcon     = new ImageView(
                new Image(LatexEditor.class.getResourceAsStream("/data/texIcon.png")));
        ImageView previewIcon = new ImageView(
                new Image(LatexEditor.class.getResourceAsStream("/data/previewIcon.png")));
        
        Button tex     = new Button("",texIcon);
        Button preview = new Button("",previewIcon);
        Button pdf     = new Button("",pdfIcon);  
        tex    .textProperty().bind(strings.getObservableProperty("generateLatex"));
        preview.textProperty().bind(strings.getObservableProperty("preview"));
        pdf    .textProperty().bind(strings.getObservableProperty("generatePdf"));
        
        tex.setOnAction(event -> generate());
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
        
        l = LateXElement.newLateXElement(command,l.getText(),lm);
        NamedObject<LateXElement> no = new NamedObject<LateXElement>(strings.getObservableProperty(command),l);
        return icon == null ? new TreeItem<>(no) : new TreeItem<>(no,icon);
    }

    private void setTree() {
        treeRoot = new TreeItem<>(new NamedObject<>(strings.getObservableProperty("title"),new Title("",lm)),rootIcon);
        tree     = new TreeView<>(treeRoot);
        tree.setMinSize(200,50);
        treeRoot.setExpanded(true); 
        currentNode = treeRoot; 
        tree.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if (t.getButton().equals(MouseButton.SECONDARY)) 
                    openContextMenu(new Point2D(t.getScreenX(), t.getScreenY()));
                else 
                	addMenu.hide();
            }

            private void openContextMenu(Point2D pt) {
                // creation of the relevant contextual popup
                addMenu.hide();
                addMenu.getItems().clear();
                LateXElement elt = currentNode.getValue().bean;
                
                buildAddMenus(elt);
//				buildClipboardMenus(elt);
                buildDeleteMenu(elt);				
                
                // display the popup
                addMenu.show(tree,pt.getX() + 10, pt.getY() + 10);
            }           
        });
        
        tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tree.getSelectionModel().selectedItemProperty().addListener(
        		(ObservableValue<? extends TreeItem<NamedObject<LateXElement>>> ov, 
        		TreeItem<NamedObject<LateXElement>> formerItem, 
        		TreeItem<NamedObject<LateXElement>> newItem) -> {
                    if (formerItem != null && currentNode != null) {
                        String text = userTextArea.getText();
                        if (!currentNode.getValue().bean.getText().equals(text))
                            setSaved(false);
                        formerItem.getValue().bean.setText(userTextArea.getText());                      
                    }
                    if (newItem != null && newItem.getValue() != null) {
                        userTextArea.setText(newItem.getValue().bean.getText());
                        info.textProperty().bind(strings.getObservableProperty(newItem.getValue().bean.getType() + "Tip"));
                        currentNode = newItem;
                    }
        });
        
//		tree.setCellFactory(new Callback<TreeView<NamedObject<LateXElement>>, TreeCell<NamedObject<LateXElement>>>() {
//			public TreeCell<NamedObject<LateXElement>> call(TreeView<NamedObject<LateXElement>> param) {
//				final TreeCell<NamedObject<LateXElement>> cell = new TreeCell<NamedObject<LateXElement>>() {
//					@Override
//					public void updateItem(NamedObject<LateXElement> item, boolean empty) {
//						super.updateItem(item,empty);
//						if (!empty && item != null) {
//							textProperty().bind(item.nameProperty());
//							String url;
//							if ((url = icons.get(item.bean.getType())) != null) {
//								ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(url))); 
//								if (icon != null) {
//									setGraphic(icon);
//									System.out.println(getUserData());
//								}
//							}
//						} else {
//							textProperty().unbind();
//							setText("");
//						}
//					}
//				};
//				return cell;
//			}
//		});
    }

//	private void buildClipboardMenus(LateXElement elt) {
//		if (!(elt instanceof Title)) {
//			MenuItem copy  = new MenuItem("Copier");
//			MenuItem cut   = new MenuItem("Couper");			
//			MenuItem paste = new MenuItem("Coller");			
//			
//			copy.setOnAction((ActionEvent ev) -> clipBoard = currentNode);
//			cut.setOnAction((ActionEvent ev) -> deleteNode(true));
//			paste.setOnAction((ActionEvent ev) -> {
//				if (clipBoard == null) {
//					//Still need to create my own dialog class...
//				} else {
//					LateXElement clipboardElt = clipBoard.getValue();
//					if (elt.getDepth() < clipboardElt.getDepth())
//						currentNode.getChildren().add(0,clipBoard);
//					else if (elt.getDepth() == clipboardElt.getDepth()) {
//						TreeItem<LateXElement> parent = currentNode.getParent();
//						int                    index  = parent.getChildren().indexOf(currentNode);
//						if (index < parent.getChildren().size() - 1)
//							parent.getChildren().add(index + 1,clipBoard);
//						else
//							parent.getChildren().add(clipBoard);
//					}
//				}
//			});
//			
//			addMenu.getItems().add(copy);
//			addMenu.getItems().add(cut);
//			addMenu.getItems().add(paste);
//		}
//	}
	
	private void deleteNode(boolean saveInClipboard) {
        TreeItem<NamedObject<LateXElement>> parent = currentNode.getParent();
        parent.getChildren().remove(currentNode);
		clipBoard   = saveInClipboard ? currentNode : clipBoard; 
		currentNode = null;
    }	

    private void buildDeleteMenu(LateXElement elt) {
        MenuItem delete;
        if (!(elt instanceof Title)) {
            addMenu.getItems().add(delete = new MenuItem());
            delete.textProperty().bind(strings.getObservableProperty("delete"));
            delete.setOnAction((ActionEvent ev) -> deleteNode(false));
        }
    }
    
    private void buildAddMenus(LateXElement elt) {
        Menu addChildHead, addChildTail, addSibling = null;
        Map<Menu, Integer> map = null;
        
        // determine the main elements of the popup
        if (elt.getDepth() != LateXElement.DEPTH_MAX) {
            addMenu.getItems().add(addChildHead = new Menu());
            addMenu.getItems().add(addChildTail = new Menu());
            map = new HashMap<>(); 
            map.put(addChildHead, INSERT_HEAD);
            map.put(addChildTail, INSERT_TAIL);
            
            addChildHead.textProperty().bind(strings.getObservableProperty("addChildHead"));
            addChildTail.textProperty().bind(strings.getObservableProperty("addChildTail"));
        }

        if (elt.getDepth() != LateXElement.DEPTH_MIN) {
            addMenu.getItems().add(addSibling = new Menu());
            addSibling.textProperty().bind(strings.getObservableProperty("addSibling"));
        }

        // determine the secondary elements of the popup
        for (Integer depth : nodesTypesMap.keySet()) {
            // first, the children elements
            if (map != null) {
                map.entrySet().stream().forEach((Map.Entry<Menu, Integer> entry) -> {
                    Menu addChild = entry.getKey();
                    if (depth > elt.getDepth()) {
                        if (addChild.getItems().size() != 0) 
                            addChild.getItems().add(new SeparatorMenuItem());
                        
                        for (String type : nodesTypesMap.get(depth)) {
                            MenuItem item = new MenuItem();
                            item.textProperty().bind(strings.getObservableProperty(type));
                            addChild.getItems().add(item);
                            item.setOnAction((ActionEvent t) -> {
                                addChild(type, entry.getValue());
                                addMenu.hide();
                            });
                        }
                    }
                });
            }
            
            // then, the sibling elements
            if (addSibling != null && depth == elt.getDepth()) {
                for (String type : nodesTypesMap.get(depth)) {
                    MenuItem item = new MenuItem();
                    item.textProperty().bind(strings.getObservableProperty(type));
                    addSibling.getItems().add(item);
                    item.setOnAction(event -> addSibling(type));
                }
            }
        }
    }
    
    private void addSibling(String command) {
        TreeItem<NamedObject<LateXElement>> newElt = newTreeItem(LateXElement.newLateXElement(command,"",lm));
        ObservableList<TreeItem<NamedObject<LateXElement>>> children = currentNode.getParent().getChildren();
        int i = children.indexOf(currentNode); 
        if (i == children.size() - 1)
            children.add(newElt);
        else         
            children.add(i + 1,newElt);
        setSaved(false);
    }

    private void addChild(String command, int option) {
        TreeItem<NamedObject<LateXElement>> newElt = newTreeItem(LateXElement.newLateXElement(command,"",lm));
        if (option == INSERT_TAIL)
            currentNode.getChildren().add(newElt);
        else
            currentNode.getChildren().add(0,newElt);
        currentNode.setExpanded(true);
        setSaved(false);
    }
    
    private HBox setEditZone() {
        info      = new Label();
        userTextArea  = new TextArea();
        userTextArea.setPrefSize(600,550);
        info.textProperty().bind(strings.getObservableProperty("editZoneTip"));
        info.setFont(subtitlesFont);
        
        VBox editor = new VBox(info,userTextArea); 
        editor.setPadding(new Insets(5));
        editor.setSpacing(5);
        
        String[] ops = { "\\cdot","+","-","\\frac{}{}","\\sqrt[]{}",
			"\\forall","\\partial","\\exists","\\nexists","\\varnothing",
		 	"\\bigcap","\\bigcup","\\bigint","\\prod","\\sum",
		 	"\\nabla","\\in","\\notin","\\ni","",
		 	"^{}","_{}","\\leq","\\geq","\\neq",
			"\\mid\\mid.\\mid\\mid"
		};
        
        String[] ctes = { "\\alpha","\\beta","\\gamma","\\delta","\\epsilon","\\mu","\\nu","\\xi","\\pi","\\rho",
        	"\\omega","\\Omega","\\theta","\\Delta","\\Psi","\\eta","\\lambda","\\sigma","\\tau",
			"\\chi","\\phi","\\infty"
        };
        
        Image img = new Image(LatexEditor.class.getResourceAsStream("/data/Operateurs.png"));
        IconSelectionView operators = new IconSelectionView(img,6,5,ops,strings.getObservableProperty("operators"));
        operators.setActionListener((java.awt.event.ActionEvent e) -> {
            userTextArea.cut();
            userTextArea.insertText(userTextArea.getCaretPosition(),e.getActionCommand());
        });
        
        img = new Image(LatexEditor.class.getResourceAsStream("/data/AlphabetGrec.png"));
        IconSelectionView greekAlphabet = new IconSelectionView(img,5,5,ctes,strings.getObservableProperty("greekAlphabet"));
        greekAlphabet.setActionListener((java.awt.event.ActionEvent e) -> {
            userTextArea.cut();
            userTextArea.insertText(userTextArea.getCaretPosition(),e.getActionCommand());
        });
        
        IconSelectionBox box = new IconSelectionBox();
        box.addSelectionView(operators);
        box.addSelectionView(greekAlphabet);
        
        outputCode            = new CodeEditor("");
        TitledPane codeEditor = new TitledPane("",outputCode);
        codeEditor.textProperty().bind(strings.getObservableProperty("outputCodeTitle"));
        outputCode.setMinHeight(500);
        
        outputTextArea        = new TextArea();
        TitledPane outputMsg  = new TitledPane("",outputTextArea);
        outputMsg.textProperty().bind(strings.getObservableProperty("outputMsgTitle"));
        outputTextArea.setMinHeight(800);
        outputTextArea.setEditable(false);
        
        Accordion accordion   = new Accordion();
        accordion.getPanes().addAll(codeEditor,outputMsg);
        accordion.setExpandedPane(codeEditor);
        
        HBox editZone = new HBox(10,box,tree,editor,setOutputZone(accordion));       
        editZone.setLayoutX(20);
        editZone.setPadding(new Insets(15));
        
        // handle resize events
        HBox.setHgrow(editor,Priority.ALWAYS);
        HBox.setHgrow(accordion,Priority.SOMETIMES);
        HBox.setHgrow(tree,Priority.NEVER);
        tree.setMinWidth(210);
        userTextArea.setMinWidth(450);
        accordion.setMinWidth(200);
        
        return editZone;
    }

    private VBox setOutputZone(Node node) {
    	Label outputLabel = new Label();
        VBox outputZone   = new VBox(outputLabel,node);
        outputLabel.textProperty().bind(strings.getObservableProperty("outputLabel"));
        outputLabel.setFont(subtitlesFont);
        outputZone.setPadding(new Insets(5));
        outputZone.setSpacing(5);
        return outputZone;
    }
    
    private void setMenuBar() {
    	// set main menu bar
        menuBar          = new MenuBar();
        Menu menuFile    = new Menu();
        Menu menuEdit    = new Menu();
        Menu menuOptions = new Menu();
        
        // set submenu File
        MenuItem newDoc  = new MenuItem();
        MenuItem save    = new MenuItem();
        MenuItem saveAs  = new MenuItem();
        MenuItem load    = new MenuItem();
        MenuItem quit    = new MenuItem();
        generate         = new MenuItem();
        menuFile.getItems().addAll(newDoc,load,save,saveAs,generate,quit);
        
        newDoc  .setAccelerator(new KeyCharacterCombination("N",CONTROL_DOWN         ));
        save    .setAccelerator(new KeyCharacterCombination("S",CONTROL_DOWN         ));
        saveAs  .setAccelerator(new KeyCharacterCombination("S",CONTROL_DOWN,ALT_DOWN));
        load    .setAccelerator(new KeyCharacterCombination("L",CONTROL_DOWN         ));
        generate.setAccelerator(new KeyCharacterCombination("G",CONTROL_DOWN         ));
        quit    .setAccelerator(new KeyCharacterCombination("Q",CONTROL_DOWN         ));
        generate.setDisable(true);
        
        newDoc  .setOnAction(ev -> { createDocument(); lateXElements = new ArrayList<>(); });
        save    .setOnAction(ev -> save());
        saveAs  .setOnAction(ev -> { createDocument(); save(); });
        load    .setOnAction(ev -> load());
        generate.setOnAction(ev -> generate());
        quit    .setOnAction(ev -> System.exit(0));
        
        menuBar.getMenus().addAll(menuFile,menuEdit,menuOptions);
        
        // set submenu Options
        MenuItem packages    = new MenuItem();
        
        // bind the text properties
        List<String>   properties = Arrays.asList("file","edit","options","newDocument","save","saveAs","load","generate","quit","packages");
        List<MenuItem> menus      = Arrays.asList(menuFile,menuEdit,menuOptions,newDoc,save,saveAs,load,generate,quit,packages);
        for (int i=0 ; i<menus.size() ; i++)
        	menus.get(i).textProperty().bind(strings.getObservableProperty(properties.get(i)));
        
        packages.setOnAction(ev -> new PreferencesPane(lm.getParameters()));
        
		ImageView checkedIcon = new ImageView(new Image(getClass().getResourceAsStream(Settings.properties.getProperty("checkedIcon"))));
		menuOptions.getItems().addAll(packages,Settings.getChooseLanguageMenu(checkedIcon),Settings.getChooseStyleMenu(checkedIcon));
    }
    
    private void save() {
        try {
            currentNode.getValue().bean.setText(userTextArea.getText());
            if (currentFile != null) {
                File f                             = new File(currentFile.getAbsolutePath());
                FilterWriter fw                    = new FilterWriter(new BufferedWriter(new FileWriter(f)), new LateXFilter());
                NamedList<LateXElement> elements = getElements();
                Iterator<String> names             = elements.getKey().iterator();
                lateXElements                      = new ArrayList<>(elements.getValue());
                List<LateXElement> state     	   = new ArrayList<>();
                
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
        	Dialogs.create().owner(primaryStage)
				.title(strings.getProperty("error"))
				.masthead(strings.getProperty("anErrorOccurredMessage"))
				.message(String.format(strings.getProperty("ioSaveError")))
				.showError();
        }
    }
   
    public void setSaved(boolean b) {
        if (!saved && b) 
            primaryStage.setTitle(primaryStage.getTitle().substring(1));
         else if (saved && !b) 
            primaryStage.setTitle("*" + primaryStage.getTitle());
        saved = b;
    }
    
    private void generate() {
        try {
            if (savedState.getCurrentState().isEmpty())
                save();
            String path = currentFile.getAbsolutePath();
            int i       = path.lastIndexOf(".");
            path        = i == -1 ? path + ".tex" : path.substring(0,i) + ".tex"; 
            lm.makeDocument(new File(path),savedState.getCurrentState());
            outputCode.setCode(Source.fromFile(new File(path),Codec.UTF8()).mkString());
        } catch (Exception e) {
            Dialogs.create().owner(primaryStage)
            	.title(strings.getProperty("error"))
            	.masthead(strings.getProperty("anErrorOccurredMessage"))
            	.message(strings.getProperty("unfoundFileErrorMessage"))
            	.showError();
            e.printStackTrace();
//            Dialogs.create().owner(primaryStage).
//            	title(strings.getProperty("error")).
//            	masthead(strings.getProperty("anErrorOccurredMessage")).
//            	showException(e);
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
            save();
			
            primaryStage.setTitle(currentFile.getName() + " - LateXEditor 4.0");
            generate.setDisable(false); 
        }       
    }
    
    private class NamedList<E> extends Pair<List<String>,List<E>> {
		private static final long	serialVersionUID	= 1L;

		public NamedList(List<String> a, List<E> b) {
    		super(a,b);
    	}
    }
    
    private NamedList<LateXElement> getElements(TreeItem<NamedObject<LateXElement>> node, String level) {
        List<LateXElement> elements = new LinkedList<>();
        List<String>       names    = new LinkedList<>();
        elements.add(node.getValue().bean);
        names.add(level);
        if (!node.isLeaf()) {
            for (TreeItem<NamedObject<LateXElement>> elt : node.getChildren()) {
                NamedList<LateXElement> childResult = getElements(elt, level + ">");
                names.addAll(childResult.getKey());
                elements.addAll(childResult.getValue());
            }
        }
        return new NamedList<>(names,elements);
    }
    
    public NamedList<LateXElement> getElements() {
    	return getElements(treeRoot,"");
    }
    
    private TreeItem<NamedObject<LateXElement>> setElements(NamedList<LateXElement> elts,
            TreeItem<NamedObject<LateXElement>> parentNode, int index, int max) {
        List<String> names         = elts.getKey();
        List<LateXElement> objects = elts.getValue();
        int level = names.get(index).length();

        for (int i = index + 1; i < max && level < names.get(i).length(); i++) {
            int currentLevel = names.get(i).length();
            if (currentLevel == level + 1) {
                TreeItem<NamedObject<LateXElement>> childNode = newTreeItem(objects.get(i));
                tree.getSelectionModel().select(childNode);
                childNode = setElements(elts,childNode,i,max);
                parentNode.getChildren().add(childNode);
            }
        }
		parentNode.setExpanded(false);
        return parentNode;
    }
    
    public void setElements(NamedList<LateXElement> elts) {
        treeRoot.getChildren().clear();
        tree.getSelectionModel().select(treeRoot);
        
        LateXElement              root = elts.getValue().get(0);
        NamedObject<LateXElement> no   = new NamedObject<LateXElement>(strings.getObservableProperty(root.getType()),root);
        treeRoot.setValue(no);
        userTextArea.setText(currentNode.getValue().bean.getText());
        
        setElements(elts,treeRoot,0,elts.getKey().size());
        tree.getSelectionModel().select(treeRoot);
		treeRoot.setExpanded(false);
        userTextArea.setDisable(false);
    }
    
    public void toPdf() throws IOException {
        if (currentFile != null) {
            generate();

            String         path = currentFile.getCanonicalPath();
            ProcessBuilder pb   = new ProcessBuilder("pdflatex", "-halt-on-error",
            	String.format("%s.tex",path.substring(0,path.lastIndexOf("."))));
            pb.directory(currentDir.getAbsoluteFile());
            
            StringBuilder sb = new StringBuilder();
//            final Object  o  = new Object();
            
            Function<String,Consumer<String>> consumerFactory = s ->
            	str -> {
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
            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public void preview() throws IOException {
		if (currentFile != null) {
			generate();

			String         path = currentFile.getCanonicalPath();
			ProcessBuilder pb   = new ProcessBuilder("latex","-halt-on-error",String.format("%s.tex",
					path.substring(0,path.lastIndexOf("."))));
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
    
    public void load() {
    	FileChooser f = new FileChooser();
    	f.setTitle("Charger un document");
    	f.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Fichier JavateX","*.javatex"));
    	
    	if (currentDir != null) 
    		f.setInitialDirectory(currentDir);
    	
    	File file = f.showOpenDialog(primaryStage);
    	
        try {
            if (file != null) {
                currentDir = file.getParentFile();
                currentFile = file;

                TokenReader        tr       = new TokenReader(new FileReader(file),"##");
                List<String>       buffer   = new LinkedList<>();
                List<LateXElement> elements = new ArrayList<>();
                List<String>       names    = new ArrayList<>();
                lm.getParameters().clear();

                String s;
                while ((s = tr.readToNextToken()) != null) 
                    buffer.add(s.trim());
                
                Iterator<String> it = buffer.iterator();
                while (it.hasNext()) {
                    String declaration = it.next();
                    String content     = it.next();
                    
                    switch (declaration) {
                    	case "packages" :
                    		lm.getParameters().addPackages(content.split("[;\\s+]|;\\s+"));
                    		break;
                    	case "commands" :
                    		lm.getParameters().include(content.split("[;\\s+]|;\\s+"));
                    		break;
                    	default         :
                    		int i       = declaration.lastIndexOf('>');
                    		String name = i == -1 ? "" : declaration.substring(0, i + 1);
                    		String type = i == -1 ? declaration : declaration.substring(i + 1).trim();
                    		
                    		names.add(name);
                    		elements.add(LateXElement.newLateXElement(type,content,lm));
                    }
                }
                
                tr.close();
                setElements(new NamedList<>(names, elements));
                primaryStage.setTitle(currentFile.getName() + " - LateXEditor 3.0");
                setSaved(true);
				savedState = new DocumentState(elements);
                generate.setDisable(false);
            }
        } catch (FileNotFoundException e) {
            Dialogs.create().owner(primaryStage)
        		.title(strings.getProperty("error"))
        		.masthead(strings.getProperty("anErrorOccurredMessage"))
        		.message(String.format(strings.getProperty("unfoundFileError"),file.getAbsolutePath()))
        		.showError();
        }catch (IOException e) {
        	Dialogs.create().owner(primaryStage)
    			.title(strings.getProperty("error"))
    			.masthead(strings.getProperty("anErrorOccurredMessage"))
    			.message(strings.getProperty("ioLoadError"))
    			.showError();
        }
     }
    
    public static void main(String[] args) {
        launch(args);
    }

    static {
        nodesTypesMap = new HashMap<>();
        nodesTypesMap.put(0,asList("title"));
        nodesTypesMap.put(1,asList("chapter"));
        nodesTypesMap.put(2,asList("section"));
        nodesTypesMap.put(3,asList("subsection"));
        nodesTypesMap.put(4,asList("subsubsection"));
        nodesTypesMap.put(5,asList("paragraph","list","image","code","latex"));
    }
}