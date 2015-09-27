package latex.elements;

import static latex.elements.Templates.TEMPLATES;
import static properties.LanguageProperties.CHAPTER;
import static properties.LanguageProperties.CODE;
import static properties.LanguageProperties.ENVIRONMENT;
import static properties.LanguageProperties.IMAGE;
import static properties.LanguageProperties.LATEX;
import static properties.LanguageProperties.LIST;
import static properties.LanguageProperties.PARAGRAPH;
import static properties.LanguageProperties.PREPROCESSOR;
import static properties.LanguageProperties.SECTION;
import static properties.LanguageProperties.SUBSECTION;
import static properties.LanguageProperties.SUBSUBSECTION;
import static properties.LanguageProperties.TITLE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import latex.LateXMaker;
import latex.Textifiable;
import properties.LanguageProperties;

public interface LateXElement extends Textifiable {
	public static int DEPTH_MIN = -1;
	public static int DEPTH_MAX =  5;
	
	public String latexify(LateXMaker lm);
	public int getDepth();
	public java.util.List<LateXElement> getChildren();
	public LateXElement clone();
	
	public static LateXElement newLateXElement(String opName, String content) {
    	LateXElement elt;
        switch (opName) {
            case TITLE        : elt = new Title              (       ); break;
            case CHAPTER      : elt = new Chapter            (content); break;
            case SECTION      : elt = new Section            (content); break;
            case SUBSECTION   : elt = new Subsection         (content); break;
            case SUBSUBSECTION: elt = new SubSubSection      (content); break;
            case PARAGRAPH    : elt = new Paragraph          (content); break;
            case LIST         : elt = new List               (content); break;
            case CODE         : elt = new ProgrammingCode    (content); break;
            case LATEX        : elt = new LateXCode          (content); break;
            case IMAGE        : elt = new Inclusion          (content); break;
            case PREPROCESSOR : elt = new PreprocessorCommand(content); break;
            case ENVIRONMENT  : elt = new Environment        (content); break;
            default           : 
            	Template t = new Template();
            	Pattern  p = Pattern.compile("(template|title)\\s*\\[(.+)\\]");
            	Matcher  m = p.matcher(opName);
            	
            	Template from = new Template();
            	if (m.matches()) {
            		if (m.group(1).equals("title")) t = new Title();
            		t.copyFrom(from = TEMPLATES.get(m.group(2).substring(0,m.group(2).indexOf("."))).stream()
            			.filter(template -> template.getAbsoluteTemplateName().equals(m.group(2)))
            			.findFirst()
            			// lazy fetch of the template
            			.orElseGet(() -> Templates.loadTemplate(m.group(2))));
            	}
            	
            	t.copyFrom(content,from.getText(),from.getAbsoluteTemplateName());
            	elt = t;
        }
        return elt;
    }
}