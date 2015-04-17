package guifx.components;

import guifx.actions.ActionManager;
import guifx.utils.NamedObject;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;
import latex.elements.LateXElement;

public abstract class ControlledTreeView<T> extends TreeView<T> {
	protected static final int	INSERT_HEAD	= 0;
	protected static final int	INSERT_TAIL	= 1;

	protected ActionManager		actionManager;

	protected TreeItem<T>		treeRoot;
	protected TreeItem<T>		currentNode	= null;
	protected TreeItem<T>		clipBoard	= null;

	protected ContextMenu		addMenu		= new ContextMenu();

	public ControlledTreeView(TreeItem<T> root, ActionManager actionManager) {
		super(root);
		this.treeRoot      = root;
		this.actionManager = actionManager;
		this.currentNode   = root;
		
		setOnMouseClicked(this::handleMouseClickOnTree);
		getSelectionModel().selectedItemProperty().addListener((ov,formerItem,newItem) -> currentNode = newItem);
	}
	
	private void handleMouseClickOnTree(MouseEvent mev) {
		if (mev.getButton().equals(MouseButton.SECONDARY)) {
			addMenu.hide();
			addMenu.getItems().clear();
			openContextMenu(new Point2D(mev.getScreenX(),mev.getScreenY()));
		} else
			addMenu.hide(); 
	}
	
	public abstract void openContextMenu(Point2D pt);
	public abstract void addChildToSelectedNode(T elt, int option);
	public abstract void addSiblingToSelectedNode(T elt);
	public abstract void cutSelectedNode(boolean saveToClipboard);
	public abstract void copySelectedNode();
	public abstract void pasteFromClipboardToSelectedNode();
	public abstract TreeItem<T> newTreeItem(T elt);
	
	public void setElements(TreeItem<T> root, List<Pair<Integer,T>> elts) {
		getSelectionModel().clearSelection();
		treeRoot = root;

		if (!elts.isEmpty()) {
			Deque<Pair<Integer,TreeItem<T>>> stack = new LinkedList<>();
			stack.push(new Pair<>(elts.get(0).getKey(),treeRoot));

			for (Pair<Integer,T> elt : elts.subList(1,elts.size())) {
				TreeItem<T> node = newTreeItem(elt.getValue());

				while (stack.peek().getKey() >= elt.getKey())
					stack.pop();
				stack.peek().getValue().getChildren().add(node);
				stack.push(new Pair<>(elt.getKey(),node));
			}
		}

		setRoot(treeRoot);
		treeRoot.setExpanded(true);
		getSelectionModel().select(treeRoot);
	}
	
	public TreeItem<T> getCurrentNode() { return currentNode; }
}