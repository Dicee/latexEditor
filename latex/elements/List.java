package latex.elements;

import latex.LateXMaker;

public class List extends AbstractLateXElement {

	public List(String content) {
		super(content,"list",5);
	}

	public String latexify(LateXMaker lm) {
		return lm.makeList(content.split(";"));		
	}
	
	public AbstractLateXElement clone() {		
		return new List(content);
	}
}
