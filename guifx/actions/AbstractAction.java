package guifx.actions;

abstract class AbstractAction implements ObservableAction {
	@Override
	public final void perform(StateObserver observer) {
		doAction();
		updateState(observer);
	}
	
	protected abstract void doAction();
}
