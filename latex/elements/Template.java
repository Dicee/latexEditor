package latex.elements;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import latex.LateXMaker;
import scala.io.Codec;
import scala.io.Source;

public class Template extends AbstractLateXElement {
	protected final Map<String,String> parameters = new HashMap<>();
	protected String templateName;
	
	public Template() { super("","template",5); }
	
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
		this.templateName = name;
		parameters.clear();
		
		content   = toText.apply(source);
		Pattern p = Pattern.compile("\\$\\{([^\\}]+)\\}");
		Matcher m = p.matcher(content);
		
		while (m.find()) parameters.put(m.group(1),"");
	}
	
	public void load(URL url) {
		String path = url.getPath();
		int    i    = path.lastIndexOf("templates");
		String name = path.substring(i + "templates".length() + 1,path.lastIndexOf(".")) + "Template";
		load(url,u -> Source.fromURL(u,Codec.UTF8()).mkString(),name);
	}

	public void load(File file) {
		String templateFamily = file.getParentFile().getParentFile().getName();
		int    index          = file.getName().lastIndexOf(".");
		String name           = String.format("%s.%sTemplate",templateFamily,file.getName().substring(0,index));
		load(file,f -> Source.fromFile(f,Codec.UTF8()).mkString(),name);
	}

	public Map<String,String> getParameters() {
		return parameters;
	}
	
	public String getTemplateName() {
		return templateName;
	}

	@Override
	public String latexify(LateXMaker lm) {
		return lm.makeTemplate(content,parameters);
	}
	
	@Override
	public LateXElement clone() {
		Template clone = new Template();
		clone.content  = content;
		clone.parameters.putAll(parameters);
		return clone;
	}
	
	@Override
	public String toString() {
		return templateName;
	}
}	
