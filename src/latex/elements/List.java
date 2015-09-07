package latex.elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import latex.LateXMaker;

public class List extends AbstractLateXElement {
	private static Pattern newlineReg = Pattern.compile("newline=(.+)");
	
	public List(String content) {
		super(content,"list",5);
	}

	public String latexify(LateXMaker lm) {
		int     index     = content.indexOf('\n');
		index             = index == -1 ? content.length() : index;
		String  firstLine = content.substring(0,index);
		Matcher m         = newlineReg.matcher(firstLine.trim());
		String  newline   = m.matches() ? m.group().trim() : ";";
		return lm.makeList(content.split(newline));		
	}
	
	public AbstractLateXElement clone() {		
		return new List(content);
	}
}