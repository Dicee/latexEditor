package latex.elements;

import latex.LateXMaker;

public class PreprocessorCommand extends AbstractLateXElement {
	public PreprocessorCommand(String content) {
		super(content,"preprocessor",-1);
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
