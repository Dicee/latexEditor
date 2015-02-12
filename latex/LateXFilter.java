package latex;

import java.util.HashMap;
import java.util.Map;

public class LateXFilter {

	private static final Map<String,String> tokens;
	
	static {
		tokens = new HashMap<String,String>();
		tokens.put("é","\\'{e}" );
		tokens.put("ù","\\`{u}" );
		tokens.put("à","\\`{a}" );
		tokens.put("è","\\`{e}" );
		tokens.put("ô","\\^{o}" );
		tokens.put("ê","\\^{e}" );
		tokens.put("â","\\^{a}" );
		tokens.put("û","\\^{u}" );
		tokens.put("î","\\^{i}" );
		tokens.put("ç","\\c{c}" );
		tokens.put("œ","\\oe{}" );		
		tokens.put("ï","\\\"{i}");				
		tokens.put("#","\\#"    );				
		tokens.put("&","\\&"    );				
		tokens.put("_","\\_"    );				
		tokens.put("%","\\%"    );				
	}
	
	public static String filter(String s) {
		for (String key : tokens.keySet()) 
			s = s.replace(key,tokens.get(key));
		return s;
	}
}