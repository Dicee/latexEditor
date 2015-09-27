package guifx.utils;

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
import scala.collection.mutable.StringBuilder;

import com.dici.files.TokenParser;
import com.dici.files.TokenParser.TokenIterator;
import com.dici.javafx.components.ControlledTreeView.NamedList;
import com.google.common.base.Throwables;

public class JavatexIO {
    public static final TokenParser JAVATEX_PARSER = new TokenParser("##"); 
    
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
		return readFromJavatex(JAVATEX_PARSER.parse(f), params);
	}

	public static final List<Pair<Integer,LateXElement>> readFromJavatex(String content, DocumentParameters params) throws WrongFormatException {
	    try {
	        return readFromJavatex(JAVATEX_PARSER.parse(content), params);
	    } catch (IOException e) {
	        // should not happen
	        throw Throwables.propagate(e);
	    } catch (WrongFormatException e) {
	        throw e;
	    }
	}

	private static final List<Pair<Integer,LateXElement>> readFromJavatex(TokenIterator tokens, DocumentParameters params) 
	        throws WrongFormatException, IOException {
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
        tokens.close();
        return res;
    }
}