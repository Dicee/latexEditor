package guifx.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import latex.LateXMaker;
import latex.elements.LateXElement;

public class JavatexIO {
	public static ProcessBuilder toPdfProcessBuilder(File dir, File file) throws IOException {
		String path = file.getCanonicalPath();
		ProcessBuilder pb = new ProcessBuilder("pdflatex","-halt-on-error",String.format("%s.tex",
				path.substring(0,path.lastIndexOf("."))));
		pb.directory(dir.getAbsoluteFile());
		return pb;
	}
	
	public static void toTex(LateXMaker lm, List<LateXElement> elts, String path) throws IOException {
		int    i    = path.lastIndexOf(".");
		path        = i == -1 ? path + ".tex" : path.substring(0,i) + ".tex";
		lm.makeDocument(new File(path),elts);
	}
}
