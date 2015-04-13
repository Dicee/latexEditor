package guifx.components;

import guifx.actions.ActionManager;
import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public abstract class ControlledTreeView<T> extends TreeView<T> {
	protected static final int						INSERT_HEAD	= 0;
	protected static final int						INSERT_TAIL	= 1;
	
	protected ActionManager	actionManager;

	protected TreeView<T>	tree;
	protected TreeItem<T>	treeRoot;
	protected TreeItem<T>	currentNode	= null;
	protected TreeItem<T>	clipBoard	= null;

	protected ContextMenu	addMenu		= new ContextMenu();

	public ControlledTreeView(TreeItem<T> root, ActionManager actionManager) {
		this.treeRoot      = root;
		this.actionManager = actionManager;
		this.currentNode   = root;
		this.tree          = new TreeView<>(root);
	}
	
	public abstract void openContextMenu(Point2D pt);
	public abstract void addChild(T elt, int option);
}
