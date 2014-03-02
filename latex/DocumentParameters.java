package latex;

public class DocumentParameters {
	private String documentClass, alinea, chapterName;
	
	public DocumentParameters(String docClass, String alinea, String chapterName) {
		this.documentClass = docClass;
		this.alinea        = alinea;
		this.chapterName   = chapterName;
	}
	
	public DocumentParameters() {
		this("report","8mm","Chapitre");
	}

	public String getDocumentClass() {
		return documentClass;
	}

	public String getAlinea() {
		return alinea;
	}

	public String getChapterName() {
		return chapterName;
	}
}
