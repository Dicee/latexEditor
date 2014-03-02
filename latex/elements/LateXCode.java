package latex.elements;

import latex.LateXMaker;

public class LateXCode extends LateXElement {

	public LateXCode(String content, LateXMaker lm) {
		super(content,lm,"Code LateX",5);
	}

	public String latexify() {
		return lm.makeLateXCode(content);
	}

	public LateXElement clone() {
		return new LateXCode(getText(),lm);
	}
}

