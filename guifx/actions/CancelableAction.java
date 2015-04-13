package guifx.actions;

public abstract class CancelableAction extends AbstractAction {
	public CancelableAction(ActionManager actionManager) {
		super(actionManager);
	}

	public abstract void cancel();
}
