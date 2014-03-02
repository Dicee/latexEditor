package guifx;

import java.util.ArrayList;

import latex.elements.LateXElement;

public class DocumentState extends State<ArrayList<LateXElement>> {

    public DocumentState(ArrayList<LateXElement> state) {
        super(state);
    }

    public int compareTo(State<ArrayList<LateXElement>> state) {

        int l1 = currentState.size(), l2 = state.currentState.size();
        if (l1 != l2) {
            return -1;
        }
        for (int i = 0; i < l1; i++) {
            if (!currentState.get(i).getText().equals(state.currentState.get(i).getText())) {
                return 1;
            }
        }
        return 0;
    }

}
