package latex.elements;

import latex.LateXMaker;

public class Section extends AbstractLateXElement {

	public Section(String content, LateXMaker lm) {
		super(content, lm,"section",2);
	}

	public String latexify() {
		return lm.makeSection(content);
	}
	
	public AbstractLateXElement clone() {		
		return new Section(getText(),lm);
	}
}
