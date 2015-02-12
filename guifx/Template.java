package guifx;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import scala.io.Codec;
import scala.io.Source;

public class Template {
	private final Map<String,String> parameters = new HashMap<>();
	private String content = "";
	
	public <T> void load(T source, Function<T,String> toText) {
		parameters.clear();
		
		content   = toText.apply(source);
		Pattern p = Pattern.compile("\\$\\{([^\\}]+)\\}");
		Matcher m = p.matcher(content);
		
		while (m.find()) parameters.put(m.group(1),"");
	}
	
	public void load(URL url) {
		load(url,u -> Source.fromURL(u,Codec.UTF8()).mkString());
	}
	
	public void load(File file) {
		load(file,f -> Source.fromFile(f,Codec.UTF8()).mkString());
	}

	public String getContent() {
		String result = content;
		for (Map.Entry<String,String> param : parameters.entrySet())
			result = result.replace(String.format("${%s}",param.getKey()),param.getValue());
		return result;
	}
	
	public Map<String,String> getParameters() {
		return parameters;
	}
}	