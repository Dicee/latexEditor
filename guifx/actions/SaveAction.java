package guifx.actions;

public abstract class SaveAction extends AbstractAction {
	public SaveAction(ActionManager actionManager) {
		super(actionManager);
	}
	
	@Override
	public void perform() {
		save();
		actionManager.handleSaveEvent();
	}
	
	public abstract void save();
}
