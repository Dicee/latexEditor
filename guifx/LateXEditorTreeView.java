package guifx;

import static guifx.utils.Settings.properties;
import static guifx.utils.Settings.strings;
import static java.util.Arrays.asList;
import guifx.actions.ActionManager;
import guifx.actions.CancelableAction;
import guifx.components.ControlledTreeView;
import guifx.utils.NamedObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import latex.elements.LateXElement;
import latex.elements.Title;

public class LateXEditorTreeView extends ControlledTreeView<NamedObject<LateXElement>> {
	private static final Map<Integer, List<String>>	NODES_TYPES_MAP;
	
	public final TreeItem<NamedObject<LateXElement>> newTreeItem(LateXElement elt) {
		return newTreeItem(new NamedObject<>(strings.getObservableProperty(elt.getType()),elt));
	}
	
	@Override
	public TreeItem<NamedObject<LateXElement>> newTreeItem(NamedObject<LateXElement> elt) {
		String url     = properties.getProperty(elt.bean.getType() + "Icon");
		Node   icon    = new ImageView(new Image(getClass().getResourceAsStream(url != null ? url : properties.getProperty("leafIcon"))));
		return icon == null ? new TreeItem<>(elt) : new TreeItem<>(elt,icon);
	}
	
	public static final NamedObject<LateXElement> newNamedObject(LateXElement elt) {
		return new NamedObject<>(strings.getObservableProperty(elt.getType()),elt);
	}
	
	public LateXEditorTreeView(TreeItem<NamedObject<LateXElement>> root, ActionManager actionManager) {
		super(root,actionManager);
		getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
	}

	@Override
	public void openContextMenu(Point2D pt) {
		// creation of the relevant contextual popup
		LateXElement elt = currentNode.getValue().bean;

		buildAddMenus(elt);
		buildClipboardMenus(elt);
		buildDeleteMenu();

		// display the popup
		addMenu.show(this,pt.getX() + 10,pt.getY() + 10);
	}

	private void buildClipboardMenus(LateXElement elt) {
		if (!(elt instanceof Title)) {
			MenuItem copy  = new MenuItem();
			MenuItem cut   = new MenuItem();
			MenuItem paste = new MenuItem();
			
			copy .textProperty().bind(strings.getObservableProperty("copy" ));
			cut  .textProperty().bind(strings.getObservableProperty("cut"  ));
			paste.textProperty().bind(strings.getObservableProperty("paste"));

			copy .setOnAction(ev -> copySelectedNode());
			cut  .setOnAction(ev -> cutSelectedNode(true));
			paste.setOnAction(ev -> pasteFromClipboardToSelectedNode());
			addMenu.getItems().addAll(copy,cut,paste);
		}
	}
	
	private void buildDeleteMenu() {
		MenuItem delete = new MenuItem();
		addMenu.getItems().add(delete);
		delete.textProperty().bind(strings.getObservableProperty("delete"));
		delete.setOnAction(ev -> cutSelectedNode(false));
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
								addChildToSelectedNode(type,entry.getValue());
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
	
	@Override
	public void addChildToSelectedNode(NamedObject<LateXElement> elt, int option) {
		TreeItem<NamedObject<LateXElement>> newElt = newTreeItem(elt);
		TreeItem<NamedObject<LateXElement>> parent = currentNode;
		
		actionManager.perform(new CancelableAction() {
			@Override
			public void doAction() {
				if (option == INSERT_TAIL) currentNode.getChildren().add(  newElt);
				else                       currentNode.getChildren().add(0,newElt);
				currentNode.setExpanded(true);
			}
			
			@Override
			public void cancel() {
				parent.getChildren().remove(newElt);
			}
		});
	}
	
	private void addChildToSelectedNode(String command, int option) {
		LateXElement elt = LateXElement.newLateXElement(command,"");
		addChildToSelectedNode(new NamedObject<>(strings.getObservableProperty(command),elt),option);
	}
	
	
	@Override
	public void addSiblingToSelectedNode(NamedObject<LateXElement> elt) {
		TreeItem<NamedObject<LateXElement>> newElt = newTreeItem(elt);
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
			public void cancel() {
				parent.getChildren().remove(newElt);
			}
		});
		
	}
	
	private void addSibling(String command) { 
		addSiblingToSelectedNode(new NamedObject<>(strings.getObservableProperty(command),LateXElement.newLateXElement(command,"")));
	} 
	
	@Override
	public void cutSelectedNode(boolean saveToClipboard) {
		TreeItem<NamedObject<LateXElement>>	parent = currentNode.getParent();
		TreeItem<NamedObject<LateXElement>> node   = currentNode;
		int                                 index  = parent.getChildren().indexOf(node);
		if (saveToClipboard) 
			clipBoard = currentNode;
		
		actionManager.perform(new CancelableAction() {
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
