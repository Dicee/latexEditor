package com.dici.latexEditor.latex.elements;

import static com.dici.latexEditor.properties.LanguageProperties.CODE;

import com.dici.latexEditor.latex.LateXMaker;

public class ProgrammingCode extends AbstractLateXElement {
	
	public ProgrammingCode(String content) {
		super(content,CODE,5);
	}

	public String latexify(LateXMaker lm) {
		int i = content.indexOf("\n");
		return lm.makeCodeListing(content.substring(0,i),content.substring(i));
	}

	public AbstractLateXElement clone() {
		return new ProgrammingCode(content);
	}
}
