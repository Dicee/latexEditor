package latex.elements;

import latex.LateXMaker;

public class Chapter extends AbstractLateXElement {

	public Chapter(String content, LateXMaker lm) {
		super(content,lm,"chapter",1);
	}

	public String latexify() {
		return lm.makeChapter(content);
	}

	public AbstractLateXElement clone() {		
		return new Chapter(getText(),lm);
	}
}
