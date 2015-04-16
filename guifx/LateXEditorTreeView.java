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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
		addMenu.show(tree,pt.getX() + 10,pt.getY() + 10);
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
	
	@Override
	private void addChild(String command, int option) {
		TreeItem<NamedObject<LateXElement>> newElt = newTreeItem(LateXElement.newLateXElement(command,""));
		TreeItem<NamedObject<LateXElement>> parent = currentNode;
		
		actionManager.perform(new CancelableAction() {
			@Override
			public void perform() {
				if (option == INSERT_TAIL) currentNode.getChildren().add(  newElt);
				else                       currentNode.getChildren().add(0,newElt);
				currentNode.setExpanded(true);
//				actionManager.setSaved(false);
			}
			
			@Override
			public void cancel() {
				parent.getChildren().remove(newElt);
			}
		});
	}
	
	private TreeItem<NamedObject<LateXElement>> newTreeItem(LateXElement l) {
		String command = l.getType();
		String url     = properties.getProperty(command + "Icon");
		Node   icon    = new ImageView(new Image(getClass().getResourceAsStream(url != null ? url : properties.getProperty("leafIcon"))));

		NamedObject<LateXElement> no = new NamedObject<LateXElement>(strings.getObservableProperty(command),l);
		return icon == null ? new TreeItem<>(no) : new TreeItem<>(no,icon);
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
