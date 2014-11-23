package latex.elements;

import latex.LateXMaker;

public abstract class AbstractLateXElement implements LateXElement {
	protected String		content;
	protected LateXMaker	lm;
	private String			name;
	private int				depth;
	
	public AbstractLateXElement(String content, LateXMaker lm, String name, int depth) {
		this.content = content;
		this.lm      = lm;
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
