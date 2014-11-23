package latex.elements;

import latex.LateXMaker;


public class SubSubSection extends AbstractLateXElement {

	public SubSubSection(String content, LateXMaker lm) {
		super(content,lm,"subsubsection",4);
	}

	public String latexify() {
		return lm.makeSubSubSection(content);
	}

	public AbstractLateXElement clone() {
		return new SubSubSection(content,lm);
	}

}
