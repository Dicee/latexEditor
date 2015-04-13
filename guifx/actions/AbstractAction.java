package guifx.actions;

public abstract class AbstractAction implements Action {
	protected ActionManager actionManager;

	public AbstractAction(ActionManager actionManager) {
		this.actionManager = actionManager;
	}
	
	@Override
	public void perform() {
		performImpl();
		actionManager.handleChangeEvent();
	}

	protected abstract void performImpl();
}
