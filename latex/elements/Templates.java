package latex.elements;

import guifx.utils.Settings;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Templates {
	public static final Map<String, List<Template>>	TEMPLATES  = new HashMap<>();
	public static final String						LATEX_HOME =
			System.getenv("LATEX_HOME").replace(System.getProperty("file.separator"),"/");
	
	public static boolean init() {
		if (LATEX_HOME != null) {
			File dir = new File(LATEX_HOME + "/includes/templates");
			Arrays.stream(dir.listFiles()).filter(File::isDirectory)
				.forEach(d -> Arrays.stream(d.listFiles()).filter(File::isDirectory).map(templateDir -> {
					Settings.loadTemplatesText(templateDir);
					return new Template(new File(String.format("%s/%s.template",templateDir.getPath(),templateDir.getName())));
				}).forEach(t -> {
					List<Template> list = TEMPLATES.containsKey(d.getName()) ? TEMPLATES.get(d.getName()) : new ArrayList<>();
					list.add(t);
					TEMPLATES.put(d.getName(),list);
				}));
		} else
			System.out.println("Warning : LATEX_HOME is not set in your environment, cannot load the templates and includes");
		return LATEX_HOME != null;
	}
	
	public static Template loadTemplate(String templateName) {
		Template res = null;
		Pattern  p   = Pattern.compile("(\\w+)\\.(\\w+)Template");
		Matcher  m   = p.matcher(templateName);
		if (m.matches()) {
			File f = new File(String.format("%s/includes/templates/%s/%s.template",LATEX_HOME,m.group(1),m.group(2)));
			List<Template> list = TEMPLATES.getOrDefault(m.group(1),new LinkedList<>(Arrays.asList(res = new Template(f))));
			TEMPLATES.put(m.group(1),list);
		} else 
			System.out.println(String.format("Warning : template %s was not found in LATEX_HOME/includes/templates",templateName));
		return res;
	}
}