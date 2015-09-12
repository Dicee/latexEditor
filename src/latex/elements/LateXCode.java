package latex.elements;

import static properties.LanguageProperties.LATEX;
import latex.LateXMaker;

public class LateXCode extends AbstractLateXElement {

	public LateXCode(String content) {
		super(content,LATEX,5);
	}

	public String latexify(LateXMaker lm) {
		return lm.makeLateXCode(content);
	}

	public AbstractLateXElement clone() {
		return new LateXCode(getText());
	}
}