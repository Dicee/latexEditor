package latex.elements;

import latex.LateXMaker;

public class Inclusion extends AbstractLateXElement {

	public Inclusion(String content, LateXMaker lm) {
		super(content, lm,"image",5);
	}

	public String latexify() {
		try {
			String[] split = content.split(";");
			return lm.includeGraphic(split[0].trim(),split[1].trim(),split[2].trim());
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("Format d'inclusion d'image incorrect.\nLe format"
					+ " attendu est URL;Légende;Echelle." );
		}
	}
	
	public AbstractLateXElement clone() {		
		return new Inclusion(getText(),lm);
	}
}
