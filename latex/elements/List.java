package latex.elements;

import latex.LateXMaker;

public class List extends AbstractLateXElement {

	public List(String content, LateXMaker lm) {
		super(content, lm,"list",5);
	}

	public String latexify() {
		return lm.makeList(content.split(";"));		
	}
	
	public AbstractLateXElement clone() {		
		return new List(getText(),lm);
	}
}
