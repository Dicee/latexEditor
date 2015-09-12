package latex.elements;

import static properties.LanguageProperties.CHAPTER;
import latex.LateXMaker;

public class Chapter extends AbstractLateXElement {

	public Chapter(String content) {
		super(content,CHAPTER,1);
	}

	public String latexify(LateXMaker lm) {
		return lm.makeChapter(content);
	}

	public AbstractLateXElement clone() {		
		return new Chapter(content);
	}
}