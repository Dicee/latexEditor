package guifx;

import static guifx.utils.Settings.strings;

import java.util.Iterator;
import java.util.Map;

import javafx.beans.value.ObservableValue;
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
			for (i=0, param = it.hasNext() ? it.next() : null ; param != null ; i++, param = it.hasNext() ? it.next() : null) {
				Label     label = new Label();
				TextField field = new TextField(param.getValue());
				
				final String key = param.getKey();
				field.textProperty().addListener((ObservableValue<? extends String> obs, String oldValue, String newValue) -> params.put(key,newValue));
				label.textProperty().bind(strings.getObservableProperty(String.format("%s.%s",t,key)));
				label.setFont(LatexEditor.subtitlesFont);

				int nCols = 4, mod = i % nCols;
				add(label,2*mod    ,i/nCols);
				add(field,2*mod + 1,i/nCols);
			}
			setHgap(10);
			setVgap(5);
		}
	}
}