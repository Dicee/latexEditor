package com.dici.latexEditor.latex.elements;

import static com.dici.latexEditor.properties.LanguageProperties.TEMPLATE;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dici.latexEditor.latex.LateXMaker;

import scala.io.Codec;
import scala.io.Source;

public class Template extends AbstractLateXElement {
	protected Map<String,String>		parameters		= new LinkedHashMap<>();
	protected final Map<String,String>	cache			= new LinkedHashMap<>();
	protected String					templateName	= "";
	
	public Template() { super("",TEMPLATE,5); }
	
	protected Template(String name, int depth) {
		super("",name,depth);
	}
	
	public Template(File f) {
		super("","template",-1);
		load(f);
	}
	
	public Template(URL url) {
		super("","template",-1);
		load(url);
	}
	
	private <T> void load(T source, Function<T,String> toText, String name) {
		templateName = name;
		content      = toText.apply(source);
		Pattern p    = Pattern.compile("\\$\\{([^\\}]+)\\}");
		Matcher m    = p.matcher(content);
		while (m.find()) parameters.put(m.group(1),"");
	}
	
	private void load(URL url) {
		String path = url.getPath();
		int    i    = path.lastIndexOf("templates");
		String name = path.substring(i + "templates".length() + 1,path.lastIndexOf(".")) + "Template";
		load(url,u -> Source.fromURL(u,Codec.UTF8()).mkString(),name);
	}

	private void load(File file) {
		String templateFamily = file.getParentFile().getParentFile().getName();
		int    index          = file.getName().lastIndexOf(".");
		String name           = String.format("%s.%sTemplate",templateFamily,file.getName().substring(0,index));
		load(file,f -> Source.fromFile(f,Codec.UTF8()).mkString(),name);
	}

	private void setContents(String content, String templateName) {
		this.content      = content;
		this.templateName = templateName;
	}
	
	public void copyFrom(String textified, String content, String templateName) {
		Pattern p = Pattern.compile("((.+)\\s*=\\s*(.+))");
		Matcher m = p.matcher(textified);
		while (m.find()) parameters.put(m.group(2),m.group(3));
		setContents(content,templateName);
	}
	
	public void copyFrom(Template t) {
		Map<String,String> map = new HashMap<>(t.parameters.size());
		cache.putAll(parameters);
		
		BinaryOperator<String> nonEmptyAndNullOrFirst = (s1,s2) -> {
			if (s1 == null)   return s2;
			if (s2 == null)   return s1;
			if (s1.isEmpty()) return s2;
			if (s2.isEmpty()) return s1;
			return s1;
		};
		
		// constructing the new map, we keep the existing values if they are left empty or null and associated
		// to the same key in the new template
		t.parameters.entrySet().forEach(entry -> map.put(entry.getKey(),nonEmptyAndNullOrFirst.apply(entry.getValue(),cache.get(entry.getKey()))));
		parameters = map;
		setContents(t.content,t.templateName);
	}
	
	public Map<String,String> getParameters() {
		return parameters;
	}
	
	public String getAbsoluteTemplateName() {
		return templateName;
	}
	
	public String getTemplateName() {
		return templateName.substring(templateName.lastIndexOf(".") + 1);
	}

	@Override
	public String latexify(LateXMaker lm) {
		return lm.makeTemplate(this);
	}
	
	@Override
	public String textify() {
		StringBuilder sb = new StringBuilder(String.format("%s[%s] ##\n",name,getAbsoluteTemplateName()));
		cache.putAll(parameters);
		cache.entrySet().stream()
			.filter(entry ->! entry.getValue().isEmpty())
			.forEach(entry -> sb.append(String.format("%s=%s\n",entry.getKey(),entry.getValue())));
		sb.append("##");
		return sb.toString();
	}
	
	@Override
	public LateXElement clone() {
		Template clone = new Template();
		clone.content  = content;
		clone.parameters.putAll(parameters);
		return clone;
	}
	
	public boolean assertProperty(String property) {
		Pattern p = Pattern.compile("(.+)\\s*!=\\s*null");
		Matcher m = p.matcher(property.trim());

		if (m.matches()) {
			String s = parameters.get(m.group(1));
			return s != null && !s.isEmpty();
		} else            
			return false;
	}
	
	@Override
	public String toString() {
		return getAbsoluteTemplateName();
	}
}	