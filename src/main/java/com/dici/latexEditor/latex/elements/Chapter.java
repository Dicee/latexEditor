package com.dici.latexEditor.latex.elements;

import static com.dici.latexEditor.properties.LanguageProperties.CHAPTER;

import com.dici.latexEditor.latex.LateXMaker;

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