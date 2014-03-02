package latex.elements;

import latex.LateXMaker;
import latex.Textifiable;

public abstract class LateXElement implements Textifiable {
	
	public static int DEPTH_MIN = 0;
	public static int DEPTH_MAX = 5;	
	
	protected String content;
	protected LateXMaker lm;
	private String name;
	private int depth;
	
	public LateXElement(String content, LateXMaker lm, String name, int depth) {
		this.content = content;
		this.lm      = lm;
		this.name    = name;
		this.depth   = depth;
	}
	
	public abstract String latexify();
	
	public String textify() {
		return getName() + " #\n" + getText() + "\n#"; 
	}
	
	public String getName() {
		return name;
	}

	public String getText() {
		return content;
	}

	public void setText(String content) {
		this.content = content;
	}
	
	public String toString() {
		//return getText();
		return name;
	}
	
	public abstract LateXElement clone();

	public int getDepth() {
		return depth;
	}
}
