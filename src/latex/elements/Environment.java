package latex.elements;

import latex.LateXMaker;

public class Environment extends AbstractLateXElement {

	public Environment(String content) {
		super(content,"environment",5);
	}

	@Override
	public String latexify(LateXMaker lm) {
		return lm.makeEnvironment(content);
	}

	@Override
	public LateXElement clone() {
		return new Environment(content);
	}
}
