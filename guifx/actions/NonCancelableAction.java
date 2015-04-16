package guifx.actions;

public abstract class NonCancelableAction extends AbstractAction {	
	@Override
	public final void updateState(StateObserver observer) { observer.handleIrreversibleStateChange(); }	
}
