package latex.elements;

import latex.LateXMaker;

public class Subsection extends AbstractLateXElement {

	public Subsection(String content, LateXMaker lm) {
		super(content, lm,"subsection",3);
	}

	public String latexify() {
		return lm.makeSubsection(content);
	}
	
	public AbstractLateXElement clone() {		
		return new Subsection(getText(),lm);
	}
}
