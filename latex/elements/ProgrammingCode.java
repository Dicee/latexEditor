package latex.elements;

import latex.LateXMaker;

public class ProgrammingCode extends AbstractLateXElement {
	
	public ProgrammingCode(String content, LateXMaker lm) {
		super(content,lm,"code",5);
	}

	public String latexify() {
		int i = content.indexOf("\n");
		return lm.makeCodeListing(content.substring(0,i),content.substring(i));
	}

	public AbstractLateXElement clone() {
		return new ProgrammingCode(getText(),lm);
	}
}
