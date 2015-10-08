package com.dici.latexEditor.guifx.components.latexEditor;

import static com.dici.latexEditor.guifx.utils.LateXEditorTreeUtils.getValue;
import static com.dici.latexEditor.guifx.utils.LateXEditorTreeUtils.newTreeItem;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import javafx.scene.control.TreeItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.dici.javafx.JavaFXThreadingRule;
import com.dici.javafx.NamedObject;
import com.dici.javafx.actions.ActionManager;
import com.dici.javafx.actions.ActionManagerImpl;
import com.dici.latexEditor.guifx.components.latexEditor.LateXEditorTreeView;
import com.dici.latexEditor.guifx.utils.Settings;
import com.dici.latexEditor.latex.elements.Chapter;
import com.dici.latexEditor.latex.elements.LateXElement;
import com.dici.latexEditor.latex.elements.Paragraph;
import com.dici.latexEditor.latex.elements.PreprocessorCommand;

public class LateXEditorTreeViewTest {
    private ActionManager actionManager;
    private LateXEditorTreeView treeView;
    private TreeItem<NamedObject<LateXElement>> chapter;
    private TreeItem<NamedObject<LateXElement>> paragraph;

    static {
        Settings.init();
    }
    
    @Rule public JavaFXThreadingRule javafxRule = new JavaFXThreadingRule();
    
    @Before
    public void setUp() {
        this.actionManager = new ActionManagerImpl();
        this.treeView      = new LateXEditorTreeView(new PreprocessorCommand(""), actionManager);
        this.chapter       = newTreeItem(new Chapter(""));
        this.paragraph     = newTreeItem(new Paragraph(""));
        
        this.chapter           .getChildren().add(this.paragraph);
        this.treeView.getRoot().getChildren().add(this.chapter);
    }
    
    @Test
    public void pasteRawContentAsChildren() {
        setSystemClipboardContent("> chapter ## some chapter title ## >> paragraph ## some paragraph ##");
        
        treeView.getSelectionModel().select(chapter);
        treeView.pasteRawContentToSelectedNode();
        
        assertHasChildren(treeView.getRoot(), new Chapter(""), new Chapter("some chapter title"));
        assertHasChildren(treeView.getRoot().getChildren().get(0), new Paragraph(""));
        assertHasChildren(treeView.getRoot().getChildren().get(1), new Paragraph("some paragraph"));

        actionManager.undo();
        assertCorrectlyReset();
    }

    @Test
    public void pasteRawContentAsSiblings() {
        setSystemClipboardContent("> chapter ## some chapter title ## >> paragraph ## some paragraph ##");
        treeView.getSelectionModel().select(paragraph);
        treeView.pasteRawContentToSelectedNode();

        TreeItem<NamedObject<LateXElement>> root = treeView.getRoot();
        assertHasChildren(root, new Chapter(""));
        assertHasChildren(root.getChildren().get(0), new Paragraph(""), new Chapter("some chapter title"));
        assertHasChildren(root.getChildren().get(0).getChildren().get(1), new Paragraph("some paragraph"));
        
        actionManager.undo();
        assertCorrectlyReset();
    }

    private void assertCorrectlyReset() {
        assertHasChildren(treeView.getRoot(), new Chapter(""));
        assertHasChildren(treeView.getRoot().getChildren().get(0), new Paragraph(""));
    }

    @Test
    public void cancelPasteRawContentAsSiblings() {
        setSystemClipboardContent("> chapter ## some chapter title ## >> paragraph ## some paragraph ##");
        
        treeView.getSelectionModel().select(paragraph);
        treeView.pasteRawContentToSelectedNode();
        
        TreeItem<NamedObject<LateXElement>> root = treeView.getRoot();
        assertHasChildren(root, new Chapter(""));
        assertHasChildren(root.getChildren().get(0), new Paragraph(""), new Chapter("some chapter title"));
        assertHasChildren(root.getChildren().get(0).getChildren().get(1), new Paragraph("some paragraph"));
    }

    private static void setSystemClipboardContent(String content) {
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(content);
        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }
    
    private static void assertHasChildren(TreeItem<NamedObject<LateXElement>> root, LateXElement... children) {
        assertThat(root.getChildren().size(), is(children.length));
        for (int i = 0; i < children.length; i++) assertThat(getValue(root.getChildren().get(i)), equalTo(children[i]));
    }
}