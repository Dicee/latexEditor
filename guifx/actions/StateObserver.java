package guifx.actions;

interface StateObserver {
	public void handleStateSaved();
	public void handleReversibleStateChange();
	public void handleIrreversibleStateChange();
}