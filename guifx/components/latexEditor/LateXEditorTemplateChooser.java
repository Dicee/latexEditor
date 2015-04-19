package guifx.components.latexEditor;

import static guifx.utils.Settings.bindProperty;
import static latex.elements.Templates.TEMPLATES;
import guifx.TemplateForm;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import latex.elements.Template;

public class LateXEditorTemplateChooser {
	private static final ContextMenu	templatesList = new ContextMenu();
	
	private LateXEditorTemplateChooser() { }
	
	public static final BorderPane buildAvailableTemplatesList(Template template) {
		templatesList.getItems().clear();
		// creation of the UI elements
		Button showMenu = new Button();
		bindProperty(showMenu.textProperty(),"showAvailableTemplates");
		showMenu.setOnAction(ev -> {
			if (!templatesList.isShowing())
				templatesList.show(showMenu,Side.RIGHT,0,0);
			else
				templatesList.hide();
		});

		BorderPane borderPane = new BorderPane();
		borderPane.setTop(showMenu);
		borderPane.setPadding(new Insets(15));
		borderPane.setPrefHeight(300);
		BorderPane.setAlignment(showMenu,Pos.CENTER);
		BorderPane.setMargin(showMenu,new Insets(10,10,30,10));

		// creation of the popup menu
		buildPopupMenu(template,borderPane);

		TemplateForm form = new TemplateForm(template);
		borderPane.setCenter(form);
		BorderPane.setAlignment(form,Pos.CENTER);
		
		return borderPane;
	}

	private static void buildPopupMenu(Template template, BorderPane borderPane) {
		Map<String,List<Template>> templatesToShow = 
			TEMPLATES.entrySet().stream()
				.filter(entry-> template.getType().equals("title") ? entry.getKey().equals("titlePage") : !entry.getKey().equals("titlePage"))
				.collect(Collectors.toMap(entry -> entry.getKey(),entry -> entry.getValue()));
				
		for (Map.Entry<String,List<Template>> entry : templatesToShow.entrySet())
			createMenuBuilderFromTemplateTitle(entry.getKey(),template,entry.getValue(),borderPane);
	}
	
	private static void createMenuBuilderFromTemplateTitle(String title, Template template, List<Template> availableTemplates, BorderPane borderPane) {
		Menu menu = new Menu(title);
		availableTemplates.stream().forEach(availableTemplate -> {
			MenuItem item = new MenuItem(availableTemplate.getTemplateName());
			item.setOnAction(ev -> {
				template.copyFrom(availableTemplate);
				TemplateForm form = new TemplateForm(template);
				borderPane.setCenter(form);
				BorderPane.setAlignment(form,Pos.CENTER);
			});
			menu.getItems().add(item);
		});
		templatesList.getItems().add(menu);
	}
}	