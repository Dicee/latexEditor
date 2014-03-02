package latex.elements;

import latex.LateXMaker;

public class ProgrammingCode extends LateXElement {

	public ProgrammingCode(String content, LateXMaker lm) {
		super(content,lm,"Code",5);
	}

	public String latexify() {
		return lm.makeCodeListing(content);
	}

	public LateXElement clone() {
		return new ProgrammingCode(getText(),lm);
	}
}
