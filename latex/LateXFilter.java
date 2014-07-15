package latex;

import java.util.HashMap;
import java.util.Map;

import utils.TextFilter;

public class LateXFilter implements TextFilter {

	private Map<String,String> tokens;
	
	public LateXFilter() {
		tokens = new HashMap<String,String>();
		tokens.put("�","\\'{e}");
		tokens.put("�","\\`{u}");
		tokens.put("�","\\`{a}");
		tokens.put("�","\\`{e}");
		tokens.put("�","\\^{o}");
		tokens.put("�","\\^{e}");
		tokens.put("�","\\^{a}");
		tokens.put("�","\\^{u}");
		tokens.put("�","\\^{i}");
		tokens.put("�","\\c{c}");
		tokens.put("�","\\oe{}");		
		tokens.put("�","\\\"{i}");				
		//tokens.put("(?i)([^\\\\]|\\A)%","$1\\\\%");			
	}
	
	@Override
	public String filter(String s) {
		for (String key : tokens.keySet()) 
			s = s.replace(key,tokens.get(key));
		//The LateX commentaries are a bit particular case
		s = s.replaceAll("(?i)([^\\\\]|\\A)%","$1\\\\%");
		return s;
	}
}
