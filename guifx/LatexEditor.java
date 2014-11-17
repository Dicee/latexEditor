package guifx;

import java.io.*;
import java.util.*;
import java.util.List;

import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.stage.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.event.EventHandler;

import javax.swing.JOptionPane;

import latex.LateXFilter;
import latex.LateXMaker;
import latex.elements.*;
import utils.*;
import utils.FilterWriter;
import static javafx.scene.input.KeyCharacterCombination.*;

public class LatexEditor extends Application {
    
    private static final int INSERT_HEAD = 0;
    private static final int INSERT_TAIL = 1;
    
    private final Node rootIcon = 
        new ImageView(new Image(getClass().getResourceAsStream("/data/texIcon.png")));
    private File currentDir        = System.getenv("LATEX_HOME") == null ? null : new File(System.getenv("LATEX_HOME"));
    static {
    	System.out.println(System.getenv("LATEX_HOME"));
    }
    private File currentFile       = null;
    
    private ArrayList<LateXElement> lateXElements = new ArrayList<>();
    
    private boolean saved            = true;
    private DocumentState savedState = new DocumentState(new ArrayList<>());
    
    private Stage primaryStage;
    private TreeView<LateXElement> tree;
    private TreeItem<LateXElement> treeRoot;
    private final LateXMaker lm = new LateXMaker();
    private TextArea textArea;
    private HBox editZone;
    private ContextMenu addMenu;
    private MenuBar menuBar;
    private MenuItem generate;
    private Label info;

    private static final HashMap<Integer,String[]> nodesTypesMap;
    private static final HashMap<String,String> icons;
    private static final HashMap<String,String> helpers;
    
    private TreeItem<LateXElement> currentNode = null;
    private TreeItem<LateXElement> clipBoard   = null;

    @Override
    public void start(Stage primaryStage) {
        setUserAgentStylesheet(STYLESHEET_CASPIAN);
        
        VBox root = new VBox(10);
        setTree();
        setEditZone();
        setMenuBar();
        this.primaryStage = primaryStage;
        
        VBox header = setHeader();
        root.getChildren().addAll(header,editZone);
		
        Scene scene = new Scene(root, 1100, 600);
        primaryStage.setTitle("LateX Editor 3.0");
        primaryStage.setScene(scene);
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
        
        Button tex     = new Button("Générer le fichier LateX",texIcon);
        Button preview = new Button("Prévisualisation",previewIcon);
        Button pdf     = new Button("Générer le fichier pdf",pdfIcon);        
        
        tex.setOnAction(event -> generate());
        preview.setOnAction((ActionEvent event) -> {
            try {
                preview();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        pdf.setOnAction((ActionEvent t) -> {
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
    
    private TreeItem<LateXElement> newTreeItem(LateXElement l) {
        String url;
        String command = l.getName();
        Node icon = null;
        if ((url = icons.get(command)) != null) 
           icon = new ImageView(new Image(getClass().getResourceAsStream(url)));        
        TreeItem<LateXElement> newElt;
        if (icon == null)
            newElt = new TreeItem<>(LateXElement.newLateXElement(command,l.getText(),lm));
        else
            newElt = new TreeItem<>(LateXElement.newLateXElement(command,l.getText(),lm),icon);
        return newElt;
    }

    private void setTree() {
        treeRoot = new TreeItem<>(new Title("", lm),rootIcon);
        tree = new TreeView<>(treeRoot);
        tree.setMinSize(200,50);
        treeRoot.setExpanded(true); 
        currentNode = treeRoot; 
        tree.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if (t.getButton().equals(MouseButton.SECONDARY)) {
                    openContextMenu(new Point2D(t.getScreenX(), t.getScreenY()));
                }
            }

            private void openContextMenu(Point2D pt) {
                //Creation d'un popup contextuel adapte
                if (addMenu == null)
                    addMenu = new ContextMenu();
                addMenu.hide();
                addMenu.getItems().clear();
                LateXElement elt = currentNode.getValue();
                
                buildAddMenus(elt);
				//buildClipboardMenus(elt);
                buildDeleteMenu(elt);				
                
                //Affichage du popup
                addMenu.show(editZone,pt.getX() + 10, pt.getY() + 10);
            }           
        });
        
        tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tree.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends TreeItem<LateXElement>> ov, TreeItem<LateXElement> formerItem, TreeItem<LateXElement> newItem) -> {
                    if (formerItem != null && currentNode != null) {
                        String text = textArea.getText();
                        if (!currentNode.getValue().getText().equals(text))
                            setSaved(false);
                        formerItem.getValue().setText(textArea.getText());                      
                    }
                    if (newItem != null && newItem.getValue() != null) {
                        textArea.setText(newItem.getValue().getText());
                        info.setText(helpers.get(newItem.getValue().getName()));
                        currentNode = newItem;
                    }
        });
    }

	private void buildClipboardMenus(LateXElement elt) {
		if (!(elt instanceof Title)) {
			MenuItem copy  = new MenuItem("Copier");
			MenuItem cut   = new MenuItem("Couper");			
			MenuItem paste = new MenuItem("Coller");			
			
			copy.setOnAction((ActionEvent ev) -> clipBoard = currentNode);
			cut.setOnAction((ActionEvent ev) -> deleteNode(true));
			paste.setOnAction((ActionEvent ev) -> {
				if (clipBoard == null) {
					//Still need to create my own dialog class...
				} else {
					LateXElement clipboardElt = clipBoard.getValue();
					if (elt.getDepth() < clipboardElt.getDepth())
						currentNode.getChildren().add(0,clipBoard);
					else if (elt.getDepth() == clipboardElt.getDepth()) {
						TreeItem<LateXElement> parent = currentNode.getParent();
						int                    index  = parent.getChildren().indexOf(currentNode);
						if (index < parent.getChildren().size() - 1)
							parent.getChildren().add(index + 1,clipBoard);
						else
							parent.getChildren().add(clipBoard);
					}
				}
			});
			
			addMenu.getItems().add(copy);
			addMenu.getItems().add(cut);
			addMenu.getItems().add(paste);
		}
	}
	
	private void deleteNode(boolean saveInClipboard) {
        TreeItem<LateXElement> parent = currentNode.getParent();
        parent.getChildren().remove(currentNode);
		clipBoard   = saveInClipboard ? currentNode : clipBoard; 
		currentNode = null;
    }	

    private void buildDeleteMenu(LateXElement elt) {
        MenuItem delete;
        if (!(elt instanceof Title)) {
            addMenu.getItems().add(delete = new MenuItem("Supprimer"));
            delete.setOnAction((ActionEvent ev) -> deleteNode(false));
        }
    }
    
    private void buildAddMenus(LateXElement elt) {
        Menu addChildHead, addChildTail, addSibling = null;
        Map<Menu, Integer> map = null;
        
        //Determination des elements principaux du popup
        if (elt.getDepth() != LateXElement.DEPTH_MAX) {
            addMenu.getItems().add(addChildHead = new Menu("Ajouter un fils en tête"));
            addMenu.getItems().add(addChildTail = new Menu("Ajouter un fils en queue"));
            map = new HashMap() {
                {
                    put(addChildHead, INSERT_HEAD);
                    put(addChildTail, INSERT_TAIL);
                }
            };
        }

        if (elt.getDepth() != LateXElement.DEPTH_MIN) 
            addMenu.getItems().add(addSibling = new Menu("Ajouter un frère"));

        //Determination des elements secondaires du popup
        for (Integer depth : nodesTypesMap.keySet()) {
            //D'abord les elements fils
            if (map != null) {
                map.entrySet().stream().forEach((Map.Entry<Menu, Integer> entry) -> {
                    Menu addChild = entry.getKey();
                    if (depth > elt.getDepth()) {
                        if (addChild.getItems().size() != 0) {
                            addChild.getItems().add(new SeparatorMenuItem());
                        }

                        for (final String type : nodesTypesMap.get(depth)) {
                            final MenuItem item = new MenuItem(type);
                            addChild.getItems().add(item);
                            item.setOnAction((ActionEvent t) -> {
                                addChild(type, entry.getValue());
                            });
                        }
                    }
                });
            }
            //Ensuite les elements freres
            if (addSibling != null && depth == elt.getDepth()) {
                for (final String type : nodesTypesMap.get(depth)) {
                    final MenuItem item = new MenuItem(type);
                    addSibling.getItems().add(item);
                    item.setOnAction(event -> addSibling(type));
                }
            }
        }
    }
    
    private void addSibling(String command) {
        TreeItem<LateXElement> newElt = newTreeItem(LateXElement.newLateXElement(command,"",lm));
        ObservableList<TreeItem<LateXElement>> childrens = currentNode.getParent().getChildren();
        int i = childrens.indexOf(currentNode); 
        if (i == childrens.size() - 1)
            childrens.add(newElt);
        else         
            childrens.add(i + 1,newElt);
        setSaved(false);
    }

    private void addChild(String command, int option) {
        TreeItem<LateXElement> newElt = newTreeItem(LateXElement.newLateXElement(command,"",lm));
        if (option == INSERT_TAIL)
            currentNode.getChildren().add(newElt);
        else
            currentNode.getChildren().add(0,newElt);
        currentNode.setExpanded(true);
        setSaved(false);
    }
    
    private void setEditZone() {
        info      = new Label("Saisissez votre texte ici");
        textArea  = new TextArea();
		textArea.setPrefSize(600, 500);		
        VBox vbox = new VBox(); 
        
        vbox.setPadding(new Insets(5));
        vbox.setSpacing(5);
		vbox.getChildren().addAll(info,textArea);
        
        String[] operators = { "\\cdot","+","-","\\frac{}{}","\\sqrt[]{}",
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
        IconSelectionView operateurs = new IconSelectionView(img,6,5,operators,"Opérateurs");
        operateurs.setActionListener((java.awt.event.ActionEvent e) -> {
            textArea.cut();
            textArea.insertText(textArea.getCaretPosition(),e.getActionCommand());
        });
        img = new Image(LatexEditor.class.getResourceAsStream("/data/AlphabetGrec.png"));
        IconSelectionView alphabetGrec = new IconSelectionView(img,5,5,ctes,"Alphabet grec");
        alphabetGrec.setActionListener((java.awt.event.ActionEvent e) -> {
            textArea.cut();
            textArea.insertText(textArea.getCaretPosition(),e.getActionCommand());
        });
        IconSelectionBox box = new IconSelectionBox();
        box.addSelectionView(operateurs);
        box.addSelectionView(alphabetGrec);
                
        editZone = new HBox(10);       
        editZone.getChildren().addAll(box,tree,vbox);
        editZone.setLayoutX(20);
        editZone.setPadding(new Insets(15));      
    }

    private void setMenuBar() {
        menuBar          = new MenuBar();
        Menu menuFile    = new Menu("Fichier");
        Menu menuEdit    = new Menu("Editer");
        Menu menuOptions = new Menu("Options");
        
        MenuItem newDoc  = new MenuItem("Nouveau document"     );
        MenuItem save    = new MenuItem("Enregistrer"          );
        MenuItem saveAs  = new MenuItem("Enregistrer sous"     );
        MenuItem load    = new MenuItem("Charger"              );
        MenuItem quit    = new MenuItem("Quitter"              );
        generate         = new MenuItem("Générer le code LateX");
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
        
        Menu chooseStyle = new Menu("Apparence");
        MenuItem modena  = new MenuItem("Modena");        
        MenuItem caspian = new MenuItem("Caspian");
        
        modena.setOnAction((ActionEvent ev) -> setUserAgentStylesheet(STYLESHEET_MODENA));
        caspian.setOnAction((ActionEvent ev) -> setUserAgentStylesheet(STYLESHEET_CASPIAN));
        
        chooseStyle.getItems().addAll(modena,caspian);
        menuOptions.getItems().add(chooseStyle);
    }
    
    private void save() {
        try {
            currentNode.getValue().setText(textArea.getText());
            if (currentFile != null) {
                File f                             = new File(currentFile.getAbsolutePath());
                FilterWriter fw                    = new FilterWriter(new BufferedWriter(new FileWriter(f)), new LateXFilter());
                ListeNommee<LateXElement> elements = getElements();
                Iterator<String> names             = elements.getA().iterator();
                lateXElements                      = new ArrayList<>(elements.getB());
                ArrayList<LateXElement> state      = new ArrayList<>();
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
            JOptionPane.showMessageDialog(null, "Erreur lors de la sauvegarde !",
                    "Impossible de sauvegarder", JOptionPane.ERROR_MESSAGE);
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
            lm.save(new File(path), savedState.getCurrentState());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
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
			
            primaryStage.setTitle(currentFile.getName() + " - LateXEditor 3.0");
            generate.setDisable(false); 
        }       
    }
    
    private ListeNommee<LateXElement> getElements(TreeItem<LateXElement> node, String level) {
        List<LateXElement> elements = new LinkedList<>();
        List<String>       names    = new LinkedList<>();
        elements.add(node.getValue());
        names.add(level);
        if (!node.isLeaf()) {
            for (TreeItem<LateXElement> elt : node.getChildren()) {
                ListeNommee<LateXElement> childResult = getElements(elt, level + ">");
                names.addAll(childResult.getA());
                elements.addAll(childResult.getB());
            }
        }
        return new ListeNommee<>(names, elements);
    }
    
    public ListeNommee<LateXElement> getElements() {
    	return getElements(treeRoot,"");
    }
    
    private TreeItem<LateXElement> setElements(ListeNommee<LateXElement> elts,
            TreeItem<LateXElement> parentNode, int index, int max) {
        List<String> names         = elts.getA();
        List<LateXElement> objects = elts.getB();
        int level = names.get(index).length();

        for (int i = index + 1; i < max && level < names.get(i).length(); i++) {
            int currentLevel = names.get(i).length();
            if (currentLevel == level + 1) {
                TreeItem<LateXElement> childNode = newTreeItem(objects.get(i));
                tree.getSelectionModel().select(childNode);
                childNode = setElements(elts, childNode,i,max);
                parentNode.getChildren().add(childNode);
            }
        }
		parentNode.setExpanded(false);
        return parentNode;
    }
    
    public void setElements(ListeNommee<LateXElement> elts) {
        treeRoot.getChildren().clear();
        tree.getSelectionModel().select(treeRoot);
        treeRoot.setValue(elts.getB().get(0));
        textArea.setText(currentNode.getValue().getText());
        setElements(elts,treeRoot,0,elts.getA().size());
        tree.getSelectionModel().select(treeRoot);
		treeRoot.setExpanded(false);
        textArea.setDisable(false);
    }
    
    public void toPdf() throws IOException {
        if (currentFile != null) {
            generate();
            String nom = currentFile.getCanonicalPath();
            String path, script, separator;
            
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                separator = "\\";
                script    = "toPdf.bat";                
            } else {
                separator = "/";
                script    = "toPdf.sh";
            }
            
            path = nom.substring(0,nom.lastIndexOf(separator));
            nom  = nom.substring(0,nom.indexOf("."));
            
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path + separator + script)))) {
                bw.write("\npdflatex \"" + nom + ".tex\"");
                bw.flush();
                bw.close();
                Runtime.getRuntime().exec(path + separator + script/* + " > " + path + separator + "toPdf.log"*/);
            }
        }
    }
    
    public void preview() throws IOException {
        if (currentFile != null) {
            generate();
            String nom  = currentFile.getCanonicalPath();            
            String path, script, separator;
            
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                separator = "\\";
                script    = "preview.bat";                
            } else {
                separator = "/";
                script    = "preview.sh";
            }
            
            path = nom.substring(0,nom.lastIndexOf(separator));
            nom  = nom.substring(0,nom.indexOf("."));
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path + separator + script)))) {
                bw.write("\nlatex \"" + nom + ".tex\"");
                bw.write("\nyap \"" + nom + ".dvi\"");
                bw.flush();
                bw.close();
                Runtime.getRuntime().exec(path + separator + script);
            }
        }
    }
    
    public void load() {
        try {
            FileChooser f = new FileChooser();
            f.setTitle("Charger un document");
            f.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Fichier JavateX","*.javatex"));

            if (currentDir != null) 
                f.setInitialDirectory(currentDir);

            File file = f.showOpenDialog(primaryStage);
            if (file != null) {
                currentDir = file.getParentFile();
                String s = file.getPath();
                int i = s.indexOf(".");
                if (i == -1 || !s.substring(i).equals(".javatex")) 
                    file = new File(s + ".javatex");
				
                currentFile = file;

                TokenReader tr              = new TokenReader(new FileReader(file),"##");
                List<String> buffer         = new ArrayList<>();
                List<LateXElement> elements = new ArrayList<>();
                List<String> names          = new ArrayList<>();

                while ((s = tr.readToNextToken()) != null) 
                    buffer.add(s.trim());
                
                Iterator<String> it = buffer.iterator();
                while (it.hasNext()) {
                    //Si le fichier est bien forme on devrait toujours pouvoir faire deux next() de suite
                    //des lors qu'on peut en faire un !
                    String declaration = it.next();
                    String content     = it.next();
                    i                  = declaration.lastIndexOf('>');
                    String name = i == -1 ? "" : declaration.substring(0, i + 1);
                    String type = i == -1 ? declaration : declaration.substring(i + 1).trim();

                    names.add(name);
                    elements.add(LateXElement.newLateXElement(type,content,lm));
                }
                
                //Si apres tout cela il n'y a aucune erreur, on peut enfin ecraser les precedents buffers
                tr.close();
                setElements(new ListeNommee<>(names, elements));
                primaryStage.setTitle(currentFile.getName() + " - LateXEditor 3.0");
                setSaved(true);
				savedState = new DocumentState(elements);
                generate.setDisable(false);
            }
        }catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Fichier introuvable.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Une erreur est survenue lors du chargement du fichier.\nVeuillez "
                    + "vérifier que sa syntaxe est conforme.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
     }
    
    public static void main(String[] args) {
        launch(args);
    }

    static {
        nodesTypesMap = new HashMap<>();
        nodesTypesMap.put(0, new String[]{"Titre"});
        nodesTypesMap.put(1, new String[]{"Chapitre"});
        nodesTypesMap.put(2, new String[]{"Section"});
        nodesTypesMap.put(3, new String[]{"Sous-section"});
        nodesTypesMap.put(4, new String[]{"Sous-sous section"});
        nodesTypesMap.put(5, new String[]{"Paragraphe", "Liste", "Inclusion d'image", "Code", "Code LateX"});
    }
    
    static {
        helpers = new HashMap<>();
        helpers.put("Titre", "Saisissez ici le titre du document et le nom de l'auteur "
                + "séparés par un ;");
        helpers.put("Chapitre", "Saisissez le titre du chapitre ici");
        helpers.put("Section", "Saisissez le titre de la section ici");
        helpers.put("Sous-section", "Saisissez le titre de la sous-section ici");
        helpers.put("Sous-sous section", "Saisissez le titre de la sous-sous section ici");
        helpers.put("Paragraphe", "Saisissez le contenu du paragraphe ici");
        helpers.put("Liste", "Saisissez ici les items de la liste séparés par des ;");
        helpers.put("Inclusion d'image", "Saisissez ici l'URL, la légende de la figure et son"
                + " rapport d'échelle séparés par des ;");
        helpers.put("Code", "Saisissez le langage utilisé suivi du code, séparé par un saut de ligne");
        helpers.put("Code LateX", "Saisissez votre code ici");
    }
    
    static {
        icons = new HashMap<>();
        icons.put("Titre","/data/texIcon.png");
        icons.put("Chapitre","/data/chapterIcon.png");
        icons.put("Section","/data/sectionIcon.png");
        icons.put("Sous-section","/data/subSectionIcon.png");
        icons.put("Sous-sous section","/data/subsubSectionIcon.png");
        for (String name : nodesTypesMap.get(5))
            icons.put(name,"/data/leafIcon.png");
    }
}

