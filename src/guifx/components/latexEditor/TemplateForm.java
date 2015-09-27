package guifx.components.latexEditor;

import static guifx.utils.Settings.bindProperty;
import static guifx.utils.Settings.strings;
import static properties.LanguageProperties.PICK_COLOR;
import guifx.LateXEditor;

import java.util.Map;
import java.util.stream.Collectors;

import javafx.geometry.Insets;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import latex.elements.Template;

import com.dici.javafx.NamedObject;

public class TemplateForm extends BorderPane {
	public TemplateForm(Template template) {
		final Map<String,String>      params      = template.getParameters();
		ListView<NamedObject<String>> listView    = new ListView<>();
		TextArea                      textArea    = new TextArea();
		Label                         pickerLabel = new Label();
		ColorPicker                   colPicker   = new ColorPicker(); 
		HBox                          header      = new HBox(10,pickerLabel,colPicker);
		
		BorderPane.setMargin(header,new Insets(0,0,5,0));
		bindProperty(pickerLabel.textProperty(),PICK_COLOR);
		pickerLabel.setFont(LateXEditor.subtitlesFont);
		
		colPicker.setOnAction(ev -> {
			Color color = colPicker.getValue();
			textArea.setText(String
				.format("%.2f/%.2f/%.2f",color.getRed(),color.getGreen(),color.getBlue())
				.replaceAll(",","\\.")
				.replaceAll("/",","));
			colPicker.hide();
			colPicker.setValue(color);
		});
		
		setTop   (header);
		setLeft  (listView);
		setCenter(textArea);
		
		listView.getItems().addAll(params.keySet().stream()
			.filter(s -> strings.containsKey(String.format("%s.%s",template,s)))
			.map(s -> new NamedObject<>(strings.getObservableProperty(String.format("%s.%s",template,s)),s))
			.collect(Collectors.toList()));
		listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		listView.getSelectionModel().selectedItemProperty().addListener((observable,oldValue,newValue) -> textArea.setText(params.get(newValue.bean)));
		listView.getSelectionModel().select(0);
		
		listView.setCellFactory(new Callback<ListView<NamedObject<String>>, ListCell<NamedObject<String>>>() {
			public ListCell<NamedObject<String>> call(ListView<NamedObject<String>> param) {
				final ListCell<NamedObject<String>> cell = new ListCell<NamedObject<String>>() {
					@Override
					public void updateItem(NamedObject<String> item, boolean empty) {
						super.updateItem(item,empty);
						if (!empty && item != null)
							textProperty().bind(item.nameProperty());
						else {
							textProperty().unbind();
							setText("");
						}
					}
				};
				return cell;
			}
		});
		
		textArea.textProperty().addListener((observable,oldValue,newValue) -> {
			String selected = listView.getSelectionModel().getSelectedItem().bean;
			params.put(selected,newValue);
		});
	}
}