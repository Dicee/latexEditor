package latex.elements;

import latex.LateXMaker;

public class Title extends LateXElement {

	public Title(String content, LateXMaker lm) {
		super(content, lm,"Titre",0);
	}

	public String latexify() {
		try {
			System.out.println(content+"  "+lm);
			String[] split = content.split(";");
			return lm.makeTitlePage(split[0],split[1]);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("Format du titre incorrect.\nLe format"
					+ " attendu est Titre;Auteur.");
		}
	}
	
	public LateXElement clone() {		
		return new Title(getText(),lm);
	}
}
