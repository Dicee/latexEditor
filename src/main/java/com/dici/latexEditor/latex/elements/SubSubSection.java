package com.dici.latexEditor.latex.elements;

import static com.dici.latexEditor.properties.LanguageProperties.SUBSUBSECTION;

import com.dici.latexEditor.latex.LateXMaker;


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