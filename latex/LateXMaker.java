package latex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import latex.elements.LateXElement;
import latex.elements.Template;
import scala.collection.mutable.StringBuilder;
import utils.FilterWriter;

public class LateXMaker {
	private int						chapterCount	= 0, figureCount = 1;
	private static final String[]	romanNumbers	= { "I", "II", "III", "IV", "VI", "VII", "VIII", "IX" };
	private DocumentParameters		parameters;
	
	public LateXMaker(DocumentParameters dp) {
		this.parameters = dp;
	}
	
	public LateXMaker() {
		this(new DocumentParameters());
	}
	
	 private String beginDocument() {
		 StringBuilder sb = new StringBuilder();		
		 
		 sb.append("\\documentclass{" + parameters.getDocumentClass() + "}\n");
		 parameters.latexify(sb,this); 
		 sb.append("\n\\begin{document}\n");			
		 sb.append("\\renewcommand{\\contentsname}{Sommaire}\n");
		 sb.append("\\renewcommand{\\chaptername}{" + parameters.getChapterName() + "}\n");
		 sb.append("\\renewcommand{\\thechapter}{\\Roman{chapter}}\n");		
		 return sb.toString();
	}
	
	public String makeParagraph(String text) {
		return "\\paragraph{}\n\\hspace{" + parameters.getAlinea() + "}\\textnormal{" + text + "}\n";
	}
	
	private String makeBalise(String name, String content) {		
		return "\\" + name + "{" + content +"}\n";	
	}
	
	public String makeCodeListing(String language, String text) {
		return String.format("\\begin{lstlisting}[language=%s]\n%s\n\\end{lstlisting}",language,text.trim());
	}
	
	public String makeChapter(String content) {
		chapterCount++;
		figureCount = 1;
		return makeBalise("chapter",content);
	}
	
	public String makeSection(String content) {
		return makeBalise("section",content);
	}
	
	public String makeSubsection(String content) {
		return makeBalise("subsection",content);
	}
	
	public String makeList(String[] list) {
		String result = "~\\\\\n\\begin{itemize}\n";
			for (String elt : list) 
				result += "\\item " + elt.trim() + "\\vspace{1mm}\n";
		return result + "\\end{itemize}\n";
	}
		
	public String includeGraphic(String path, String caption, float scale) {
		String result = "";
		result += "\\begin{center}\n";
		result += "\\includegraphics[scale=" + scale + "]{" + path + "}\n";
		result += "~\\\\~\\\\Figure " + romanNumbers[chapterCount-1] + "." + figureCount + " - " + caption + "\n";
		result += "\\end{center}\n";
		figureCount++;
		return result;
	}
	
	public String finishDocument() {
		return "\\end{document}";
	}

	public void makeDocument(File f, List<LateXElement> latexElements) throws IOException {
		chapterCount = 0;
		figureCount  = 1;
		
		FilterWriter fw = null;
		try {
			fw = new FilterWriter(new BufferedWriter(new FileWriter(f)),new LateXFilter());
			fw.writeln(beginDocument());
			for (LateXElement elt : latexElements) 
				fw.writeln(elt.latexify(this));	
			fw.writeln(finishDocument());
		} finally {
			if (fw != null) {
				fw.flush();
				fw.close();
			}
		}
	}

	public String includeGraphic(String path, String caption, String scale) {			
		return includeGraphic(path,caption,Float.parseFloat(scale));		
	}

	public String makeLateXCode(String content) {
		return content;
	}

	public String makeSubSubSection(String content) {
		return makeBalise("subsubsection",content);
	}
	
	public String makeTemplate(Template t) {
		String result = t.getText();

		Pattern p = Pattern.compile("\\{\\?\\s*(.+)\\s*\\?\\s*(.*)\\s*\\?\\s*\\}");
		Matcher m = p.matcher(result);
		
		while (m.find()) 
			result = result.replace(m.group(0),t.assertProperty(m.group(1)) ? m.group(2) : "");
		
		for (Map.Entry<String,String> param : t.getParameters().entrySet())
			result = result.replace(String.format("${%s}",param.getKey()),param.getValue());
		return result;
	}
	
	public String makePackage(String option, String name) {
		return option != null ?
				String.format("\\usepackage[%s]{%s}",option,name) : 
					String.format("\\usepackage{%s}",name);
	}
	
	public DocumentParameters getParameters() {
		return parameters;
	}
}
