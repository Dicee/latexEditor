package latex.elements;

import latex.LateXMaker;

public class Paragraph extends LateXElement {

	public Paragraph(String content, LateXMaker lm) {
		super(content, lm,"Paragraphe",5);
	}

	public String latexify() {
		return lm.makeParagraph(content);
	}
	
	public LateXElement clone() {		
		return new Paragraph(getText(),lm);
	}
}
