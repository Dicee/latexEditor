package latex.elements;

import latex.LateXMaker;

public class LateXCode extends AbstractLateXElement {

	public LateXCode(String content, LateXMaker lm) {
		super(content,lm,"latex",5);
	}

	public String latexify() {
		return lm.makeLateXCode(content);
	}

	public AbstractLateXElement clone() {
		return new LateXCode(getText(),lm);
	}
}

