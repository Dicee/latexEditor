package latex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import latex.elements.LateXElement;

public class Package implements LateXElement, Comparable<Package> {
	String	name;
	String	option;

	public Package(String name) {
		this(name,null);
	}

	public Package(String name, String option) {
		this.name   = name;
		this.option = option;
	}

	@Override
	public String getText() {
		return option == null ?	name : String.format("%s[%s]",name,option);
	}

	@Override
	public void setText(String text) {
		String  pattern = "(\\w*)\\s*\\[\\s*(\\w*)\\s*\\]";
		Pattern p       = Pattern.compile(pattern);
		Matcher m       = p.matcher(text.trim());
		if (m.matches()) {
			name   = m.group(1);
			option = m.group(2);
		} else
			name = text;
	}

	@Override
	public String getType() {
		return "Package";
	}

	@Override
	public String textify() {
		return getText();
	}

	@Override
	public String latexify() {
		return option != null ?
			String.format("\\usepackage[%s]{%s}",option,name) : 
			String.format("\\usepackage{%s}",name);
	}

	@Override
	public int getDepth() {
		return LateXElement.DEPTH_MAX;
	}

	@Override
	public String toString() {
		return option != null ?	String.format("%s[%s]",name,option) : name;
	}
	
	@Override
	public LateXElement clone() {
		return new Package(name,option);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + name.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		Package other = (Package) obj;
		return name.equals(other.name);
	}

	public String getName() {
		return name;
	}

	public String getOption() {
		return option;
	}

	@Override
	public int compareTo(Package that) {
		return name.compareTo(that.name);
	}
}