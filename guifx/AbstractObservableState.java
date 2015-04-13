package guifx;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class AbstractObservableState implements ObservableState {
	private Set<StateListener> listeners = new HashSet<>();
	
	@Override
	final public void addStateListener(StateListener listener) { listeners.add(listener); }
	protected final void fireChangeEvent() { fireEvent(StateListener::handleChangeEvent); }
	protected final void fireSaveEvent() { fireEvent(StateListener::handleSaveEvent); }
	
	private void fireEvent(Consumer<StateListener> callable) {
		for (StateListener listener : listeners) 
			try {
				new Thread(() -> callable.accept(listener)).start();
			} catch (Throwable t) {
				t.printStackTrace();
			}
	}
}
