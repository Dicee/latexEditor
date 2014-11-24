package latex.elements;

import latex.LateXMaker;

public class Title extends AbstractLateXElement {

	public Title(String content, LateXMaker lm) {
		super(content, lm,"title",0);
	}

	public String latexify() {
		try {
			String[] split = content.split(";");
			return lm.makeTitlePage(split[0],split[1]);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("Format du titre incorrect.\nLe format"
					+ " attendu est Titre;Auteur.");
		}
	}
	
	public AbstractLateXElement clone() {		
		return new Title(getText(),lm);
	}
}
