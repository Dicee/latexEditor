package guifx.components.latexEditor;

import static guifx.utils.Settings.properties;
import static guifx.utils.Settings.strings;
import guifx.LatexEditor;
import guifx.components.generics.IconSelectionBox;
import guifx.components.generics.IconSelectionView;
import guifx.utils.Settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javafx.geometry.Insets;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;

public class LateXEditorShortcutsPane extends TitledPane {
	private static final ActionListener NO_OP = new ActionListener() { @Override public void actionPerformed(ActionEvent e) { } };
	
	private final IconSelectionView	operators;
	private final IconSelectionView	greekAlphabet;

	public LateXEditorShortcutsPane() {
		operators     = setOperators();
		greekAlphabet = setGreekAlphabet();
		setOnClick(NO_OP);

		IconSelectionBox box = new IconSelectionBox(operators,greekAlphabet);
		box.setPadding(new Insets(5,5,5,20));
		
		setContent(box);
		Settings.bindProperty(textProperty(),"boxTitle");
	}
	
	private IconSelectionView setOperators() {
		final String[] ops = { 
				"\\cdot","+","-","\\frac{}{}","\\sqrt[]{}",
				"\\forall","\\partial","\\exists","\\nexists","\\varnothing",
				"\\bigcap","\\bigcup","\\bigint","\\prod","\\sum",
				"\\nabla","\\in","\\notin","\\ni","",
				"^{}","_{}","\\leq","\\geq","\\neq",
				"\\mid\\mid.\\mid\\mid"
		};
		Image img = LatexEditor.getResourceImage(properties.getProperty("operatorsIcon"));
		return new IconSelectionView(img,6,5,ops,strings.getObservableProperty("operators"));
	}

	private IconSelectionView setGreekAlphabet() {
		final String[] ctes = { 
				"\\alpha","\\beta","\\gamma","\\delta","\\epsilon","\\mu","\\nu","\\xi","\\pi","\\rho",
				"\\omega","\\Omega","\\theta","\\Delta","\\Psi","\\eta","\\lambda","\\sigma","\\tau",
				"\\chi","\\phi","\\infty"
		};
		Image img = LatexEditor.getResourceImage(properties.getProperty("alphabetIcon"));
		return new IconSelectionView(img,5,5,ctes,strings.getObservableProperty("greekAlphabet"));
	}

	public void setOnClick(ActionListener onClick) { 
		operators    .setActionListener(onClick);
		greekAlphabet.setActionListener(onClick);
	}
}

