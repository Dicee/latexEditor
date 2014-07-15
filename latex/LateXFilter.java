package latex;

import java.util.HashMap;
import java.util.Map;

import utils.TextFilter;

public class LateXFilter implements TextFilter {

	private Map<String,String> tokens;
	
	public LateXFilter() {
		tokens = new HashMap<String,String>();
		tokens.put("é","\\'{e}");
		tokens.put("ù","\\`{u}");
		tokens.put("à","\\`{a}");
		tokens.put("è","\\`{e}");
		tokens.put("ô","\\^{o}");
		tokens.put("ê","\\^{e}");
		tokens.put("â","\\^{a}");
		tokens.put("û","\\^{u}");
		tokens.put("î","\\^{i}");
		tokens.put("ç","\\c{c}");
		tokens.put("œ","\\oe{}");		
		tokens.put("ï","\\\"{i}");				
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
