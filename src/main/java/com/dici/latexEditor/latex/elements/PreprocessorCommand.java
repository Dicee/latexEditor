package com.dici.latexEditor.latex.elements;

import static com.dici.latexEditor.properties.LanguageProperties.PREPROCESSOR;

import com.dici.latexEditor.latex.LateXMaker;

public class PreprocessorCommand extends AbstractLateXElement {
	public PreprocessorCommand(String content) {
		super(content,PREPROCESSOR,-1);
	}

	@Override
	public String latexify(LateXMaker lm) {
		return lm.makePreprocessorCommand(content);
	}

	@Override
	public LateXElement clone() {
		return new PreprocessorCommand(content);
	}
}
