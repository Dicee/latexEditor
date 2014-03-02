package latex.elements;

import latex.LateXMaker;

public class Section extends LateXElement {

	public Section(String content, LateXMaker lm) {
		super(content, lm,"Section",2);
	}

	public String latexify() {
		return lm.makeSection(content);
	}
	
	public LateXElement clone() {		
		return new Section(getText(),lm);
	}
}
