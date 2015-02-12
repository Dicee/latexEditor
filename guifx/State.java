package guifx;

public abstract class State<E> implements Comparable<State<E>> {
	
	protected E currentState;
	
	public State(E state) {
		this.currentState = state;
	}

	public E getCurrentState() {
		return currentState;
	}

	public void setCurrentState(E currentState) {
		this.currentState = currentState;
	}
}