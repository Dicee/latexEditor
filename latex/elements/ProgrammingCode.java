package latex.elements;

import latex.LateXMaker;

public class ProgrammingCode extends LateXElement {
	
	public ProgrammingCode(String content, LateXMaker lm) {
		super(content,lm,"Code",5);
	}

	public String latexify() {
		int i = content.indexOf("\n");
		return lm.makeCodeListing(content.substring(0,i),content.substring(i));
	}

	public LateXElement clone() {
		return new ProgrammingCode(getText(),lm);
	}
}
