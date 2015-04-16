package guifx.utils;

import static guifx.utils.Settings.strings;
import javafx.stage.Stage;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialogs;

public class DialogsFactory {
	public static final Action showPreFormattedError(Stage owner, String titlePropertyName, String mastheadPropertyName, String msgPropertyName) {
		return Dialogs.create().owner(owner)
				.title(strings.getProperty(titlePropertyName))
				.masthead(strings.getProperty(mastheadPropertyName))
				.message(strings.getProperty(msgPropertyName))
				.showError();
	}
	
	public static final Action showError(Stage owner, String title, String masthead, String msg) {
		return Dialogs.create().owner(owner)
				.title(title)
				.masthead(masthead)
				.message(msg)
				.showError();
	}
}
