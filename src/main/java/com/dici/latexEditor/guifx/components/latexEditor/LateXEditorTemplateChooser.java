package com.dici.latexEditor.guifx.components.latexEditor;

import static com.dici.latexEditor.guifx.utils.Settings.bindProperty;
import static com.dici.latexEditor.latex.elements.Templates.TEMPLATES;
import static com.dici.latexEditor.properties.ConfigProperties.CHECKED_ICON;
import static com.dici.latexEditor.properties.LanguageProperties.SHOW_AVAILABLE_TEMPLATES;
import static com.dici.latexEditor.properties.LanguageProperties.TITLE;
import static javafx.geometry.Pos.CENTER;
import static javafx.geometry.Side.RIGHT;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.dici.latexEditor.guifx.LateXEditor;
import com.dici.latexEditor.latex.elements.Template;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

public class LateXEditorTemplateChooser extends BorderPane {
    private final ContextMenu  templatesList    = new ContextMenu();
    private Optional<MenuItem> selectedTemplate = Optional.empty();
	
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
				
		for (Map.Entry<String, List<Template>> entry : templatesToShow.entrySet())
			createMenuBuilderFromTemplateTitle(entry.getKey(), template, entry.getValue());
	}
	
	private void buildTemplateForm(Template template) {
		TemplateForm form = new TemplateForm(template);
		setCenter(form);
		setAlignment(form, CENTER);
	}
	
	private void createMenuBuilderFromTemplateTitle(String title, Template template, List<Template> availableTemplates) {
		Menu menu = new Menu(title);
		availableTemplates.stream().forEach(availableTemplate -> {
			MenuItem item = new MenuItem(availableTemplate.getTemplateName());
			item.setOnAction(ev -> {
				template.copyFrom(availableTemplate);
				TemplateForm form = new TemplateForm(template);
				setCenter(form);
				BorderPane.setAlignment(form, CENTER);
				
				Node checkedIcon = selectedTemplate.isPresent() ? 
				    selectedTemplate.get().getGraphic() : 
				    new ImageView(LateXEditor.getResourceImage(CHECKED_ICON));
				
				selectedTemplate.ifPresent(t -> t.setGraphic(null));
				item.setGraphic(checkedIcon);
				selectedTemplate = Optional.of(item);
			});
			menu.getItems().add(item);
		});
		templatesList.getItems().add(menu);
	}
}	