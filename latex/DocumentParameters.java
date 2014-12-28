package latex;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import scala.collection.mutable.StringBuilder;
import scala.io.Codec;
import scala.io.Source;
import utils.Setter;

public class DocumentParameters {
	private String					documentClass, alinea, chapterName;
	private Set<String>				includes			= new HashSet<>();
	private Set<Package>			packages			= new HashSet<>();
	private ObservableList<Package>	packagesView		= FXCollections.observableArrayList();
	private ObservableList<String>	includesView		= FXCollections.observableArrayList();
	
	private static final String[] DEFAULT_PACKAGES = { 
		"color","graphicx","geometry","listings","textcomp","amssymb","amsmath","setspace","eurosym","gensymb" ,"tikz","epigraph"
	};
	private static final String[] DEFAULT_INCLUDES = { "java.listing","js-html-css.listing" };
	
	public DocumentParameters(String docClass, String alinea, String chapterName) {
		this.documentClass = docClass;
		this.alinea        = alinea;
		this.chapterName   = chapterName;
		addPackage("fontenc","T1");
		addPackage("babel","francais");
		include(DEFAULT_INCLUDES);
		addPackages(DEFAULT_PACKAGES);
	}
	
	public DocumentParameters() {
		this("report","8mm","Chapitre");
	}
	
	public void include(String name) {
		if (!name.isEmpty() && includes.add(name)) 
			includesView.add(name);
	}
	
	public void include(String... names) {
		for (String name : names)
			include(name);
	}
	
	public void removeInclude(String name) {
		includes.remove(name);
		includesView.remove(name);
	}
		
	public void addPackage(String name, String option) {
		if (!name.isEmpty()) {
			Package p = new Package(name,option);
			packages    .add(p);
			packagesView.add(p);
		}
	}
	
	public void addPackage(String name) {
		if (!name.isEmpty()) {
			Package p = new Package("");
			p.setText(name);

			if (packages.contains(p)) {
				packages.remove(p);
				packagesView.remove(p);
			}

			packages.add(p);
			packagesView.add(p);
			FXCollections.sort(packagesView);
		}
	}
	
	public void addPackages(String... names) {
		for (String name : names)
			addPackage(name);
	}
	
	public void removePackage(String name) {
		Package p = new Package("");
		p.setText(name);
		packages    .remove(p);
		packagesView.remove(p);
	}
	
	public void clear() {
		packages    .clear();
		packagesView.clear();
		includes    .clear();
		includesView.clear();
	}
	
	public StringBuilder latexify(StringBuilder sb, LateXMaker lm) {
		return mkString("","","",
			p -> p.latexify(lm),
			name -> Source.fromURL(DocumentParameters.class.getResource("includes/" + name),Codec.UTF8()).addString(sb),
			sb);
	}
	
	public StringBuilder textify(StringBuilder sb) {
		mkString(" packages ##\n","##\n commands ##\n","##",Package::toString,name -> sb.append(name + "\n"),sb);
		sb.append(" documentSettings ##\n");
		sb.append(format("%s=%s\n","documentClass",documentClass));
		sb.append(format("%s=%s\n","alinea",alinea));
		sb.append(format("%s=%s\n","chapterName",chapterName));
		sb.append("##\n");
		return sb;
	}
	
	private StringBuilder mkString(String before, String sep, String after, Function<Package,String> packageConverter, 
			Consumer<String> commandConverter, StringBuilder sb) {
		FXCollections.sort(packagesView);

		sb.append(before);
		packages.stream().forEach(p -> sb.append(packageConverter.apply(p) + "\n"));
		sb.append(sep);
		
		String name = null;
		try {
			for (String path : includes) {
				name = path;
				commandConverter.accept(name);
			}
		} catch (Throwable t) {
			throw new Error(String.format("Package %s does not exist",name));
		}		
		sb.append(after + "\n");
		return sb;
	}
	
	public void loadSettings(String settings) {
		// Temporary fix... those fields may be replaced by an HashMap later if
		// the application requires to add some more. For the moment, it would change
		// too much code for no real benefit
		Map<String,Setter<String>> setters = new HashMap<>();
		setters.put("documentClass",this::setDocumentClass);
		setters.put("alinea"       ,this::setAlinea       );
		setters.put("chapterName"  ,this::setChapterName  );
		
		Pattern p = Pattern.compile("(\\S+)\\s*=\\s*(\\S+)");
		Matcher m = p.matcher(settings);
		
		while (m.find()) {
			if (setters.containsKey(m.group(1))) {
				setters.get(m.group(1)).set(m.group(2));
				System.out.println(m.group());
			}
		}
	}

	/**
	 * Returns a view of the selected packages. This view has a an uni-directional 
	 * binding with the actual collection of packages. Modifications on the view 
	 * will have no impact on the backing collection, use addPackage and
	 * addPackages methods in order to add new packages.
	 * @return a view of the selected packages
	 */
    public ObservableList<Package> getPackagesView() {
       return packagesView;
    }
    
    /**
	 * Returns a view of the selected customized commands. This view has a an
	 * uni-directional binding with the actual collection of packages. 
	 * Modifications on the view will have no impact on the backing collection, 
	 * use addPackage and addPackages methods in order to add new packages.
	 * @return a view of the selected packages
	 */
    public ObservableList<String> getIncludesView() {
       return includesView;
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

	public void setDocumentClass(String documentClass) {
		this.documentClass = documentClass;
	}

	public void setAlinea(String alinea) {
		this.alinea = alinea;
	}

	public void setChapterName(String chapterName) {
		this.chapterName = chapterName;
	}
}
