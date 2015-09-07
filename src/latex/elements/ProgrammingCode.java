package latex.elements;

import latex.LateXMaker;

public class ProgrammingCode extends AbstractLateXElement {
	
	public ProgrammingCode(String content) {
		super(content,"code",5);
	}

	public String latexify(LateXMaker lm) {
		int i = content.indexOf("\n");
		return lm.makeCodeListing(content.substring(0,i),content.substring(i));
	}

	public AbstractLateXElement clone() {
		return new ProgrammingCode(content);
	}
}
