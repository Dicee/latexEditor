package guifx.actions;

public abstract class AbstractAction implements Action {
	private final boolean save;

	public AbstractAction(boolean save) {
		this.save = save;
	}
	
	@Override
	final public void perform(ActionManager actionManager) {
		performImpl();
		actionManager.setSaved(save);
	}

	protected abstract void performImpl();
}
