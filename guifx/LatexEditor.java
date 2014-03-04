package guifx;

import java.io.*;
import java.util.*;

import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.stage.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

import latex.LateXFilter;
import latex.LateXMaker;
import latex.elements.*;
import latex.elements.List;
import utils.*;
import utils.FilterWriter;

/**
 *
 * @author David
 */
public class LatexEditor extends Application {
    
    private final Node rootIcon = 
        new ImageView(new Image(getClass().getResourceAsStream("/data/texIcon.png")));
    private File currentDir        = null;
    private File currentFile       = null;
    
    private ArrayList<LateXElement> lateXElements = new ArrayList<>();
    
    private boolean saved            = true;
    private DocumentState savedState = new DocumentState(new ArrayList<LateXElement>());
    
    private Stage primaryStage;
    private TreeView<LateXElement> tree;
    private TreeItem<LateXElement> treeRoot;
    private LateXMaker lm = new LateXMaker();
    private TextArea textArea;
    private HBox editZone;
    private ContextMenu addMenu;
    private MenuBar menuBar;
    private MenuItem generate;
    private Label info;

    private HashMap<Integer,String[]> nodesTypesMap;
    private HashMap<String,String> icons;
    private HashMap<String,String> helpers;

    private TreeItem<LateXElement> currentNode = null;

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        setTree();
        setEditZone();
        initializeNodesTypesMap();
        setIconsMap();
        setMenuBar();
        setHelpers();
        this.primaryStage = primaryStage;
        System.out.println(System.getProperty("os.name").toLowerCase().contains("windows"));
        
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
        
        tex.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                generate();
            }
        });
        preview.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                try {
                    preview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        pdf.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                try {
                    toPdf();
                } catch (IOException ex) {
                    Logger.getLogger(LatexEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        ToolBar tb = new ToolBar(tex,preview,pdf);
        header.getChildren().addAll(menuBar,tb);
        return header;
    }
    private void setIconsMap() {
        icons = new HashMap<>();
        icons.put("Titre","/data/texIcon.png");
        icons.put("Chapitre","/data/chapterIcon.png");
        icons.put("Section","/data/sectionIcon.png");
        icons.put("Sous-section","/data/subSectionIcon.png");
        icons.put("Sous-sous section","/data/subsubSectionIcon.png");
        for (String name : nodesTypesMap.get(5))
            icons.put(name,"/data/leafIcon.png");
    }
    
    private TreeItem<LateXElement> newTreeItem(LateXElement l) {
        String url;
        String command = l.getName();
        Node icon = null;
        if ((url = icons.get(command)) != null) 
           icon = new ImageView(new Image(getClass().getResourceAsStream(url)));        
        TreeItem<LateXElement> newElt;
        if (icon == null)
            newElt = new TreeItem<>(newLateXElement(command,l.getText()));
        else
            newElt = new TreeItem<>(newLateXElement(command,l.getText()),icon);
        return newElt;
    }

    private void setTree() {
        treeRoot = new TreeItem<LateXElement>(new Title("Test", lm),rootIcon);
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
                addMenu.getItems().removeAll(addMenu.getItems());
                LateXElement elt = currentNode.getValue();
                Menu addChild = null, addSibling = null;
                //Determination des elements principaux du popup
                if (elt.getDepth() != LateXElement.DEPTH_MAX) {
                    addMenu.getItems().add(addChild = new Menu("Ajouter un fils"));
                }
                if (elt.getDepth() != LateXElement.DEPTH_MIN) {
                    addMenu.getItems().add(addSibling = new Menu("Ajouter un frère"));
                }
                //Determination des elements secondaires du popup
                for (Integer depth : nodesTypesMap.keySet()) {
                    //D'abord les elements fils
                    if (addChild != null && depth > elt.getDepth()) {
                        if (addChild.getItems().size() != 0) {
                            addChild.getItems().add(new SeparatorMenuItem());
                        }
                        for (final String type : nodesTypesMap.get(depth)) {
                            final MenuItem item = new MenuItem(type);
                            addChild.getItems().add(item);
                            item.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent t) {
                                    addChild(type);
                                }
                            });
                        }
                    }
                    //Ensuite les elements freres
                    if (addSibling != null && depth == elt.getDepth()) {
                        if (depth > elt.getDepth()) {
                            if (addSibling.getItems().size() != 0) {
                                addSibling.getItems().add(new SeparatorMenuItem());
                            }
                        }
                        for (final String type : nodesTypesMap.get(depth)) {
                            final MenuItem item = new MenuItem(type);
                            addSibling.getItems().add(item);
                            item.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent t) {
                                    addSibling(type);
                                }
                            });
                        }
                    }
                }
                //Affichage du popup
                addMenu.show(editZone,pt.getX() + 10, pt.getY() + 10);
            }
        });
        tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tree.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<TreeItem<LateXElement>>() {
                    @Override
                    public void changed(ObservableValue<? extends TreeItem<LateXElement>> ov,
                            TreeItem<LateXElement> formerItem,
                            TreeItem<LateXElement> newItem
                    ) {
                        if (formerItem != null) {
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
                    }
                });
    }
    
    private void addSibling(String command) {
        TreeItem<LateXElement> newElt = newTreeItem(newLateXElement(command,""));
        currentNode.getParent().getChildren().add(newElt);
        currentNode = newElt;
        setSaved(false);
    }

    private void addChild(String command) {
        TreeItem<LateXElement> newElt = newTreeItem(newLateXElement(command,""));
        currentNode.getChildren().add(newElt);
        currentNode.setExpanded(true);
        currentNode = newElt;
        setSaved(false);
    }
    
    private LateXElement newLateXElement(String opName, String content) {
	LateXElement newElt;
	switch (opName) {
            case "Titre"             : newElt = new Title(content,lm); break;
            case "Chapitre"          : newElt = new Chapter(content,lm);	break;
            case "Section"           : newElt = new Section(content,lm); break;
            case "Sous-section"      : newElt = new Subsection(content,lm); break;
            case "Sous-sous section" : newElt = new SubSubSection(content,lm); break;
            case "Paragraphe"        : newElt = new Paragraph(content,lm); break;
            case "Liste"             : newElt = new List(content,lm); break;
            case "Code"              : newElt = new ProgrammingCode(content,lm); break;
            case "Code LateX"        : newElt = new LateXCode(content,lm); break;
            default                  : newElt = new Inclusion(content,lm); break;
	}
	return newElt;
    }
    
    private void setEditZone() {
        info      = new Label("Saisissez votre texte ici");
        textArea  = new TextArea();
        textArea.setPrefSize(600, 500);
        VBox vbox = new VBox(); 
      /*  textArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                System.out.println(t+" " +t1);
            }
        });*/
        vbox.setPadding(new Insets(5));
        vbox.setSpacing(5);
        /*ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(textArea);
        scrollPane.setPrefSize(600, 500);*/
        vbox.getChildren().addAll(info,textArea);//scrollPane);
        
        String[] operators = { "\\cdot","+","-","\\frac{}{}","\\sqrt[]{}",
				"\\forall","\\partial","\\exists","\\nexists","\\varnothing",
		 		"\\bigcap","\\bigcup","\\bigint","\\prod","\\sum",
		 		"\\nabla","\\in","\\notin","\\ni","",
		 		"^{}","_{}"
		       };
        String[] ctes = { "\\alpha","\\beta","\\gamma","\\delta","\\epsilon","\\mu","\\nu","\\xi","\\pi","\\rho",
			   "\\omega","\\Omega","\\theta","\\Delta","\\Psi","\\eta","\\lambda","\\sigma","\\tau",
			   "\\khi","\\phi","\\infty"
			 };
        Image img = new Image(LatexEditor.class.getResourceAsStream("/data/Operateurs.png"));
        IconSelectionView operateurs = new IconSelectionView(img,5,5,operators,"Opérateurs");
        operateurs.setActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {                
                textArea.cut();
                textArea.insertText(textArea.getCaretPosition(),e.getActionCommand()); 
            }
        });
        img = new Image(LatexEditor.class.getResourceAsStream("/data/AlphabetGrec.png"));
        IconSelectionView alphabetGrec = new IconSelectionView(img,5,5,ctes,"Alphabet grec");
        alphabetGrec.setActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {                
                textArea.cut();
                textArea.insertText(textArea.getCaretPosition(),e.getActionCommand()); 
            }
        });
        IconSelectionBox box = new IconSelectionBox();
        box.addSelectionView(operateurs);
        box.addSelectionView(alphabetGrec);
                
        editZone = new HBox(10);       
        editZone.getChildren().addAll(box,tree, vbox);
        editZone.setLayoutX(20);
        editZone.setPadding(new Insets(15));      
    }

    private void initializeNodesTypesMap() {
        nodesTypesMap = new HashMap<>();
        nodesTypesMap.put(0, new String[]{"Titre"});
        nodesTypesMap.put(1, new String[]{"Chapitre"});
        nodesTypesMap.put(2, new String[]{"Section"});
        nodesTypesMap.put(3, new String[]{"Sous-section"});
        nodesTypesMap.put(4, new String[]{"Sous-sous section"});
        nodesTypesMap.put(5, new String[]{"Paragraphe", "Liste", "Inclusion d'image", "Code", "Code LateX"});
    }

    private void setMenuBar() {
        menuBar = new MenuBar();
        Menu menuFile = new Menu("Fichier");
        Menu menuEdit = new Menu("Editer");
        Menu menuHelp = new Menu("Aide");
        
        MenuItem newDoc;
        MenuItem save;
        MenuItem saveAs;
        MenuItem load;
        MenuItem quit;
        
        menuFile.getItems().add(newDoc   = new MenuItem("Nouveau document"));
        menuFile.getItems().add(load     = new MenuItem("Charger"));
        menuFile.getItems().add(save     = new MenuItem("Enregistrer"));
        menuFile.getItems().add(saveAs   = new MenuItem("Enregistrer sous"));
        menuFile.getItems().add(generate = new MenuItem("Générer le code LateX"));
        menuFile.getItems().add(quit     = new MenuItem("Quitter"));
        
        newDoc  .setAccelerator(new KeyCharacterCombination("N",
                                KeyCombination.CONTROL_DOWN));
        save    .setAccelerator(new KeyCharacterCombination("S",
                                KeyCharacterCombination.CONTROL_DOWN));
        saveAs  .setAccelerator(new KeyCharacterCombination("S",
                                KeyCharacterCombination.CONTROL_DOWN,
                                KeyCharacterCombination.ALT_DOWN));
        load    .setAccelerator(new KeyCharacterCombination("L",
                                KeyCharacterCombination.CONTROL_DOWN));
        generate.setAccelerator(new KeyCharacterCombination("G",
                                KeyCharacterCombination.CONTROL_DOWN));
        quit    .setAccelerator(new KeyCharacterCombination("Q",
                                KeyCharacterCombination.CONTROL_DOWN));
        generate.setDisable(true);
        
        newDoc.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                createDocument();
                lateXElements = new ArrayList<>();
            }
        });
        save.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                save();
            }
        });
        saveAs.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                createDocument();
                save();
            }
        });
        load.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
               load();
            }
        });
        generate.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
               generate();
            }
        });
        quit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                System.exit(0);
            }
        });
        menuBar.getMenus().addAll(menuFile,menuEdit,menuHelp);
    }
    
    private void save() {
        try {
            currentNode.getValue().setText(textArea.getText());
            if (currentFile != null) {
                String path = currentFile.getAbsolutePath();
                File f = new File(path.substring(0, path.indexOf(".") + 1) + "javatex");
                FilterWriter fw = new FilterWriter(new BufferedWriter(new FileWriter(f)), new LateXFilter());
                ListeNommee<LateXElement> elements = getElements();
                Iterator<String> names = elements.getA().iterator();
                lateXElements = elements.getB();
                ArrayList<LateXElement> state = new ArrayList<>();
                for (LateXElement l : lateXElements) {
                    fw.write(names.next() + " " + l.textify() + "\n");
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
        if (!saved && b) {
            primaryStage.setTitle(primaryStage.getTitle().substring(1));
        } else if (saved && !b) {
            primaryStage.setTitle("*" + primaryStage.getTitle());
        }
        saved = b;
    }
    
    private void generate() {
        try {
            if (savedState.getCurrentState().isEmpty())
                save();
            lm.save(currentFile, savedState.getCurrentState());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void createDocument() {
        FileChooser f = new FileChooser();
        f.setTitle("Nouveau document");
        f.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichier LateX","*.tex"));

        if (currentDir != null) {
            f.setInitialDirectory(currentDir);
        }
        
        File file = f.showSaveDialog(primaryStage);       
        if (file != null) {
            currentDir = file.getParentFile();
            String s = file.getPath();
            int i = s.indexOf(".");
            if (i == -1 || !s.substring(i).equals(".javatex")) {
                file = new File(s + ".tex");
            }
            currentFile = file;
            save();
            primaryStage.setTitle(s + ".tex" + " - LateXEditor 3.0");
            generate.setDisable(false); 
        }       
    }
    
    private ListeNommee<LateXElement> getElements(TreeItem<LateXElement> node, String level) {
        ArrayList<LateXElement> elements = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
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
        ArrayList<String> names         = elts.getA();
        ArrayList<LateXElement> objects = elts.getB();
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
        return parentNode;
    }
    
    public void setElements(ListeNommee<LateXElement> elts) {
        treeRoot.getChildren().removeAll(treeRoot.getChildren());
        tree.getSelectionModel().select(treeRoot);
        treeRoot.setValue(elts.getB().get(0));
        textArea.setText(currentNode.getValue().getText());
        setElements(elts,treeRoot,0,elts.getA().size());
        tree.getSelectionModel().select(treeRoot);
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
            f.setTitle("Nouveau document");
            f.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Fichier JavateX", "*.javatex"));

            if (currentDir != null) {
                f.setInitialDirectory(currentDir);
            }

            File file = f.showOpenDialog(primaryStage);
            if (file != null) {
                currentDir = file.getParentFile();
                String s = file.getPath();
                int i = s.indexOf(".");
                if (i == -1 || !s.substring(i).equals(".javatex")) {
                    file = new File(s + ".tex");
                }
                currentFile = file;

                TokenReader tr = new TokenReader(new FileReader(file), "#");
                ArrayList<String> buffer = new ArrayList<>();
                ArrayList<LateXElement> elements = new ArrayList<>();
                ArrayList<String> names = new ArrayList<>();

                while ((s = tr.readToNextToken()) != null) {
                    buffer.add(s.trim());
                }

                String path = file.getAbsolutePath();
                currentFile = new File(path.substring(0, path.indexOf(".") + 1) + "tex");
                Iterator<String> it = buffer.iterator();
                while (it.hasNext()) {
		//Si le fichier est bien forme on devrait toujours pouvoir faire deux next() de suite
                    //des lors qu'on peut en faire un !
                    //createLateXElement(it.next(),it.next(),lateXElements);
                    String declaration = it.next();
                    String content = it.next();
                    i = declaration.lastIndexOf('>');
                    String name = i == -1 ? "" : declaration.substring(0, i + 1);
                    String type = i == -1 ? declaration : declaration.substring(i + 1).trim();

                    names.add(name);
                    elements.add(newLateXElement(type, content));
                }
                //Si apres tout cela il n'y a aucune erreur, on peut enfin ecraser les precedents buffers
                tr.close();
                setElements(new ListeNommee<>(names, elements));
                primaryStage.setTitle(path + " - LateXEditor 3.0");
                setSaved(true);
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
    
    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private void setHelpers() {
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
        helpers.put("Code", "Saisissez votre code ici");
        helpers.put("Code LateX", "Saisissez votre code ici");
    }
}

