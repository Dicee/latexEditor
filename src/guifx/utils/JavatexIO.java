package guifx.utils;

import static com.dici.files.FileUtils.toExtension;
import static latex.elements.LateXElement.newLateXElement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
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
		ProcessBuilder pb = new ProcessBuilder("pdflatex", "-enable-installer", toExtension(file, "tex").getCanonicalPath());
		pb.directory(dir.getAbsoluteFile());
		return pb;
	}
	
	public static final void toTex(LateXMaker lm, List<LateXElement> elts, String path) throws IOException {
		lm.makeDocument(toExtension(path, "tex"), elts);
	}
	
	public static final void saveAsJavatex(File file, NamedList<LateXElement> elts, LateXMaker lm) throws IOException {
		BufferedWriter     bw            = Files.newBufferedWriter(file.toPath());
		Iterator<String>   names         = elts.getKey().iterator();
		List<LateXElement> lateXElements = elts.getValue();

		bw.write(lm.getParameters().textify(new StringBuilder()).toString());
		for (LateXElement l : lateXElements) bw.write(String.format("%s %s\n", names.next(), l.textify()));
		bw.flush();
		bw.close();
	}
	
	public static final List<Pair<Integer, LateXElement>> readFromJavatex(File f, DocumentParameters params) 
	        throws IOException, FileNotFoundException, WrongFormatException {
		return readFromJavatex(JAVATEX_PARSER.parse(f), params);
	}

	public static final List<Pair<Integer, LateXElement>> readFromJavatex(String content, DocumentParameters params) throws WrongFormatException {
	    try {
	        return readFromJavatex(JAVATEX_PARSER.parse(content), params);
	    } catch (IOException e) {
	        // should not happen
	        throw Throwables.propagate(e);
	    } catch (WrongFormatException e) {
	        throw e;
	    }
	}

	private static final List<Pair<Integer, LateXElement>> readFromJavatex(TokenIterator tokens, DocumentParameters params) 
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
                        res.add(new Pair<>(m.group(1).length(), newLateXElement(m.group(2) + param, content)));
                    } else
                        throw new WrongFormatException(decl);
            }
        }
        tokens.close();
        return res;
    }
}