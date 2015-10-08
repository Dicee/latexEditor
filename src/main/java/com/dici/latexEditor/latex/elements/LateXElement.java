package com.dici.latexEditor.latex.elements;

import static com.dici.latexEditor.latex.elements.Templates.TEMPLATES;
import static com.dici.latexEditor.properties.LanguageProperties.CHAPTER;
import static com.dici.latexEditor.properties.LanguageProperties.CODE;
import static com.dici.latexEditor.properties.LanguageProperties.ENVIRONMENT;
import static com.dici.latexEditor.properties.LanguageProperties.IMAGE;
import static com.dici.latexEditor.properties.LanguageProperties.LATEX;
import static com.dici.latexEditor.properties.LanguageProperties.LIST;
import static com.dici.latexEditor.properties.LanguageProperties.PARAGRAPH;
import static com.dici.latexEditor.properties.LanguageProperties.PREPROCESSOR;
import static com.dici.latexEditor.properties.LanguageProperties.SECTION;
import static com.dici.latexEditor.properties.LanguageProperties.SUBSECTION;
import static com.dici.latexEditor.properties.LanguageProperties.SUBSUBSECTION;
import static com.dici.latexEditor.properties.LanguageProperties.TITLE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dici.latexEditor.latex.LateXMaker;
import com.dici.latexEditor.latex.Textifiable;
import com.google.common.base.Objects;

public interface LateXElement extends Textifiable {
	public static int DEPTH_MIN = -1;
	public static int DEPTH_MAX =  5;
	
	String latexify(LateXMaker lm);
	int getDepth();
	java.util.List<LateXElement> getChildren();
	LateXElement clone();
	
	default boolean isOfType(String type) { return Objects.equal(getType(), type); }
	
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