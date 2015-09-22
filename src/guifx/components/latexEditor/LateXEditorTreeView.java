package guifx.components.latexEditor;

import static guifx.utils.Settings.bindProperty;
import static guifx.utils.Settings.strings;
import static java.util.Arrays.asList;
import static properties.LanguageProperties.ADD_CHILD_HEAD;
import static properties.LanguageProperties.ADD_CHILD_TAIL;
import static properties.LanguageProperties.ADD_SIBLING;
import static properties.LanguageProperties.COPY;
import static properties.LanguageProperties.COPY_RAW;
import static properties.LanguageProperties.CUT;
import static properties.LanguageProperties.DELETE;
import static properties.LanguageProperties.PASTE;
import static properties.LanguageProperties.PASTE_RAW;
import static properties.LanguageProperties.TITLE;
import guifx.actions.ActionManager;
import guifx.actions.CancelableAction;
import guifx.components.generics.ControlledTreeView;
import guifx.utils.NamedObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import javafx.util.Pair;
import latex.elements.LateXElement;
import latex.elements.Title;

import com.dici.check.Check;

public class LateXEditorTreeView extends ControlledTreeView<NamedObject<LateXElement>> {
	private static final Map<Integer, List<String>>	NODES_TYPES_MAP;

	private TreeItem<NamedObject<LateXElement>> newTreeItem(LateXElement elt) {
		return factory.apply(newNamedObject(elt));
	}
	
	public static NamedObject<LateXElement> newNamedObject(LateXElement elt) {
		return new NamedObject<>(strings.getObservableProperty(elt.getType()),elt);
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
						addChildToSelectedNode(type,entry.getValue());
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
		MenuItem copy     = new MenuItem();
		MenuItem cut      = new MenuItem();
		MenuItem paste    = new MenuItem();
		MenuItem copyRaw  = new MenuItem();
		MenuItem pasteRaw = new MenuItem();
			
		bindProperty(copy    .textProperty(),COPY     );
		bindProperty(cut     .textProperty(),CUT      );
		bindProperty(paste   .textProperty(),PASTE    );
		bindProperty(copyRaw .textProperty(),COPY_RAW );
		bindProperty(pasteRaw.textProperty(),PASTE_RAW);
		
		copy    .setOnAction(ev -> copySelectedNode());
		cut     .setOnAction(ev -> cutSelectedNode(true));
		paste   .setOnAction(ev -> pasteFromClipboardToSelectedNode());
		copyRaw .setOnAction(ev -> copySelectedNodeRawContent());
		pasteRaw.setOnAction(ev -> pasteRawContentToSelectedNode());
		
		addMenu.getItems().addAll(copy,cut,paste,copyRaw,pasteRaw);
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
		addChildToSelectedNode(new NamedObject<>(strings.getObservableProperty(command),elt),option);
	}
	
	public void pasteRawContentToSelectedNode() {
		
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
		addSiblingToSelectedNode(new NamedObject<>(strings.getObservableProperty(command),LateXElement.newLateXElement(command,"")));
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
	
	public NamedList<LateXElement> getLateXElements() {
		NamedList<NamedObject<LateXElement>> elts = super.getElements();
		return new NamedList<>(
			elts.getKey(),
			elts.getValue().stream().map(namedObj -> namedObj.bean).collect(Collectors.toList()));
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
}