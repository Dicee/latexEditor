package latex.elements;

import latex.LateXMaker;

public class Paragraph extends AbstractLateXElement {

	public Paragraph(String content, LateXMaker lm) {
		super(content, lm,"paragraph",5);
	}

	public String latexify() {
		return lm.makeParagraph(content);
	}
	
	public AbstractLateXElement clone() {		
		return new Paragraph(getText(),lm);
	}
}
