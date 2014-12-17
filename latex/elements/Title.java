package latex.elements;

import latex.LateXMaker;

public class Title extends Template {
	public Title() {
		super("title",0);
	}

	public String latexify(LateXMaker lm) {
		try {
			String[] split = content.split(";");
			return lm.makeTitlePage(split[0].trim(),split[1].trim());
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("Format du titre incorrect.\nLe format"
					+ " attendu est Titre;Auteur.");
		}
	}
	
//	@Override
//	public String getType() {
//		return "template";
//	}
	
	@Override
	public AbstractLateXElement clone() {
		Title clone = new Title();
		clone.content  = content;
		clone.parameters.putAll(parameters);
		return clone;
	}
}
