package guifx;

import javafx.scene.layout.GridPane;

public class TemplateForm extends GridPane {
	private final Template t;
	
	public TemplateForm(Template t) {
		this.t = t;
		
		gridPane.add(commonCaracteristics,0,0,1,2);
	}
	
	
}
