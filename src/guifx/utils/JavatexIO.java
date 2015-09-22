package guifx.utils;

import guifx.components.generics.ControlledTreeView.NamedList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.util.Pair;
import latex.DocumentParameters;
import latex.LateXMaker;
import latex.elements.LateXElement;

import com.dici.collection.richIterator.RichIterator;
import com.dici.collection.richIterator.RichIterators;
import scala.collection.mutable.StringBuilder;

public class JavatexIO {
	public static final ProcessBuilder toPdfProcessBuilder(File dir, File file) throws IOException {
		String path = file.getCanonicalPath();
		ProcessBuilder pb = new ProcessBuilder("pdflatex","-halt-on-error",String.format("%s.tex",
				path.substring(0,path.lastIndexOf("."))));
		pb.directory(dir.getAbsoluteFile());
		return pb;
	}
	
	public static final void toTex(LateXMaker lm, List<LateXElement> elts, String path) throws IOException {
		int    i    = path.lastIndexOf(".");
		path        = i == -1 ? path + ".tex" : path.substring(0,i) + ".tex";
		lm.makeDocument(fixExtension(new File(path),"tex"),elts);
	}
	
	public static final File fixExtension(File file, String extension) {
		String path = file.getAbsolutePath();
		int    i    = path.lastIndexOf(".");
		path        = i == -1 ? path + "." + extension : path.substring(0,i) + "." + extension;
		return new File(path);
	}
	
	public static final void saveAsJavatex(File file, NamedList<LateXElement> elts, LateXMaker lm) throws IOException {
		File                    f             = new File(file.getAbsolutePath());
		BufferedWriter          fw            = new BufferedWriter(new FileWriter(f));
		Iterator<String>        names         = elts.getKey().iterator();
		List<LateXElement>      lateXElements = elts.getValue();

		fw.write(lm.getParameters().textify(new StringBuilder()).toString());
		for (LateXElement l : lateXElements) fw.write(String.format("%s %s\n",names.next(),l.textify()));
		fw.flush();
		fw.close();
	}
	
	public static final List<Pair<Integer,LateXElement>> readFromJavatex(File f, DocumentParameters params) 
			throws IOException, FileNotFoundException, WrongFormatException {
		try (RichIterator<String> tokens = RichIterators.tokens(f, "##")) {
		    List<Pair<Integer,LateXElement>> res = new LinkedList<>();  
    		while (tokens.hasNext()) {
    			String decl    = tokens.next().trim();
    			String content = tokens.next().trim();
    			
    			switch (decl) {
    				case "packages"        : params.addPackages(content.split("[;\\s+]|;\\s+")); break;
    				case "commands"        : params.include(content.split("[;\\s+]|;\\s+"    )); break;
    				case "documentSettings": params.loadSettings(content);                       break;
    				default:
    					Pattern p = Pattern.compile("(>*)\\s*(\\w+)\\s*(\\[(.*)\\])?");
    					Matcher m = p.matcher(decl);
    
    					if (m.matches()) {
    						String param = (m.group(4) == null || m.group(4).isEmpty()) ? "" : m.group(3);
    						res.add(new Pair<>(m.group(1).length(),LateXElement.newLateXElement(m.group(2) + param,content)));
    					} else
    						throw new WrongFormatException(decl);
    			}
    		}
    		return res;
		}
	}
}