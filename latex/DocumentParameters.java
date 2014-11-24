package latex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import scala.collection.mutable.StringBuilder;
import scala.io.Codec;
import scala.io.Source;

public class DocumentParameters {
	private String					documentClass, alinea, chapterName;
	private Set<String>				includes			= new HashSet<>();
	private Set<Package>			packages			= new HashSet<>();
	private ObservableList<Package>	packagesView		= FXCollections.observableArrayList();
	private ObservableList<String>	includesView		= FXCollections.observableArrayList();
	
	private static final String[] DEFAULT_PACKAGES = { 
		"color","graphicx","geometry","listings","textcomp","amssymb","amsmath","setspace","eurosym","gensymb" 
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
	
	public StringBuilder latexify(StringBuilder sb) {
		return mkString("","","",
			Package::latexify,
			name -> Source.fromURL(DocumentParameters.class.getResource("includes/" + name),Codec.UTF8()).addString(sb),
			sb);
	}
	
	public StringBuilder textify(StringBuilder sb) {
		return mkString(" packages ##\n","##\n commands ##\n","##\n",
			Package::toString,
			name -> sb.append(name + "\n"),
			sb);
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

	public String getDocumentClass() {
		return documentClass;
	}

	public String getAlinea() {
		return alinea;
	}

	public String getChapterName() {
		return chapterName;
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
}
