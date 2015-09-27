package guifx.components.latexEditor;

import static guifx.utils.Settings.bindProperty;
import static javafx.geometry.Pos.CENTER;
import static javafx.geometry.Side.RIGHT;
import static latex.elements.Templates.TEMPLATES;
import static properties.LanguageProperties.SHOW_AVAILABLE_TEMPLATES;
import static properties.LanguageProperties.TITLE;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import latex.elements.Template;

public class LateXEditorTemplateChooser extends BorderPane {
	private final ContextMenu	templatesList = new ContextMenu();
	
	public LateXEditorTemplateChooser(Template template) {
		super();
		
		buildShowMenu    (        );
		buildPopupMenu   (template);
		buildTemplateForm(template);
	}

	private void buildShowMenu() {
		Button showMenu = new Button();
		bindProperty(showMenu.textProperty(), SHOW_AVAILABLE_TEMPLATES);
		showMenu.setOnAction(ev -> {
			if (!templatesList.isShowing()) templatesList.show(showMenu, RIGHT, 0, 0);
			else             				templatesList.hide();
		});

		setTop(showMenu);
		setPadding(new Insets(15));
		setPrefHeight(300);
		setAlignment(showMenu, CENTER);
		setMargin(showMenu, new Insets(10, 10, 30, 10));
	}

	private final void buildPopupMenu(Template template) {
		Map<String, List<Template>> templatesToShow = 
			TEMPLATES.entrySet().stream()
				.filter(entry -> template.getType().equals(TITLE) ^ !entry.getKey().equals("titlePage"))
				.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
				
		for (Map.Entry<String,List<Template>> entry : templatesToShow.entrySet())
			createMenuBuilderFromTemplateTitle(entry.getKey(), template, entry.getValue());
	}
	
	private void buildTemplateForm(Template template) {
		TemplateForm form = new TemplateForm(template);
		setCenter(form);
		setAlignment(form, CENTER);
	}
	
	private final void createMenuBuilderFromTemplateTitle(String title, Template template, List<Template> availableTemplates) {
		Menu menu = new Menu(title);
		availableTemplates.stream().forEach(availableTemplate -> {
			MenuItem item = new MenuItem(availableTemplate.getTemplateName());
			item.setOnAction(ev -> {
				template.copyFrom(availableTemplate);
				TemplateForm form = new TemplateForm(template);
				setCenter(form);
				BorderPane.setAlignment(form, CENTER);
			});
			menu.getItems().add(item);
		});
		templatesList.getItems().add(menu);
	}
}	