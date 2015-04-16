package guifx.actions;

public abstract class SaveAction extends AbstractAction {
	@Override
	public final void updateState(StateObserver observer) { observer.handleStateSaved(); }
	public abstract void save();
}
