package latex.elements;

import static properties.LanguageProperties.SUBSUBSECTION;
import latex.LateXMaker;


public class SubSubSection extends AbstractLateXElement {

	public SubSubSection(String content) {
		super(content,SUBSUBSECTION,4);
	}

	public String latexify(LateXMaker lm) {
		return lm.makeSubSubSection(content);
	}

	public AbstractLateXElement clone() {
		return new SubSubSection(content);
	}
}