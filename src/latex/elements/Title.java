package latex.elements;

import static properties.LanguageProperties.TITLE;

public class Title extends Template {
	public Title() {
		super(TITLE,0);
	}

	@Override
	public AbstractLateXElement clone() {
		Title clone    = new Title();
		clone.content  = content;
		clone.parameters.putAll(parameters);
		return clone;
	}
}