package com.dici.latexEditor.guifx.components.latexEditor;

import static com.dici.latexEditor.guifx.LateXEditor.getResourceImage;
import static com.dici.latexEditor.guifx.utils.Settings.strings;
import static com.dici.latexEditor.properties.ConfigProperties.ALPHABET_ICON;
import static com.dici.latexEditor.properties.ConfigProperties.OPERATORS_ICON;
import static com.dici.latexEditor.properties.LanguageProperties.BOX_TITLE;
import static com.dici.latexEditor.properties.LanguageProperties.GREEK_ALPHABET;
import static com.dici.latexEditor.properties.LanguageProperties.OPERATORS;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javafx.geometry.Insets;
import javafx.scene.control.TitledPane;

import com.dici.javafx.components.IconSelectionBox;
import com.dici.javafx.components.IconSelectionView;
import com.dici.latexEditor.guifx.utils.Settings;

public class LateXEditorShortcutsPane extends TitledPane {
    private static final ActionListener NO_OP = new ActionListener() { @Override public void actionPerformed(ActionEvent e) { } };
    
    private final IconSelectionView operators;
    private final IconSelectionView greekAlphabet;

    public LateXEditorShortcutsPane() {
        operators     = setOperators();
        greekAlphabet = setGreekAlphabet();
        setOnClick(NO_OP);

        IconSelectionBox box = new IconSelectionBox(operators, greekAlphabet);
        box.setPadding(new Insets(5, 5, 5, 20));
        
        setContent(box);
        Settings.bindProperty(textProperty(), BOX_TITLE);
    }
    
    private IconSelectionView setOperators() {
        final String[] ops = { 
                "\\cdot", "+", "-", "\\frac{}{}", "\\sqrt[]{}", 
                "\\forall", "\\partial", "\\exists", "\\nexists", "\\varnothing", 
                "\\bigcap", "\\bigcup", "\\bigint", "\\prod", "\\sum", 
                "\\nabla", "\\in", "\\notin", "\\ni", "", 
                "^{}", "_{}", "\\leq", "\\geq", "\\neq", 
                "\\mid\\mid.\\mid\\mid"
        };
        return new IconSelectionView(getResourceImage(OPERATORS_ICON), 6, 5, ops, strings.getObservableProperty(OPERATORS));
    }

    private IconSelectionView setGreekAlphabet() {
        final String[] ctes = { 
                "\\alpha", "\\beta", "\\gamma", "\\delta", "\\epsilon", "\\mu", "\\nu", "\\xi", "\\pi", "\\rho", 
                "\\omega", "\\Omega", "\\theta", "\\Delta", "\\Psi", "\\eta", "\\lambda", "\\sigma", "\\tau", 
                "\\chi", "\\phi", "\\infty"
        };
        return new IconSelectionView(getResourceImage(ALPHABET_ICON), 5, 5, ctes, strings.getObservableProperty(GREEK_ALPHABET));
    }

    public void setOnClick(ActionListener onClick) { 
        operators    .setActionListener(onClick);
        greekAlphabet.setActionListener(onClick);
    }
}