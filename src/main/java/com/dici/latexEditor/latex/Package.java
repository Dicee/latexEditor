package com.dici.latexEditor.latex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Package implements Comparable<Package> {
	String	name;
	String	option;

	public Package(String name) {
		this(name,null);
	}

	public Package(String name, String option) {
		this.name   = name;
		this.option = option;
	}

	public String getText() {
		return option == null ?	name : String.format("%s[%s]",name,option);
	}

	public void setText(String text) {
		String  pattern = "(\\w*)\\s*\\[(.+)\\]";
		Pattern p       = Pattern.compile(pattern);
		Matcher m       = p.matcher(text.trim());
		if (m.matches()) {
			name   = m.group(1);
			option = m.group(2);
		} else
			name = text;
	}

	public String textify() {
		return getText();
	}

	public String latexify(LateXMaker lm) {
		return lm.makePackage(option,name);
	}

	public String toString() {
		return option != null ?	String.format("%s[%s]",name,option) : name;
	}
	
	public Package clone() {
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