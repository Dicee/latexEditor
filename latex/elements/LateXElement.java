package latex.elements;

import latex.LateXMaker;
import latex.Textifiable;

public interface LateXElement extends Textifiable {
	public static int DEPTH_MIN = 0;
	public static int DEPTH_MAX = 5;
	
	public String latexify(LateXMaker lm);
	public int getDepth();
	public LateXElement clone();
	
	public static LateXElement newLateXElement(String opName, String content, LateXMaker lm) {
    	LateXElement elt;
        switch (opName) {
            case "title"         : elt = new Title          (       ); break;
            case "chapter"       : elt = new Chapter        (content); break;
            case "section"       : elt = new Section        (content); break;
            case "subsection"    : elt = new Subsection     (content); break;
            case "subsubsection" : elt = new SubSubSection  (content); break;
            case "paragraph"     : elt = new Paragraph      (content); break;
            case "list"          : elt = new List           (content); break;
            case "code"          : elt = new ProgrammingCode(content); break;
            case "latex"         : elt = new LateXCode      (content); break;
            default              : elt = new Inclusion      (content); break;
        }
        return elt;
    }
}
