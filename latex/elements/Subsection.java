package latex.elements;

import latex.LateXMaker;

public class Subsection extends LateXElement {

	public Subsection(String content, LateXMaker lm) {
		super(content, lm,"Sous-section",3);
	}

	public String latexify() {
		return lm.makeSubsection(content);
	}
	
	public LateXElement clone() {		
		return new Subsection(getText(),lm);
	}
}
