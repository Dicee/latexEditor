package guifx.actions;

interface ObservableAction extends Action {
	default void updateState(StateObserver observer) { };
}
