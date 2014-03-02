package utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;

public class TokenReader extends BufferedReader {
	
	private String tokens;

	public TokenReader(Reader reader, String tokens) throws FileNotFoundException {
		super(reader);
		this.tokens = tokens;
	}
	
	public String readToNextToken() throws IOException {
		String result = "";
		int n;
		while ((n =  read()) != -1 && !isToken((char) n)) 
			result += (char) n;
		return n == -1 ? null : result;
	}

	private boolean isToken(char c) {
		int l = tokens.length();
		for (int i=0 ; i<l ; i++)
			if (tokens.charAt(i) == c) return true;
		return false;
	}
}
