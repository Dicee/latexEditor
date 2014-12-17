package guifx;

import static guifx.utils.Settings.strings;

import java.util.Iterator;
import java.util.Map;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import latex.elements.Template;

public class TemplateForm extends GridPane {
	public TemplateForm(Template t) {
		final Map<String,String>           params = t.getParameters();
		Iterator<Map.Entry<String,String>> it     = params.entrySet().iterator();

		if (it.hasNext()) {
			Map.Entry<String,String> param;
			int i;
			for (i=0, param = it.next() ; it.hasNext() ; i++, param = it.next()) {
				Label     label = new Label();
				TextField field = new TextField(param.getValue());
				
				label.textProperty().bind(strings.getObservableProperty(String.format("%s.%s",t.getTemplateName(),param.getKey())));
				label.setFont(LatexEditor.subtitlesFont);

				if (i % 2 == 0) {
					add(label,0,i/2);
					add(field,1,i/2);
				} else {
					add(label,2,i/2);
					add(field,3,i/2);
				}
			}
			setHgap(10);
			setVgap(5);
		}
	}
}
