package guifx.actions;

import java.util.Deque;
import java.util.LinkedList;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class ActionManager {
	private Deque<CancelableAction>	previous	= new LinkedList<>();
	private Deque<CancelableAction>	next		= new LinkedList<>();

	private BooleanProperty			hasNext		= new SimpleBooleanProperty(false);
	private BooleanProperty			hasPrevious	= new SimpleBooleanProperty(false);
	
	public void perform(Action action) {
		action.peform();
		if (action instanceof CancelableAction) { previous.push((CancelableAction) action); hasPrevious.set(true); }
		else                                    clearPrev();
		clearNext();
	}
	
	public void undo() {
		if (!previous.isEmpty()) { 
			next.push(previous.pop());
			hasNext.set(true);
			next.peek().cancel();
		}
	}
	
	public void redo() {
		if (!next.isEmpty()) perform(next.pop());
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
	
	public BooleanProperty hasNextProperty() {
		return hasNext;
	}
	
	public BooleanProperty hasPreviousProperty() {
		return hasPrevious;
	}
}