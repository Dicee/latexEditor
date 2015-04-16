package guifx.actions;

public abstract class CancelableAction extends AbstractAction {	
	@Override
	public final void updateState(StateObserver observer) { observer.handleReversibleStateChange(); }	
	public abstract void cancel();
}
