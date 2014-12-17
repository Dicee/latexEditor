package latex.elements;

import latex.LateXMaker;

public class Paragraph extends AbstractLateXElement {

	public Paragraph(String content) {
		super(content,"paragraph",5);
	}

	public String latexify(LateXMaker lm) {
		return lm.makeParagraph(content);
	}
	
	public AbstractLateXElement clone() {		
		return new Paragraph(content);
	}
}
