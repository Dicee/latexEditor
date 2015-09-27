package guifx.components.latexEditor;

import static com.dici.collection.CollectionUtils.setOf;
import static com.dici.collection.richIterator.PairRichIterator.pairIterator;
import static guifx.utils.DialogsFactory.showPreFormattedError;
import static guifx.utils.JavatexIO.readFromJavatex;
import static guifx.utils.Settings.bindProperty;
import static guifx.utils.Settings.strings;
import static java.util.Arrays.asList;
import static properties.LanguageProperties.ADD_CHILD_HEAD;
import static properties.LanguageProperties.ADD_CHILD_TAIL;
import static properties.LanguageProperties.ADD_SIBLING;
import static properties.LanguageProperties.AN_ERROR_OCCURRED_MESSAGE;
import static properties.LanguageProperties.CHAPTER;
import static properties.LanguageProperties.CODE;
import static properties.LanguageProperties.COPY;
import static properties.LanguageProperties.COPY_RAW;
import static properties.LanguageProperties.CUT;
import static properties.LanguageProperties.DELETE;
import static properties.LanguageProperties.ERROR;
import static properties.LanguageProperties.IMAGE;
import static properties.LanguageProperties.LATEX;
import static properties.LanguageProperties.LIST;
import static properties.LanguageProperties.MALFORMED_JAVATEX_ERROR;
import static properties.LanguageProperties.PARAGRAPH;
import static properties.LanguageProperties.PASTE;
import static properties.LanguageProperties.PASTE_RAW;
import static properties.LanguageProperties.SECTION;
import static properties.LanguageProperties.SUBSECTION;
import static properties.LanguageProperties.SUBSUBSECTION;
import static properties.LanguageProperties.TEMPLATE;
import static properties.LanguageProperties.TITLE;
import guifx.utils.NamedObject;
import guifx.utils.WrongFormatException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.geometry.Point2D;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.util.Pair;
import latex.DocumentParameters;
import latex.elements.LateXElement;
import properties.LanguageProperties;

import com.dici.check.Check;
import com.dici.javafx.actions.ActionManager;
import com.dici.javafx.actions.CancelableAction;
import com.dici.javafx.components.ControlledTreeView;

public class LateXEditorTreeView extends ControlledTreeView<NamedObject<LateXElement>> {
	private static final Map<Integer, List<String>>	NODES_TYPES_MAP;

	private static final Set<String> RAW_CONTENT_COPY_ELIGIBLES = setOf(CHAPTER, SECTION, SUBSECTION, SUBSUBSECTION, 
	        PARAGRAPH, IMAGE, CODE);
	
	private TreeItem<NamedObject<LateXElement>> newTreeItem(LateXElement elt) {
		return factory.apply(namedLateXElement(elt));
	}
	
	public LateXEditorTreeView(TreeItem<NamedObject<LateXElement>> root, ActionManager actionManager, 
			Function<NamedObject<LateXElement>,TreeItem<NamedObject<LateXElement>>> factory) {
		super(root,actionManager,factory);
		getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
	}

	@Override
	public void openContextMenu(Point2D pt) {
		LateXElement elt = currentNode.getValue().bean;

		buildAddMenus(elt);
		if (!elt.getType().equals(TITLE)) {
			buildClipboardMenus(elt);
			buildDeleteMenu();
		}
		addMenu.show(this,pt.getX() + 10,pt.getY() + 10);
	}
	
	private void buildAddMenus(LateXElement elt) {
		Map<Menu,Integer> addChildMenus  = createAddChildMenus (elt);
		Optional<Menu>    addSiblingMenu = createAddSiblingMenu(elt); 
		
		for (Integer depth : NODES_TYPES_MAP.keySet()) {
			buildChildrenElements(addChildMenus,elt.getDepth(),depth);
			if (addSiblingMenu.isPresent()) 
				buildSiblingElements(addSiblingMenu.get(),elt.getDepth(),depth);
		}
	}

	private Map<Menu,Integer> createAddChildMenus(LateXElement elt) {
		Map<Menu,Integer> map = new HashMap<>();
		if (elt.getDepth() != LateXElement.DEPTH_MAX) {
			Menu addChildHead = new Menu();
			Menu addChildTail = new Menu();
			bindProperty(addChildHead.textProperty(),ADD_CHILD_HEAD);
			bindProperty(addChildTail.textProperty(),ADD_CHILD_TAIL);
			
			map.put(addChildHead,INSERT_HEAD);
			map.put(addChildTail,INSERT_TAIL);
			
			addMenu.getItems().addAll(addChildHead,addChildTail);
		}
		return map;
	}
	
	private Optional<Menu> createAddSiblingMenu(LateXElement elt) {
		if (elt.getDepth() != LateXElement.DEPTH_MIN) {
			Menu addSibling = new Menu();
			addMenu.getItems().add(addSibling);
			addSibling.textProperty().bind(strings.getObservableProperty(ADD_SIBLING));
			return Optional.of(addSibling);
		}
		return Optional.empty();
	}

	private void buildChildrenElements(Map<Menu,Integer> addChildMenus, int eltDepth, int childrenDepth) {
		if (childrenDepth > eltDepth) {
			addChildMenus.entrySet().stream().forEach(entry -> {
				Menu addChild = entry.getKey();
				if (!addChild.getItems().isEmpty())
					addChild.getItems().add(new SeparatorMenuItem());
				
				for (String type : NODES_TYPES_MAP.get(childrenDepth)) {
					MenuItem item = new MenuItem();
					item.textProperty().bind(strings.getObservableProperty(type));
					addChild.getItems().add(item);
					item.setOnAction(ev -> {
						addChildToSelectedNode(type, entry.getValue());
						addMenu.hide();
					});
				}
			});
		}
	}

	private void buildSiblingElements(Menu addSiblingMenu, int eltDepth, int depth) {
		if (eltDepth == depth) { 
			for (String type : NODES_TYPES_MAP.get(eltDepth)) {
				MenuItem item = new MenuItem();
				item.textProperty().bind(strings.getObservableProperty(type));
				addSiblingMenu.getItems().add(item);
				item.setOnAction(event -> addSibling(type));
			}
		}
	}

	private void buildClipboardMenus(LateXElement elt) {
		MenuItem copy  = new MenuItem();
		MenuItem cut   = new MenuItem();
		MenuItem paste = new MenuItem();
			
		bindProperty(copy .textProperty(),COPY );
		bindProperty(cut  .textProperty(),CUT  );
		bindProperty(paste.textProperty(),PASTE);
		
		copy .setOnAction(ev -> copySelectedNode());
		cut  .setOnAction(ev -> cutSelectedNode(true));
		paste.setOnAction(ev -> pasteFromClipboardToSelectedNode());
		
		addMenu.getItems().addAll(copy, cut, paste);
		
		if (RAW_CONTENT_COPY_ELIGIBLES.contains(elt.getType())) buildRawContentClipboardMenus();
	}
	
	private void buildRawContentClipboardMenus() {
	    MenuItem copyRaw  = new MenuItem();
	    MenuItem pasteRaw = new MenuItem();
	    
	    bindProperty(copyRaw .textProperty(),COPY_RAW );
	    bindProperty(pasteRaw.textProperty(),PASTE_RAW);

	    copyRaw .setOnAction(ev -> copySelectedNodeRawContent());
	    pasteRaw.setOnAction(ev -> pasteRawContentToSelectedNode());
	    
	    addMenu.getItems().addAll(copyRaw, pasteRaw);
	}
	
	private void buildDeleteMenu() {
		MenuItem delete = new MenuItem();
		addMenu.getItems().add(delete);
		delete.textProperty().bind(strings.getObservableProperty(DELETE));
		delete.setOnAction(ev -> cutSelectedNode(false));
		if (currentNode.getParent() == null) delete.setDisable(true);
	}
	
	public void updateElements(List<Pair<Integer,LateXElement>> elts) {
		TreeItem<NamedObject<LateXElement>> node = getRoot();
		Pair<Integer, LateXElement> pair = elts.get(0);
		Check.isTrue(pair.getKey() == 0 && pair.getValue().getType().equals(TITLE));
		
		
	}
	
	@Override
	public void addChildToSelectedNode(NamedObject<LateXElement> elt, int option) {
		actionManager.perform(new CancelableAction() {
			TreeItem<NamedObject<LateXElement>> newElt = factory.apply(elt);
			TreeItem<NamedObject<LateXElement>> parent = currentNode;
			
			@Override
			public void doAction() {
				if (option == INSERT_TAIL) parent.getChildren().add(  newElt);
				else                       parent.getChildren().add(0,newElt);
				parent.setExpanded(true);
			}
			
			@Override
			public void cancel() { parent.getChildren().remove(newElt); }
		});
	}
	
	private void addChildToSelectedNode(String command, int option) {
		LateXElement elt = LateXElement.newLateXElement(command,"");
		addChildToSelectedNode(namedLateXElement(elt), option);
	}
	
	public void pasteRawContentToSelectedNode() {
		try {
		    String clipboard = (String) Clipboard.getSystemClipboard().getContent(DataFormat.PLAIN_TEXT);
            List<Pair<Integer, LateXElement>> elements = readFromJavatex(clipboard, new DocumentParameters());
            
            TreeItem<NamedObject<LateXElement>> fakeRoot = factory.apply(new NamedObject<>(null, null));
            treeFromFlatList(fakeRoot, namedLateXElements(elements), factory);

            if (fakeRoot.getChildren().isEmpty()) return;
            
            boolean insertAsChildren = getValue(fakeRoot.getChildren().get(0)).getDepth() > getSelectedItem().getDepth();
            int     numChildren      = fakeRoot.getChildren().size(); 
            
            // cannot insert as siblings if there is no parent
            TreeItem<NamedObject<LateXElement>> node        = currentNode;
            TreeItem<NamedObject<LateXElement>> parent      = node.getParent();
            int                                 indexOfNode = parent == null ? -1 : parent.getChildren().indexOf(node);
            Check.isTrue(insertAsChildren || parent != null);
            
            actionManager.perform(new CancelableAction() {
                
                @Override
                protected void doAction() {
                    if (insertAsChildren) moveChildrenToNewRoot(fakeRoot, node);                      
                    else {
                        // insert as siblings
                        for (int i = 0; i < numChildren; i++) parent.getChildren().add(indexOfNode + 1 + i, fakeRoot.getChildren().remove(0));
                    }
                }
                
                @Override
                public void cancel() {
                    if (insertAsChildren) moveChildrenToNewRoot(node, fakeRoot);
                    else {
                        // remove siblings from selected node, add back as children of the fake root
                        for (int i = 0; i < numChildren; i++) fakeRoot.getChildren().add(parent.getChildren().remove(indexOfNode + 1));
                    }
                }
                
                private <T> void moveChildrenToNewRoot(TreeItem<T> root, TreeItem<T> newRoot) {
                    // make the code simple, not optimized. I don't expect the tree to get big (typically less than a few
                    // hundred leaves), so this is fine.
                    for (int i = 0; i < numChildren; i++) newRoot.getChildren().add(i, root.getChildren().remove(0));      
                }
            });
        } catch (WrongFormatException e) {
            showPreFormattedError(this, ERROR, AN_ERROR_OCCURRED_MESSAGE, MALFORMED_JAVATEX_ERROR);
        }
	}
	
	private static LateXElement getValue(TreeItem<NamedObject<LateXElement>> treeItem) { return treeItem.getValue().bean; }
	
	private static NamedObject<LateXElement> namedLateXElement(LateXElement elt) {
        return new NamedObject<>(strings.getObservableProperty(elt.getType()), elt);
    }
	
	public static List<Pair<Integer,NamedObject<LateXElement>>> namedLateXElements(List<Pair<Integer, LateXElement>> elts) {
	    return pairIterator(elts.iterator(), Pair::getKey, pair -> namedLateXElement(pair.getValue())).toList();
	}
	
	public void copySelectedNodeRawContent() {
		ClipboardContent clipboardContent = new ClipboardContent();
		clipboardContent.putString(currentNode.getValue().bean.textify());
		Clipboard.getSystemClipboard().setContent(clipboardContent);
	}
	
	@Override
	public void addSiblingToSelectedNode(NamedObject<LateXElement> elt) {
		TreeItem<NamedObject<LateXElement>> newElt = factory.apply(elt);
		TreeItem<NamedObject<LateXElement>> parent = currentNode.getParent();
		TreeItem<NamedObject<LateXElement>> node   = currentNode;
		
		actionManager.perform(new CancelableAction() {
			@Override
			public void doAction() {
				int i = parent.getChildren().indexOf(node);
				if (i == parent.getChildren().size() - 1) parent.getChildren().add(newElt);
				else                                      parent.getChildren().add(i + 1,newElt);
			}
			
			@Override
			public void cancel() { parent.getChildren().remove(newElt); }
		});
	}
	
	private void addSibling(String command) { 
		addSiblingToSelectedNode(new NamedObject<>(strings.getObservableProperty(command), LateXElement.newLateXElement(command,"")));
	} 
	
	@Override
	public void cutSelectedNode(boolean saveToClipboard) {
		if (saveToClipboard) 
			clipBoard = currentNode;
		
		actionManager.perform(new CancelableAction() {
			TreeItem<NamedObject<LateXElement>>	parent = currentNode.getParent();
			TreeItem<NamedObject<LateXElement>> node   = currentNode;
			int                                 index  = parent.getChildren().indexOf(node);
			
			@Override
			public void doAction() {
				TreeItem<NamedObject<LateXElement>> next;
				if      (parent.getChildren().size() == 1        ) next = parent;
				else if (index != parent.getChildren().size() - 1) next = parent.getChildren().get(index + 1);
				else                                               next = parent.getChildren().get(index - 1);
				parent.getChildren().remove(node);
				getSelectionModel().select(next);
			}
			
			@Override
			public void cancel() {
				parent.getChildren().add(index,node);
				getSelectionModel().select(node);
			}
		});
	}
	
	@Override
	public void copySelectedNode() { clipBoard = cloneNode(currentNode); }
	
	private TreeItem<NamedObject<LateXElement>> cloneNode(TreeItem<NamedObject<LateXElement>> node) {
		TreeItem<NamedObject<LateXElement>> root = newTreeItem(node.getValue().bean.clone());
		root.getChildren().addAll(node.getChildren().stream().map(n -> cloneNode(n)).collect(Collectors.toList()));
		return root;
	}
	
	@Override
	public void pasteFromClipboardToSelectedNode() {
		TreeItem<NamedObject<LateXElement>> toPaste      = cloneNode(clipBoard);
		TreeItem<NamedObject<LateXElement>> current      = currentNode;
		LateXElement                        clipboardElt = toPaste    .getValue().bean;
		LateXElement                        elt          = currentNode.getValue().bean;
		TreeItem<NamedObject<LateXElement>>	parent       = elt.getDepth() < clipboardElt.getDepth() ? currentNode : currentNode.getParent();
		
		if (elt.getDepth() <= clipboardElt.getDepth())
			actionManager.perform(new CancelableAction() {
				@Override
				public void doAction() {
					if (toPaste != null) {
						if (elt.getDepth() < clipboardElt.getDepth())
							parent.getChildren().add(0,toPaste);
						else if (elt.getDepth() == clipboardElt.getDepth()) {
							int index = parent.getChildren().indexOf(current);
							if (index < parent.getChildren().size() - 1) parent.getChildren().add(index + 1,toPaste);
							else                                         parent.getChildren().add(toPaste);
						} 
						getSelectionModel().select(toPaste);
					}
				}
				
				@Override
				public void cancel() {
					parent.getChildren().remove(toPaste);
				}
			});
	}
	
	public boolean      isSelectedItemRawContentCopiable() { return RAW_CONTENT_COPY_ELIGIBLES.contains(getSelectedItem().getType()); }
	public LateXElement getSelectedItem                 () { return getValue(getSelectionModel().getSelectedItem())                 ; }
	
	public NamedList<LateXElement> getLateXElements() {
		NamedList<NamedObject<LateXElement>> elts = super.getElements();
		return new NamedList<>(
			elts.getKey(),
			elts.getValue().stream().map(namedObj -> namedObj.bean).collect(Collectors.toList()));
	}
	
	static {
        NODES_TYPES_MAP = new HashMap<>();
        NODES_TYPES_MAP.put(0, asList(TITLE));
        NODES_TYPES_MAP.put(1, asList(CHAPTER));
        NODES_TYPES_MAP.put(2, asList(SECTION));
        NODES_TYPES_MAP.put(3, asList(SUBSECTION));
        NODES_TYPES_MAP.put(4, asList(SUBSUBSECTION));
        NODES_TYPES_MAP.put(5, asList(PARAGRAPH, LIST, IMAGE, CODE, LATEX, TEMPLATE));
	}
}