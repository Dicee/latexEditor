package guifx.actions;

public abstract class CancelableAction extends AbstractAction {
	public CancelableAction(ActionManager actionManager) { super(actionManager,false); }
	
	@Override
	final public void perform() {
		action
	}
	
	public abstract void cancel();
}
