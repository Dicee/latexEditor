package latex.elements;

public class Title extends Template {
	public Title() {
		super("title",0);
	}

	@Override
	public AbstractLateXElement clone() {
		Title clone    = new Title();
		clone.content  = content;
		clone.parameters.putAll(parameters);
		return clone;
	}
}