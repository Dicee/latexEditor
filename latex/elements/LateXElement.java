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
    
    public static LateXElement newLateXElement(String opName, String content,LateXMaker lm) {
        LateXElement elt;
        switch (opName) {
            case "Titre"             : elt = new Title(content,lm);           break;
            case "Chapitre"          : elt = new Chapter(content,lm);	     break;
            case "Section"           : elt = new Section(content,lm);         break;
            case "Sous-section"      : elt = new Subsection(content,lm);      break;
            case "Sous-sous section" : elt = new SubSubSection(content,lm);   break;
            case "Paragraphe"        : elt = new Paragraph(content,lm);       break;
            case "Liste"             : elt = new List(content,lm);            break;
            case "Code"              : elt = new ProgrammingCode(content,lm); break;
            case "Code LateX"        : elt = new LateXCode(content,lm);       break;
            default                  : elt = new Inclusion(content,lm);       break;
        }
        return elt;
    }
}
