package latex.elements;

import latex.LateXMaker;

public class List extends LateXElement {

	public List(String content, LateXMaker lm) {
		super(content, lm,"Liste",5);
	}

	public String latexify() {
		return lm.makeList(content.split(";"));		
	}
	
	public LateXElement clone() {		
		return new List(getText(),lm);
	}
}
