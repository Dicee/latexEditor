package guifx.components;

import static guifx.utils.Settings.bindProperty;
import static latex.elements.Templates.TEMPLATES;
import guifx.TemplateForm;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

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
	private ContextMenu								templatesList		= new ContextMenu();
	private ContextMenu	splitPane;
	
	
	private void buildAvailableTemplatesList(Template t) {
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
		Function<String, Consumer<List<Template>>> createMenu = title -> {
			Menu menu = new Menu(title);
			return templates -> {
				templates.stream().forEach(template -> {
					MenuItem item = new MenuItem(template.getTemplateName());
					item.setOnAction(ev -> {
						t.copyFrom(template);
						TemplateForm form = new TemplateForm(t);
						borderPane.setCenter(form);
						BorderPane.setAlignment(form,Pos.CENTER);
					});
					menu.getItems().add(item);
				});
				templatesList.getItems().add(menu);
			};
		};

		switch (t.getType()) {
			case "template":
				for (Map.Entry<String, List<Template>> entry : TEMPLATES.entrySet())
					if (!entry.getKey().equals("title"))
						createMenu.apply(entry.getKey()).accept(entry.getValue());
				break;
			case "title":
				createMenu.apply("titlePage").accept(TEMPLATES.get("titlePage"));
				break;
			default:
				throw new IllegalArgumentException(String.format("Unkown type %s",t.getType()));
		}

		TemplateForm form = new TemplateForm(t);
		borderPane.setCenter(form);
		BorderPane.setAlignment(form,Pos.CENTER);
	}
}
