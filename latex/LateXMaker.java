package latex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import latex.elements.LateXElement;
import utils.FilterWriter;

public class LateXMaker {
	private int chapterCount = 0, figureCount = 1;	
	private static final String[] romanNumbers = { "I","II","III","IV","VI","VII","VIII","IX" };
	private DocumentParameters parameters;
	
	public LateXMaker(DocumentParameters dp) {
		this.parameters = dp;
	}
	
	public LateXMaker() {
		this(new DocumentParameters());
	}
	
	 private String beginDocument() {
		 String result = "";		
		 result += "\\usepackage[T1]{fontenc}\n";
		 result += "\\usepackage[francais]{babel}\n";
		 result += "\\usepackage{color}\n";
		 result += "\\usepackage{graphicx}\n";
		 result += "\\usepackage{geometry}\n";
		 result += "\\usepackage{listings}\n";
		 result += "\\usepackage{textcomp}\n";
		 result += "\\usepackage{amssymb}\n";
		 result += "\\usepackage{amsmath}\n";
		 result += "\\definecolor{purple}{rgb}{0.5,0,0.41}";
		 result += "\\geometry{top=3cm, bottom=3cm, left=2.6cm , right=2.6cm}\n";
		 result += "\\lstset{\n";
		 result += "language=Java,\n";
		 result += "basicstyle=\\normalsize,\n";
		 result += "upquote=true,\n";
		 result += "aboveskip={1.5\\baselineskip},\n";
		 result += "columns=fullflexible,\n";
		 result += "showstringspaces=false,\n";
		 result += "extendedchars=true,\n";
		 result += "breaklines=true,\n";
		 result += "showtabs=false,\n";
		 result += "showspaces=false,\n";
		 result += "tabsize=4,\n";
		 result += "showstringspaces=false,\n";
		 result += "identifierstyle=\\ttfamily,\n";
		 result += " keywordstyle=\\bf\\color[rgb]{0.5,0,0.41},\n";
		 result += "commentstyle=\\color[rgb]{0.25,0.37,0.75},\n";
		 result += "stringstyle=\\color[rgb]{0.16,0,1},}\n";
		 result += "\\begin{document}\n";			
		 result += "\\renewcommand{\\contentsname}{Sommaire}\n";
		 result += "\\renewcommand{\\chaptername}{" + parameters.getChapterName() + "}\n";
		 result += "\\renewcommand{\\thechapter}{\\Roman{chapter}\n}\n";		
		 return result;
	}
		
	public String makeTitlePage(String title, String author) {
		String result = "";
		result += "\\documentclass{" + parameters.getDocumentClass() + "}\n";
		result += "\\huge{\\title{" + title + "}}\n";
		result += "\\large{\\author{" + author + "}}\n";
		result += beginDocument();
		result += "\\maketitle\n";
		result += "\\tableofcontents\n";
		return result;
	}
	
	public String makeTitlePage(String title, String author, String date) {
		String result = makeTitlePage(title,author) + "\n";
		result += "\\large{\\date{" + date + "}}\n";	
		return result;
	}
	
	public String makeParagraph(String text) {
		return "\\paragraph{\\hspace{" + parameters.getAlinea() + "}\\textnormal{" + text +"}}\n";
	}
	
	private String makeBalise(String name, String content) {		
		return "\\" + name + "{" + content +"}\n";	
	}
	
	public String makeCodeListing(String text) {
		return "\\begin{lstlisting}\n" + text + "\n\\end{lstlisting}";
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
				result += "\\item " + elt + "\\vspace{1mm}\n";
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

	public void save(File f, List<LateXElement> latexElements) throws IOException {
		chapterCount = 0;
		figureCount  = 1;
		FilterWriter fw = null;
		try {
			fw = new FilterWriter(new BufferedWriter(new FileWriter(f)),new LateXFilter());
			for (LateXElement elt : latexElements) 
				fw.writeln(elt.latexify());			
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
}
