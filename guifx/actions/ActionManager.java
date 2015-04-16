package guifx.actions;

import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class ActionManager implements StateObserver {
	private Deque<CancelableAction>	previous	= new LinkedList<>();
	private Deque<CancelableAction>	next		= new LinkedList<>();

	private BooleanProperty			hasNext		= new SimpleBooleanProperty(false);
	private BooleanProperty			hasPrevious	= new SimpleBooleanProperty(false);
	private BooleanProperty			isSaved  	= new SimpleBooleanProperty(true);
	
	
	public void perform(Action action) {
		action.perform(this);
		clearNext();
	}
	
	public void undo() {
		if (!previous.isEmpty()) { 
			next.push(previous.pop());
			hasNext.set(true);
			next.peek().cancel();
		} else 
			throw new NoSuchElementException("No action to undo");
	}
	
	public void redo() {
		if (!next.isEmpty()) perform(next.pop());
		else                 throw new NoSuchElementException("No action to redo");
	}
	
	public void reset() {
		clearNext();
		clearPrev();
	}

	private void clearNext() {
		next.clear();
		hasNext.set(false);
	}
	
	private void clearPrev() {
		previous.clear();
		hasPrevious.set(false);
	}

	@Override
	public void handleStateSaved() { isSaved.set(false); }
	
	@Override
	public void handleReversibleStateChange() { 
		hasPrevious.set(true);
		isSaved.set(false);
	 } 

	 @Override
	public void handleIrreversibleStateChange() { 
		clearPrev();
		isSaved.set(false);
	 } 
	
	public BooleanProperty hasNextProperty() { return hasNext; }
	public BooleanProperty hasPreviousProperty() { return hasPrevious; }
	public BooleanProperty isSavedProperty() { return isSaved; }
}