package com.dici.latexEditor.latex;

import static com.dici.latexEditor.latex.LateXFilter.filter;
import static java.lang.String.format;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dici.latexEditor.latex.elements.LateXElement;
import com.dici.latexEditor.latex.elements.PreprocessorCommand;
import com.dici.latexEditor.latex.elements.Template;

import scala.collection.mutable.StringBuilder;

public class LateXMaker {
	private int						chapterCount	= 0, figureCount = 1;
	private static final String[]	romanNumbers	= { "I", "II", "III", "IV", "VI", "VII", "VIII", "IX" };
	private DocumentParameters		parameters;
	private boolean					preproc;
	
	public LateXMaker() { this(new DocumentParameters()); }
	public LateXMaker(DocumentParameters dp) { this.parameters = dp; }
	
	 private String beginDocument(String preproc) {
		 StringBuilder sb = new StringBuilder();		
		 
		 sb.append("\\documentclass{" + parameters.getDocumentClass() + "}\n");
		 parameters.latexify(sb,this);
		 sb.append(preproc + "\n");
		 sb.append("\n\\begin{document}\n");			
		 sb.append("\\renewcommand{\\chaptername}{" + filter(parameters.getChapterName()) + "}\n");
		 sb.append("\\renewcommand{\\thechapter}{\\Roman{chapter}}\n");		
		 return sb.toString();
	}
	 
	private String beginDocument() {
		 return beginDocument("");
	}
	
	public String makeParagraph(String title, String text) {
		return title.isEmpty() ? 
			format("\\paragraph{}\n\\hspace{%s}%s\n",parameters.getAlinea(),filter(text)) :
			format("\\paragraph{%s}\n%s\n",filter(title),filter(text));
	}
	
	private String makeBalise(String name, String content) {		
		return format("\\%s{%s}\n",name,filter(content));	
	}
	
	public String makeCodeListing(String language, String text) {
		return format("\\begin{lstlisting}[language=%s]\n%s\n\\end{lstlisting}",language,text.trim());
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
		StringBuilder result = new StringBuilder("\\vspace{1.5mm}\n\\begin{itemize}\n");
			for (String elt : list) 
				result.append(format("\\item %s\\vspace{1mm}\n",filter(elt.trim())));
		return result + "\\end{itemize}\n";
	}
		
	public String includeGraphic(String path, String caption, float scale) {
		StringBuilder result = new StringBuilder("");
		result.append("\\begin{center}\n");
		result.append(format("\\includegraphics[scale=%s]{%s}\n",String.valueOf(scale).replace(",","."),path));
		result.append(format("~\\\\~\\\\Figure %s.%d - %s\n",romanNumbers[chapterCount-1],figureCount,filter(caption)));
		result.append("\\end{center}\n");
		figureCount++;
		return result.toString();
	}
	
	public String finishDocument() {
		return "\\end{document}";
	}

	public File makeDocument(File f, List<LateXElement> latexElements) throws IOException {
		chapterCount = 0;
		figureCount  = 1;
		preproc      = false;
		
		try (BufferedWriter bw = Files.newBufferedWriter(f.toPath())) {
			String begin;
			if (latexElements.get(0) instanceof PreprocessorCommand) {
				PreprocessorCommand preproc = (PreprocessorCommand) latexElements.get(0);
				begin                       = beginDocument(preproc.latexify(this));
				latexElements               = latexElements.subList(1,latexElements.size());
			}
			else begin = beginDocument();
			bw.write(begin + "\n");
			
			for (LateXElement elt : latexElements) 
				bw.write(elt.latexify(this) + "\n");	
			bw.write(finishDocument() + "\n");
		}
		return f;
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
		String  result = t.getText();
		Pattern p      = Pattern.compile("(?s)\\{\\?\\s*(.+?)\\s*\\?\\s*(.*?)\\s*\\?\\s*\\}");
		Matcher m      = p.matcher(result);
		
		while (m.find()) 
			result = result.replace(m.group(0),t.assertProperty(m.group(1)) ? m.group(2).trim() : "");
		
		for (Map.Entry<String,String> param : t.getParameters().entrySet())
			result = result.replace(String.format("${%s}",param.getKey()),filter(param.getValue().trim()));
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

	public String makePreprocessorCommand(String content) {
		if (preproc) throw new IllegalStateException("Illegal use of the 'preprocessor' element");
		else preproc = true;
		return content;
	}

	public String makeEnvironment(String content) {
		throw new UnsupportedOperationException();
	}
}