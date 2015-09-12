package latex.elements;

import static properties.LanguageProperties.SECTION;
import latex.LateXMaker;

public class Section extends AbstractLateXElement {

	public Section(String content) {
		super(content,SECTION,2);
	}

	public String latexify(LateXMaker lm) {
		return lm.makeSection(content);
	}
	
	public AbstractLateXElement clone() {		
		return new Section(content);
	}
}