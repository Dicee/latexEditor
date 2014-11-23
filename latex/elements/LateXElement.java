package latex.elements;

import latex.LateXMaker;
import latex.Textifiable;

public interface LateXElement extends Textifiable {
	public static int DEPTH_MIN = 0;
	public static int DEPTH_MAX = 5;
	
	public String latexify();
	public int getDepth();
	public LateXElement clone();
	
	public static LateXElement newLateXElement(String opName, String content,LateXMaker lm) {
    	LateXElement elt;
        switch (opName) {
            case "title"         : elt = new Title          (content,lm); break;
            case "chapter"       : elt = new Chapter        (content,lm); break;
            case "section"       : elt = new Section        (content,lm); break;
            case "subsection"    : elt = new Subsection     (content,lm); break;
            case "subsubsection" : elt = new SubSubSection  (content,lm); break;
            case "paragraph"     : elt = new Paragraph      (content,lm); break;
            case "list"          : elt = new List           (content,lm); break;
            case "code"          : elt = new ProgrammingCode(content,lm); break;
            case "latex"         : elt = new LateXCode      (content,lm); break;
            default              : elt = new Inclusion      (content,lm); break;
        }
        return elt;
    }
}
