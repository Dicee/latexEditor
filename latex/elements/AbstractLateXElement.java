package latex.elements;

public abstract class AbstractLateXElement implements LateXElement {
	protected String		content;
	protected String		name;
	private int				depth;
	
	public AbstractLateXElement(String content, String name, int depth) {
		this.content = content;
		this.name    = name;
		this.depth   = depth;
	}
	
	@Override
	public String textify() {
		return getType() + " ##\n" + getText() + "\n##"; 
	}
	
	public abstract LateXElement clone();
	
	@Override
	public String getType() {
		return name;
	}

	@Override
	public String getText() {
		return content;
	}

	@Override
	public void setText(String content) {
		this.content = content;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public int getDepth() {
		return depth;
	}
}