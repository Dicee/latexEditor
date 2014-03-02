package latex.elements;

import latex.LateXMaker;

public class Chapter extends LateXElement {

	public Chapter(String content, LateXMaker lm) {
		super(content, lm,"Chapitre",1);
	}

	public String latexify() {
		return lm.makeChapter(content);
	}

	public LateXElement clone() {		
		return new Chapter(getText(),lm);
	}
}
