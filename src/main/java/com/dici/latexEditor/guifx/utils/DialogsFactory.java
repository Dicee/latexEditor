package com.dici.latexEditor.guifx.utils;

import static com.dici.javafx.Settings.strings;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialogs;

public class DialogsFactory {
	public static Action showPreFormattedError(Object owner, String titlePropertyName, String mastheadPropertyName, String msgPropertyName) {
		return preformattedDialog(owner, titlePropertyName, mastheadPropertyName, msgPropertyName).showError();
	}
	
	public static Action showError(Object owner, String title, String masthead, String msg) {
		return Dialogs.create().owner(owner).title(title).masthead(masthead).message(msg).showError();
	}
	
	public static Action showPreFormattedWarning(Object owner, String titlePropertyName, String mastheadPropertyName, String msgPropertyName) {
		return preformattedDialog(owner, titlePropertyName, mastheadPropertyName, msgPropertyName).showWarning();
	}
	
	private static Dialogs preformattedDialog(Object owner, String titlePropertyName, String mastheadPropertyName, String msgPropertyName) {
		return Dialogs.create()
				.owner(owner)
				.title(strings.getProperty(titlePropertyName))
				.masthead(strings.getProperty(mastheadPropertyName))
				.message(strings.getProperty(msgPropertyName));
	}
}