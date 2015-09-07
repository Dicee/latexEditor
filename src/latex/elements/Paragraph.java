package latex.elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import latex.LateXMaker;

public class Paragraph extends AbstractLateXElement {
	private static Pattern titleReg = Pattern.compile("title=(.+)");

	public Paragraph(String content) {
		super(content,"paragraph",5);
	}

	public String latexify(LateXMaker lm) {
		int     index     = content.indexOf('\n');
		index             = index == -1 ? content.length() : index;
		String  firstLine = content.substring(0,index);
		Matcher m         = titleReg.matcher(firstLine.trim());
		return m.matches() ? lm.makeParagraph(m.group(1),content.substring(index + 1)) : lm.makeParagraph("",content);
	}
	
	public AbstractLateXElement clone() {		
		return new Paragraph(content);
	}
}