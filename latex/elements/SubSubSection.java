package latex.elements;

import latex.LateXMaker;


public class SubSubSection extends LateXElement {

	public SubSubSection(String content, LateXMaker lm) {
		super(content,lm,"Sous-sous section",4);
	}

	public String latexify() {
		return lm.makeSubSubSection(content);
	}

	public LateXElement clone() {
		return new SubSubSection(content,lm);
	}

}
