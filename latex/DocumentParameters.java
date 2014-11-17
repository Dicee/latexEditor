package latex;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DocumentParameters {
	private String documentClass, alinea, chapterName;
	private Set<String> includes = new HashSet<>();
	private Set<String> packages = new HashSet<>();
	
	private static final String[] DEFAULT_PACKAGES = { 
		"color","graphicx","geometry","listings","textcomp","amssymb","amsmath","setspace","eurosym","gensymb" 
	};
	private static final String[] DEFAULT_INCLUDES = { "java.listing","js-html-css.listing" };
	
	public DocumentParameters(String docClass, String alinea, String chapterName) {
		this.documentClass = docClass;
		this.alinea        = alinea;
		this.chapterName   = chapterName;
		addPackage("T1","fontenc");
		addPackage("francais","babel");
		include(DEFAULT_INCLUDES);
		addPackages(DEFAULT_PACKAGES);
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
	
	public void include(String... names) {
		for (String name : names)
			includes.add(name);
	}
		
	public void addPackage(String option, String name) {
		packages.add(String.format("\\usepackage[%s]{%s}\n",option,name));
	}
	
	public void addPackages(String... names) {
		for (String name : names)
			packages.add(String.format("\\usepackage{%s}\n",name));
	}
	
	public StringBuilder mkString(StringBuilder sb) {
		String name = null;
		packages.stream().forEach(p -> sb.append(p));
		try {
			for (String path : includes) {
				name = path;
				sb.append(Files.readAllLines(Paths.get(DocumentParameters.class.getResource("includes/" + path).toURI())).stream()
					.collect(Collectors.joining("\n")));
			}
		} catch (Throwable t) {
			throw new Error(String.format("Package %s does not exist",name));
		}		
		return sb;
	}
}
