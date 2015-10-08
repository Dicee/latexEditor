package com.dici.latexEditor.latex.elements;

import static com.dici.latexEditor.properties.LanguageProperties.LATEX;

import com.dici.latexEditor.latex.LateXMaker;

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