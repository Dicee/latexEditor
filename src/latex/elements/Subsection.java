package latex.elements;

import static properties.LanguageProperties.SUBSECTION;
import latex.LateXMaker;

public class Subsection extends AbstractLateXElement {

	public Subsection(String content) {
		super(content,SUBSECTION,3);
	}

	public String latexify(LateXMaker lm) {
		return lm.makeSubsection(content);
	}
	
	public AbstractLateXElement clone() {		
		return new Subsection(content);
	}
}