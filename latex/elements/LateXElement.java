package latex.elements;

import static guifx.LatexEditor.TEMPLATES;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import latex.LateXMaker;
import latex.Textifiable;

public interface LateXElement extends Textifiable {
	public static int DEPTH_MIN = 0;
	public static int DEPTH_MAX = 5;
	
	public String latexify(LateXMaker lm);
	public int getDepth();
	public LateXElement clone();
	
	public static LateXElement newLateXElement(String opName, String content) {
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
            case "image"         : elt = new Inclusion      (content); break;
            default              : 
            	Template t = new Template();
            	Pattern  p = Pattern.compile("(template|title)\\s*\\[(.+)\\]");
            	Matcher  m = p.matcher(opName);
            	
            	Template from = new Template();
            	if (m.matches()) {
            		if (m.group(1).equals("title")) t = new Title();
            		t.copyFrom(from = TEMPLATES.get(m.group(2).substring(0,m.group(2).indexOf("."))).stream()
            			.filter(template -> template.getAbsoluteTemplateName().equals(m.group(2)))
            			.findFirst()
            			.get());
            	}
            	
            	t.copyFrom(content,from.getText(),from.getAbsoluteTemplateName());
            	elt = t;
        }
        return elt;
    }
}